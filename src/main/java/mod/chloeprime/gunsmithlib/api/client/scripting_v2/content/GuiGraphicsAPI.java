package mod.chloeprime.gunsmithlib.api.client.scripting_v2.content;

import com.mojang.math.Axis;
import mod.chloeprime.gunsmithlib.mixin.client.minecraft.GuiGraphicsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

/**
 * 对原版 {@link GuiGraphics} 的封装，供客户端脚本在 HUD 渲染期间绘制内容。
 * <p>
 * 通过状态机 context 的 {@code gui_graphics()} 方法获取实例。
 * 每次调用都会创建一个新的 {@link GuiGraphics}（使用全新的 {@code PoseStack}），
 * 不会受到当前 HUD 渲染状态的影响。
 * <p>
 * 返回的对象只在当前回调期间有效，请不要将其保存到全局变量中跨帧使用。
 * 建议在 HUD 渲染期间（例如 {@code gunsmithlib_modify_display_property} 回调中）使用。
 * <p>
 * 颜色参数均为 {@link Color} 对象。
 * 在 Lua 中可以使用 {@code gunsmithlib.new_color(r, g, b)} 或
 * {@code gunsmithlib.new_color(r, g, b, a)} 创建，分量范围可以是 0-255 或 0-1。
 * <p>
 * 所有文本参数都会通过 {@link Component#translatable(String)} 解析：
 * 没有对应翻译资源时会原样显示传入的文本，因此既可以直接传字面文本，也可以传翻译键。
 *
 * @since 7.0.0
 */
@SuppressWarnings("unused")
public class GuiGraphicsAPI {
    private final GuiGraphics impl;

    /**
     * 内部构造方法，请通过状态机 context 的 {@code gui_graphics()} 获取实例。
     *
     * @param impl 被封装的原版 {@link GuiGraphics}
     */
    @ApiStatus.Internal
    public GuiGraphicsAPI(GuiGraphics impl) {
        this.impl = Objects.requireNonNull(impl, "impl");
    }

    /**
     * 获取当前 GUI 的缩放后宽度。
     *
     * @return 当前 GUI 的缩放后宽度
     */
    public int width() {
        return impl.guiWidth();
    }

    /**
     * 获取当前 GUI 的缩放后高度。
     *
     * @return 当前 GUI 的缩放后高度
     */
    public int height() {
        return impl.guiHeight();
    }

    // 变换

    /**
     * 压入一次矩阵变换。
     * <p>
     * 必须和 {@link #pop_pose()} 成对使用，否则会影响后续的 HUD 渲染。
     */
    public void push_pose() {
        impl.pose().pushPose();
    }

    /**
     * 弹出最近一次压入的矩阵变换。
     *
     * @see #push_pose()
     */
    public void pop_pose() {
        impl.pose().popPose();
    }

    /**
     * 平移坐标系。
     *
     * @param x x 轴平移量
     * @param y y 轴平移量
     * @param z z 轴平移量，HUD 渲染中通常填 {@code 0}
     */
    public void translate(float x, float y, float z) {
        impl.pose().translate(x, y, z);
    }

    /**
     * 绕指定坐标轴旋转坐标系。
     *
     * @param axis    旋转轴，可选 {@code "x"}、{@code "y"}、{@code "z"}
     * @param degrees 旋转角度，单位为角度
     */
    public void rotate(String axis, float degrees) {
        var quaternion = switch (axis.toLowerCase(Locale.ROOT)) {
            case "x" -> Axis.XP.rotationDegrees(degrees);
            case "y" -> Axis.YP.rotationDegrees(degrees);
            case "z" -> Axis.ZP.rotationDegrees(degrees);
            default -> throw new IllegalArgumentException("Invalid rotation axis: " + axis + " (expected x, y or z)");
        };
        impl.pose().mulPose(quaternion);
    }

    /**
     * 缩放坐标系。
     *
     * @param x x 轴缩放比例
     * @param y y 轴缩放比例
     * @param z z 轴缩放比例，HUD 渲染中通常填 {@code 1}
     */
    public void scale(float x, float y, float z) {
        impl.pose().scale(x, y, z);
    }

    // 矩形

    /**
     * 绘制一个实心矩形。
     *
     * @param min_x 左上角 x 坐标
     * @param min_y 左上角 y 坐标
     * @param max_x 右下角 x 坐标
     * @param max_y 右下角 y 坐标
     * @param color 颜色，{@link Color} 对象
     */
    public void fill(int min_x, int min_y, int max_x, int max_y, Color color) {
        impl.fill(min_x, min_y, max_x, max_y, color.getRGB());
    }

    /**
     * 绘制一个渐变色矩形。
     *
     * @param x1         第一个角的 x 坐标
     * @param y1         第一个角的 y 坐标
     * @param x2         第二个角的 x 坐标
     * @param y2         第二个角的 y 坐标
     * @param color_from 起始颜色，{@link Color} 对象
     * @param color_to   结束颜色，{@link Color} 对象
     */
    public void fill_gradient(int x1, int y1, int x2, int y2, Color color_from, Color color_to) {
        impl.fillGradient(x1, y1, x2, y2, color_from.getRGB(), color_to.getRGB());
    }

    /**
     * 绘制一条水平线。
     *
     * @param min_x 起点 x 坐标
     * @param max_x 终点 x 坐标
     * @param y     线的 y 坐标
     * @param color 颜色，{@link Color} 对象
     */
    public void h_line(int min_x, int max_x, int y, Color color) {
        impl.hLine(min_x, max_x, y, color.getRGB());
    }

    /**
     * 绘制一条竖直线。
     *
     * @param x     线的 x 坐标
     * @param min_y 起点 y 坐标
     * @param max_y 终点 y 坐标
     * @param color 颜色，{@link Color} 对象
     */
    public void v_line(int x, int min_y, int max_y, Color color) {
        impl.vLine(x, min_y, max_y, color.getRGB());
    }

    /**
     * 绘制一个空心矩形（边框）。
     *
     * @param x     左上角 x 坐标
     * @param y     左上角 y 坐标
     * @param width 宽度
     * @param height 高度
     * @param color 颜色，{@link Color} 对象
     */
    public void render_outline(int x, int y, int width, int height, Color color) {
        impl.renderOutline(x, y, width, height, color.getRGB());
    }

    // 颜色

    /**
     * 设置后续渲染使用的颜色。
     * <p>
     * 主要影响贴图绘制，绘制完后记得调用 {@code set_color(gunsmithlib.new_color(255, 255, 255))} 还原。
     *
     * @param color 颜色，{@link Color} 对象
     */
    public void set_color(Color color) {
        var rgba = color.getRGBComponents(GET_COLOR_BUFFER.get());
        impl.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    // 文本

    /**
     * 绘制一行文本。
     * <p>
     * 文本会作为翻译键解析；{@code x}、{@code y} 会向下取整
     * （接受 {@link Component} 的重载只支持整数坐标）。
     *
     * @param text        要绘制的文本（翻译键）
     * @param x           文本左端 x 坐标
     * @param y           文本顶端 y 坐标
     * @param color       文本颜色，{@link Color} 对象
     * @param drop_shadow 是否绘制阴影
     * @return 绘制出的文本宽度
     */
    public int draw_string(String text, float x, float y, Color color, boolean drop_shadow) {
        return impl.drawString(font(), Component.translatable(text), Mth.floor(x), Mth.floor(y), color.getRGB(), drop_shadow);
    }

    /**
     * 绘制一行居中文本。
     * <p>
     * 文本会作为翻译键解析。
     *
     * @param text  要绘制的文本（翻译键）
     * @param x     文本中心 x 坐标
     * @param y     文本顶端 y 坐标
     * @param color 文本颜色，{@link Color} 对象
     */
    public void draw_string_centered(String text, int x, int y, Color color) {
        impl.drawCenteredString(font(), Component.translatable(text), x, y, color.getRGB());
    }

    /**
     * 绘制自动换行的文本。
     * <p>
     * 文本会作为翻译键解析，例如 {@code "gunsmithlib.hud.some_text"}。
     *
     * @param text       要绘制的文本（翻译键）
     * @param x          文本左端 x 坐标
     * @param y          文本顶端 y 坐标
     * @param line_width 每行的最大宽度
     * @param color      文本颜色，{@link Color} 对象
     */
    public void draw_string_wrapped(String text, int x, int y, int line_width, Color color) {
        impl.drawWordWrap(font(), Component.translatable(text), x, y, line_width, color.getRGB());
    }

    // 贴图与物品

    /**
     * 绘制一张贴图。
     * <p>
     * 贴图尺寸按原版的 256x256 处理，且绘制尺寸和贴图取样尺寸相同。
     * 需要更精细的控制时请使用另一个重载。
     *
     * @param texture 贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x       左上角 x 坐标
     * @param y       左上角 y 坐标
     * @param u       贴图取样起点 x 坐标
     * @param v       贴图取样起点 y 坐标
     * @param width   绘制宽度（同时也是取样宽度）
     * @param height  绘制高度（同时也是取样高度）
     */
    public void blit(String texture, int x, int y, int u, int v, int width, int height) {
        impl.blit(parseTexture(texture), x, y, 0, u, v, width, height, 256, 256);
    }

    /**
     * 绘制一张贴图，直接指定 UV 坐标和渲染层级。
     * <p>
     * 和另一个重载不同，本方法的 {@code u}、{@code v}、{@code u_width}、{@code v_height}
     * 是 0-1 的 UV 坐标，而不是像素坐标。
     *
     * @param texture  贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x        左上角 x 坐标
     * @param y        左上角 y 坐标
     * @param width    绘制宽度
     * @param height   绘制高度
     * @param z        渲染层级（z 偏移），HUD 渲染中通常填 {@code 0}
     * @param u        UV 起点 x 坐标，范围为 0-1
     * @param v        UV 起点 y 坐标，范围为 0-1
     * @param u_width  UV 宽度，范围为 0-1
     * @param v_height UV 高度，范围为 0-1
     */
    public void blit(String texture, int x, int y, int width, int height, int z, float u, float v, float u_width, float v_height) {
        ((GuiGraphicsAccessor) impl).invokeInnerBlit(parseTexture(texture), x, x + width, y, y + height, z, u, u + u_width, v, v + v_height);
    }

    /**
     * 绘制九宫格拉伸的贴图。
     * 四边切片大小相同，贴图尺寸按原版的 256x256 处理。
     *
     * @param texture       贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         绘制宽度
     * @param height        绘制高度
     * @param slice_size    四边切片大小
     * @param u             源区域左上角 x 坐标，单位为像素
     * @param v             源区域左上角 y 坐标，单位为像素
     * @param source_width  源区域宽度，单位为像素
     * @param source_height 源区域高度，单位为像素
     */
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int slice_size, int u, int v, int source_width, int source_height
    ) {
        impl.blitNineSliced(parseTexture(texture), x, y, width, height,
                slice_size, slice_size, slice_size, slice_size,
                source_width, source_height, u, v);
    }

    /**
     * 绘制九宫格拉伸的贴图。
     * 可分别指定四边切片大小，贴图尺寸按原版的 256x256 处理。
     *
     * @param texture       贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         绘制宽度
     * @param height        绘制高度
     * @param left_slice    左侧切片宽度
     * @param top_slice     顶部切片高度
     * @param right_slice   右侧切片宽度
     * @param bottom_slice  底部切片高度
     * @param u             源区域左上角 x 坐标，单位为像素
     * @param v             源区域左上角 y 坐标，单位为像素
     * @param source_width  源区域宽度，单位为像素
     * @param source_height 源区域高度，单位为像素
     */
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int left_slice, int top_slice, int right_slice, int bottom_slice,
            int u, int v, int source_width, int source_height
    ) {
        impl.blitNineSliced(parseTexture(texture), x, y, width, height,
                left_slice, top_slice, right_slice, bottom_slice,
                source_width, source_height, u, v);
    }

    /**
     * 绘制重复平铺的贴图。
     * 贴图尺寸按原版的 256x256 处理。
     *
     * @param texture       贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         绘制宽度
     * @param height        绘制高度
     * @param u             源区域左上角 x 坐标，单位为像素
     * @param v             源区域左上角 y 坐标，单位为像素
     * @param source_width  源区域宽度，单位为像素
     * @param source_height 源区域高度，单位为像素
     */
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height
    ) {
        impl.blitRepeating(parseTexture(texture), x, y, width, height, u, v, source_width, source_height);
    }

    /**
     * 绘制重复平铺的贴图。
     *
     * @param texture       贴图 id，例如 {@code "minecraft:textures/gui/widgets.png"}
     * @param x             左上角 x 坐标
     * @param y             左上角 y 坐标
     * @param width         绘制宽度
     * @param height        绘制高度
     * @param u             源区域左上角 x 坐标，单位为像素
     * @param v             源区域左上角 y 坐标，单位为像素
     * @param source_width  源区域宽度，单位为像素
     * @param source_height 源区域高度，单位为像素
     * @param texture_width 贴图总宽度，单位为像素
     * @param texture_height 贴图总高度，单位为像素
     */
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height,
            int texture_width, int texture_height
    ) {
        impl.blitRepeating(parseTexture(texture), x, y, width, height, u, v, source_width, source_height, texture_width, texture_height);
    }

    /**
     * 绘制一个物品。
     *
     * @param stack 要绘制的物品
     * @param x     左上角 x 坐标
     * @param y     左上角 y 坐标
     */
    public void render_item(ItemStack stack, int x, int y) {
        impl.renderItem(stack, x, y);
    }

    /**
     * 绘制物品的角标（数量、耐久条等）。
     *
     * @param stack 要绘制角标的物品
     * @param x     左上角 x 坐标
     * @param y     左上角 y 坐标
     */
    public void render_item_decorations(ItemStack stack, int x, int y) {
        impl.renderItemDecorations(font(), stack, x, y);
    }

    // 裁剪

    /**
     * 开启矩形裁剪区域。
     * <p>
     * 必须和 {@link #disable_scissor()} 成对使用。
     *
     * @param min_x 裁剪区域左上角 x 坐标
     * @param min_y 裁剪区域左上角 y 坐标
     * @param max_x 裁剪区域右下角 x 坐标
     * @param max_y 裁剪区域右下角 y 坐标
     */
    public void enable_scissor(int min_x, int min_y, int max_x, int max_y) {
        impl.enableScissor(min_x, min_y, max_x, max_y);
    }

    /**
     * 关闭最近一次开启的裁剪区域。
     *
     * @see #enable_scissor(int, int, int, int)
     */
    public void disable_scissor() {
        impl.disableScissor();
    }


    private static final ThreadLocal<float[]> GET_COLOR_BUFFER = ThreadLocal.withInitial(() -> new float[4]);

    private static Font font() {
        return Objects.requireNonNull(Minecraft.getInstance().font, "font");
    }

    private static ResourceLocation parseTexture(String id) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid texture id: " + id);
        }
        return location;
    }
}
