package mod.chloeprime.gunsmithlib.client.util;

import mod.chloeprime.gunsmithlib.common.util.LinearAlgebraTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;
import org.joml.Vector2f;
import org.joml.Vector3d;
import org.joml.Vector4f;

/**
 * 把世界坐标投影为 HUD 屏幕坐标。
 * <p>
 * HUD 渲染时 {@code RenderSystem} 里的投影矩阵已经是 GUI 的正交投影，
 * 无法用来投影世界坐标，因此这里在关卡渲染期间缓存一份世界渲染实际使用的
 * 视图投影矩阵，供 HUD 渲染期间（例如 {@code gunsmithlib_render_hud} 回调）使用。
 * <p>
 * 缓存的矩阵与当前帧 HUD 下方显示的画面完全一致，
 * 因此瞄准缩放、疾跑等造成的 FOV 变化、视角摇晃、反胃效果都能被正确处理。
 *
 * @since 7.0.0
 */
@EventBusSubscriber(Dist.CLIENT)
public final class WorldToScreenProjection {
    private static final Matrix4f VIEW_PROJECTION = new Matrix4f();
    private static final Vector3d CAMERA_POS = new Vector3d();
    private static boolean available = false;

    /**
     * 缓存关卡渲染实际使用的视图投影矩阵。
     * <p>
     * 视图矩阵（相机朝向）与投影矩阵分别在事件的 {@code getModelViewMatrix()} 和
     * {@code getProjectionMatrix()} 里，两者相乘就是关卡渲染世界坐标时实际使用的变换，
     * 因此瞄准缩放、疾跑/水下造成的 FOV 变化、视角摇晃、反胃效果都能被正确处理。
     * <p>
     * <b>不能像 1.20.1 那样用 {@code getPoseStack().last().pose()} 当视图矩阵</b>：
     * 1.21.1 起 {@code LevelRenderer} 把视图矩阵放进了 {@code RenderSystem} 的 model view stack，
     * 派发事件时传入的 PoseStack 只是实体渲染用的临时栈，此刻已经被清空（单位矩阵）。
     * 把它当视图矩阵会丢掉相机朝向，{@link #worldToScreen(Vec3)} 算出来的屏幕坐标就会偏。
     */
    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        VIEW_PROJECTION.set(event.getProjectionMatrix()).mul(event.getModelViewMatrix());
        CAMERA_POS.set(LinearAlgebraTypes.moj2joml(event.getCamera().getPosition()));
        available = true;
    }

    /**
     * 将世界坐标转换为 GUI 缩放后的屏幕坐标，与 {@code GuiGraphics} 使用的坐标系一致。
     *
     * @param worldPos 世界坐标
     * @return 屏幕坐标；相机尚未初始化，或该坐标位于相机背后时返回 {@code null}
     */
    public static @Nullable Vector2f worldToScreen(Vec3 worldPos) {
        if (!available) {
            return null;
        }
        var window = Minecraft.getInstance().getWindow();
        var width = window.getGuiScaledWidth();
        var height = window.getGuiScaledHeight();
        if (width <= 0 || height <= 0) {
            return null;
        }
        var clip = new Vector4f(
                (float) (worldPos.x() - CAMERA_POS.x),
                (float) (worldPos.y() - CAMERA_POS.y),
                (float) (worldPos.z() - CAMERA_POS.z),
                1.0F);
        VIEW_PROJECTION.transform(clip);
        // w <= 0 说明该点在相机背后（或与相机平面重合），此时的投影结果没有意义
        if (!(clip.w > 1.0E-6F)) {
            return null;
        }
        var ndcX = clip.x / clip.w;
        var ndcY = clip.y / clip.w;
        return new Vector2f(
                (ndcX * 0.5F + 0.5F) * width,
                (0.5F - ndcY * 0.5F) * height);
    }

    /**
     * 计算世界坐标处 1 个方块在屏幕上对应的像素数。
     * <p>
     * 沿相机的上方向量取样，所以结果包含了当前投影矩阵的全部效果：
     * FOV 设置、瞄准/开镜造成的视野放大、疾跑与水下等造成的 FOV 变化。
     * 需要让 HUD 的视觉尺寸在世界中保持恒定时，应该用它而不是用距离去近似，
     * 否则开镜放大后 HUD 会相对世界变小。
     *
     * @param worldPos 世界坐标
     * @return 1 个方块对应的屏幕像素数；相机尚未初始化，或该坐标位于相机背后时返回 {@code 0}
     */
    public static float screenPixelsPerBlock(Vec3 worldPos) {
        var origin = worldToScreen(worldPos);
        if (origin == null) {
            return 0;
        }
        var up = Minecraft.getInstance().gameRenderer.getMainCamera().getUpVector();
        var shifted = new Vec3(
                worldPos.x + up.x(),
                worldPos.y + up.y(),
                worldPos.z + up.z());
        var moved = worldToScreen(shifted);
        if (moved == null) {
            return 0;
        }
        return origin.distance(moved);
    }

    private WorldToScreenProjection() {
    }
}
