package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import com.mojang.math.Axis;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.InWorldGuiGraphics;
import mod.chloeprime.gunsmithlib.client.util.WorldToScreenProjection;
import mod.chloeprime.gunsmithlib.mixin.client.minecraft.GuiGraphicsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3f;

import java.awt.*;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;
import java.util.Objects;

/**
 * 把世界坐标投影到屏幕，并在该位置绘制 HUD 的实现。
 * <p>
 * 实现方式是给 {@link GuiGraphicsAPI} 的坐标系加上一个锚点变换：
 * 每次绘制时先压入一次矩阵变换，把原点平移到锚点的投影位置并缩放，
 * 绘制结束后立刻弹出，因此不会污染 HUD 其它部分的渲染状态。
 * <p>
 * {@link #push_pose()}、{@link #translate(float, float, float)} 等变换方法维护的是
 * 锚点变换之上的用户变换栈，语义与 {@link GuiGraphicsAPI} 完全一致。
 * <p>
 * 裁剪区域（{@link #enable_scissor(int, int, int, int)}）在底层是屏幕坐标的状态，
 * 不受矩阵变换影响，因此这里会把矩形变换到屏幕坐标后再交给底层处理。
 *
 * @since 7.0.0
 */
public class InWorldGuiGraphicsImpl extends AbstractGuiGraphicsAPIImpl implements InWorldGuiGraphics {
    /**
     * 距离缩放模式下的参考距离：锚点距离相机为这个值时，
     * 1 个 HUD 单位恰好等于 1 个 GUI 像素（以基准视野为准；
     * 开镜等导致视野放大时，HUD 会按同样的比例一起放大）。
     */
    public static final float REFERENCE_DISTANCE = 8.0F;

    /**
     * 距离缩放时使用的最小距离，避免贴脸时缩放倍数过大。
     */
    public static final double MIN_DISTANCE = 0.5;

    private final GuiGraphics impl;
    private final float scale;
    private final double distance;
    private final @Nullable Matrix4f anchor;
    private final Deque<Matrix4f> transforms = new ArrayDeque<>();

    /**
     * @param impl     被封装的原版 {@link GuiGraphics}
     * @param anchor   锚点的屏幕坐标；{@code null} 表示锚点不可见
     * @param scale    每个 HUD 单位对应的 GUI 像素数，必须大于 0
     * @param distance 锚点到相机的距离，锚点不可见时传 {@code 0}
     */
    public InWorldGuiGraphicsImpl(GuiGraphics impl, @Nullable Vector2f anchor, float scale, double distance) {
        this.impl = Objects.requireNonNull(impl, "impl");
        if (!(scale > 0)) {
            throw new IllegalArgumentException("scale must be positive, but was " + scale);
        }
        this.scale = scale;
        this.distance = distance;
        this.anchor = anchor == null ? null : new Matrix4f()
                .translate(anchor.x, anchor.y, 0)
                .scale(scale, scale, 1);
        this.transforms.push(new Matrix4f());
    }

    /**
     * 创建一个锚定在世界坐标上的 HUD 绘制对象。
     *
     * @param impl           被封装的原版 {@link GuiGraphics}
     * @param worldPos       锚点的世界坐标
     * @param scale          每个 HUD 单位对应的 GUI 像素数
     * @param distanceScaled 为 {@code true} 时让 HUD 的视觉尺寸在世界中保持恒定
     *                       （锚点距离相机 {@link #REFERENCE_DISTANCE} 格时 1 个 HUD 单位对应
     *                       1 个 GUI 像素，否则按距离成比例缩放；并跟随 FOV 与瞄准缩放一起缩放），
     *                       适合血条这类“贴在世界物体上”的 HUD；
     *                       为 {@code false} 时 HUD 在屏幕上保持恒定尺寸，适合锁定准星这类 HUD
     * @return 锚定在世界坐标上的 HUD 绘制对象，锚点不可见时 {@link #visible()} 为 {@code false}
     */
    public static InWorldGuiGraphicsImpl at(GuiGraphics impl, Vec3 worldPos, float scale, boolean distanceScaled) {
        var screen = WorldToScreenProjection.worldToScreen(worldPos);
        if (screen == null) {
            return new InWorldGuiGraphicsImpl(impl, null, scale, 0);
        }
        var camera = Minecraft.getInstance().gameRenderer.getMainCamera().getPosition();
        var distance = camera.distanceTo(worldPos);
        var autoScale = distanceScaled ? getWorldScaledScale(worldPos, distance) : 1;
        return new InWorldGuiGraphicsImpl(impl, screen, scale * autoScale, distance);
    }

    /**
     * 计算「视觉尺寸在世界中保持恒定」时，每个 HUD 单位对应的 GUI 像素数。
     * <p>
     * 基准是「锚点距离相机 {@link #REFERENCE_DISTANCE} 格、且处于基准视野（未开镜）时，
     * 1 个 HUD 单位 = 1 个 GUI 像素」。
     * 实际尺寸由锚点处 1 个方块占多少屏幕像素推算而来，
     * 所以会跟随 FOV 设置和瞄准/开镜导致的视野放大一起缩放，
     * 不会出现「开镜后 HUD 相对世界变小」的问题。
     *
     * @param worldPos 锚点的世界坐标
     * @param distance 锚点到相机的距离，取不到投影信息时用它近似
     * @return 每个 HUD 单位对应的 GUI 像素数
     */
    private static float getWorldScaledScale(Vec3 worldPos, double distance) {
        var pixelsPerBlock = WorldToScreenProjection.screenPixelsPerBlock(worldPos);
        if (!(pixelsPerBlock > 0)) {
            // 取不到投影信息（例如相机还没初始化）时，退回到按距离近似
            return (float) (REFERENCE_DISTANCE / Math.max(distance, MIN_DISTANCE));
        }
        var minecraft = Minecraft.getInstance();
        // 基准视野下，1 个方块在 1 格距离处对应的屏幕像素数
        var fov = Math.toRadians(Math.max(minecraft.options.fov().get(), 1.0)) * 0.5;
        var basePixelsPerBlock = minecraft.getWindow().getGuiScaledHeight() * 0.5 / Math.tan(fov);
        // 把「REFERENCE_DISTANCE 处 1 单位 = 1 像素」换算成每个单位对应多少个方块
        var blocksPerUnit = REFERENCE_DISTANCE / basePixelsPerBlock;
        return (float) (blocksPerUnit * pixelsPerBlock);
    }

    @Override
    public boolean visible() {
        return anchor != null;
    }

    @Override
    public double distance() {
        return anchor == null ? 0 : distance;
    }

    @Override
    public int width() {
        return Math.round(impl.guiWidth() / scale);
    }

    @Override
    public int height() {
        return Math.round(impl.guiHeight() / scale);
    }

    // 变换

    @Override
    public void push_pose() {
        transforms.push(new Matrix4f(currentTransform()));
    }

    @Override
    public void pop_pose() {
        // 锚点变换不属于用户变换，不允许被弹出
        if (transforms.size() > 1) {
            transforms.pop();
        }
    }

    @Override
    public void translate(float x, float y, float z) {
        currentTransform().translate(x, y, z);
    }

    @Override
    public void rotate(String axis, float degrees) {
        var quaternion = switch (axis.toLowerCase(Locale.ROOT)) {
            case "x" -> Axis.XP.rotationDegrees(degrees);
            case "y" -> Axis.YP.rotationDegrees(degrees);
            case "z" -> Axis.ZP.rotationDegrees(degrees);
            default -> throw new IllegalArgumentException("Invalid rotation axis: " + axis + " (expected x, y or z)");
        };
        currentTransform().rotate(quaternion);
    }

    @Override
    public void scale(float x, float y, float z) {
        currentTransform().scale(x, y, z);
    }

    // 矩形

    @Override
    public void fill(int min_x, int min_y, int max_x, int max_y, Color color) {
        draw(() -> impl.fill(min_x, min_y, max_x, max_y, color.getRGB()));
    }

    @Override
    public void fill_gradient(int x1, int y1, int x2, int y2, Color color_from, Color color_to) {
        draw(() -> impl.fillGradient(x1, y1, x2, y2, color_from.getRGB(), color_to.getRGB()));
    }

    @Override
    public void h_line(int min_x, int max_x, int y, Color color) {
        draw(() -> impl.hLine(min_x, max_x, y, color.getRGB()));
    }

    @Override
    public void v_line(int x, int min_y, int max_y, Color color) {
        draw(() -> impl.vLine(x, min_y, max_y, color.getRGB()));
    }

    @Override
    public void render_outline(int x, int y, int width, int height, Color color) {
        draw(() -> impl.renderOutline(x, y, width, height, color.getRGB()));
    }

    // 颜色

    @Override
    public void set_color(Color color) {
        var rgba = color.getRGBComponents(GET_COLOR_BUFFER.get());
        impl.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    // 文本

    @Override
    public int draw_string(String text, float x, float y, Color color, boolean drop_shadow) {
        if (anchor == null) {
            return 0;
        }
        pushTransform();
        try {
            return impl.drawString(font(), parseText(text), Mth.floor(x), Mth.floor(y), color.getRGB(), drop_shadow);
        } finally {
            popTransform();
        }
    }

    @Override
    public void draw_string_centered(String text, int x, int y, Color color) {
        draw(() -> impl.drawCenteredString(font(), parseText(text), x, y, color.getRGB()));
    }

    @Override
    public void draw_string_wrapped(String text, int x, int y, int line_width, Color color) {
        draw(() -> impl.drawWordWrap(font(), parseText(text), x, y, line_width, color.getRGB()));
    }

    // 贴图与物品

    @Override
    public void blit(String texture, int x, int y, int u, int v, int width, int height) {
        var location = parseTexture(texture);
        draw(() -> impl.blit(location, x, y, 0, u, v, width, height, 256, 256));
    }

    @Override
    public void blit(String texture, int x, int y, int width, int height, int z, float u, float v, float u_width, float v_height) {
        var location = parseTexture(texture);
        draw(() -> ((GuiGraphicsAccessor) impl).invokeInnerBlit(location, x, x + width, y, y + height, z, u, u + u_width, v, v + v_height));
    }

    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int slice_size, int u, int v, int source_width, int source_height
    ) {
        var location = parseTexture(texture);
        draw(() -> impl.blitNineSliced(location, x, y, width, height,
                slice_size, slice_size, slice_size, slice_size,
                source_width, source_height, u, v));
    }

    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int left_slice, int top_slice, int right_slice, int bottom_slice,
            int u, int v, int source_width, int source_height
    ) {
        var location = parseTexture(texture);
        draw(() -> impl.blitNineSliced(location, x, y, width, height,
                left_slice, top_slice, right_slice, bottom_slice,
                source_width, source_height, u, v));
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height
    ) {
        var location = parseTexture(texture);
        draw(() -> impl.blitRepeating(location, x, y, width, height, u, v, source_width, source_height));
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height,
            int texture_width, int texture_height
    ) {
        var location = parseTexture(texture);
        draw(() -> impl.blitRepeating(location, x, y, width, height, u, v, source_width, source_height, texture_width, texture_height));
    }

    @Override
    public void render_item(ItemStack stack, int x, int y) {
        draw(() -> impl.renderItem(stack, x, y));
    }

    @Override
    public void render_item_decorations(ItemStack stack, int x, int y) {
        draw(() -> impl.renderItemDecorations(font(), stack, x, y));
    }

    // 裁剪

    @Override
    public void enable_scissor(int min_x, int min_y, int max_x, int max_y) {
        if (anchor == null) {
            // 依旧要压入一次裁剪，保证脚本里的 enable/disable 成对，
            // 否则 disable 会弹掉别的 HUD 的裁剪区域。
            impl.enableScissor(0, 0, 0, 0);
            return;
        }
        var matrix = new Matrix4f(anchor).mul(currentTransform());
        var corner = new Vector3f();
        matrix.transformPosition(min_x, min_y, 0, corner);
        var screenMinX = corner.x;
        var screenMinY = corner.y;
        var screenMaxX = corner.x;
        var screenMaxY = corner.y;
        matrix.transformPosition(max_x, min_y, 0, corner);
        screenMinX = Math.min(screenMinX, corner.x);
        screenMinY = Math.min(screenMinY, corner.y);
        screenMaxX = Math.max(screenMaxX, corner.x);
        screenMaxY = Math.max(screenMaxY, corner.y);
        matrix.transformPosition(max_x, max_y, 0, corner);
        screenMinX = Math.min(screenMinX, corner.x);
        screenMinY = Math.min(screenMinY, corner.y);
        screenMaxX = Math.max(screenMaxX, corner.x);
        screenMaxY = Math.max(screenMaxY, corner.y);
        matrix.transformPosition(min_x, max_y, 0, corner);
        screenMinX = Math.min(screenMinX, corner.x);
        screenMinY = Math.min(screenMinY, corner.y);
        screenMaxX = Math.max(screenMaxX, corner.x);
        screenMaxY = Math.max(screenMaxY, corner.y);
        impl.enableScissor(
                Mth.floor(screenMinX), Mth.floor(screenMinY),
                Mth.ceil(screenMaxX), Mth.ceil(screenMaxY));
    }

    @Override
    public void disable_scissor() {
        impl.disableScissor();
    }

    // 内部实现

    /**
     * 在当前用户变换之上再压入锚点变换，并在绘制结束后弹出。
     */
    private void draw(Runnable action) {
        if (anchor == null) {
            return;
        }
        pushTransform();
        try {
            action.run();
        } finally {
            popTransform();
        }
    }

    private void pushTransform() {
        var anchor = Objects.requireNonNull(this.anchor, "anchor");
        var pose = impl.pose();
        pose.pushPose();
        pose.mulPoseMatrix(anchor);
        pose.mulPoseMatrix(currentTransform());
    }

    private void popTransform() {
        impl.pose().popPose();
    }

    private Matrix4f currentTransform() {
        return Objects.requireNonNull(transforms.peek(), "transform");
    }

    private static final ThreadLocal<float[]> GET_COLOR_BUFFER = ThreadLocal.withInitial(() -> new float[4]);

    private static Font font() {
        return Objects.requireNonNull(Minecraft.getInstance().font, "font");
    }
}
