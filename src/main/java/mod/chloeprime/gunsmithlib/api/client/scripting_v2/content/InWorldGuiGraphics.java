package mod.chloeprime.gunsmithlib.api.client.scripting_v2.content;

/**
 * 锚定在世界坐标上的 {@link GuiGraphicsAPI}。
 * <p>
 * 和普通的 {@link GuiGraphicsAPI} 不同，本接口的坐标系原点不是屏幕左上角，
 * 而是锚点世界坐标在屏幕上的投影位置：
 * <ul>
 * <li>原点为锚点的投影位置，{@code +x} 向右、{@code +y} 向下，其余语义与 {@link GuiGraphicsAPI} 一致；</li>
 * <li>每个 HUD 单位对应的屏幕像素由创建时指定的缩放决定，
 *     因此建议使用“以锚点为中心”的相对坐标（例如血条画在 {@code y = -4} 左右）。</li>
 * </ul>
 * 适合绘制“3D 动作游戏”风格的血条、锁定准星等跟随世界物体的 HUD。
 * <p>
 * 锚点位于相机背后或相机尚未初始化时 {@link #visible()} 为 {@code false}，
 * 此时所有绘制调用都不会产生任何效果，脚本可以据此跳过整个 HUD 的绘制。
 * <p>
 * 注意：{@link #width()}、{@link #height()} 返回的是可见区域在本对象坐标系下的尺寸
 * （即屏幕上的 GUI 像素数除以缩放），而不是屏幕像素数。
 * <p>
 * 通过 {@code context:gunsmithlib_extension():gui_graphics_in_world(...)} 获取实例。
 *
 * @since 7.0.0
 */
@SuppressWarnings("unused")
public interface InWorldGuiGraphics extends GuiGraphicsAPI {
    /**
     * 锚点当前是否可见。
     * <p>
     * 相机尚未初始化（例如刚进入世界、还没有渲染过一帧），
     * 或者锚点位于相机背后时返回 {@code false}。
     * <p>
     * 返回 {@code false} 时本对象的所有绘制方法都是空操作，不会抛出异常。
     *
     * @return 锚点当前是否可见
     */
    boolean visible();

    /**
     * 获取锚点到相机的距离，单位为方块。
     * <p>
     * 锚点不可见时返回 {@code 0}。
     *
     * @return 锚点到相机的距离
     */
    double distance();
}
