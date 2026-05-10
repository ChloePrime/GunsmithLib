package mod.chloeprime.gunsmithlib.api.client.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ShooterStates;

/**
 * 只在客户端有效的射手状态。
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface ClientShootStates extends ShooterStates {
    /**
     * {@link #get_scope_type()} 在玩家使用基喵时返回的值
     */
    String SCOPE_TYPE_IRON_ZOOM = "IRON_ZOOM";

    /**
     * {@link #get_scope_type()} 在玩家使用战术瞄具时返回的值
     */
    String SCOPE_TYPE_SIGHT = "SIGHT";

    /**
     * {@link #get_scope_type()} 在玩家使用大瞄准镜时返回的值
     */
    String SCOPE_TYPE_SCOPE = "SCOPE";

    /**
     * 获取当前使用的瞄准镜类型。
     * <p>
     * 可能的值为：
     * <ul>
     * <li>IRON_ZOOM  机瞄</li>
     * <li>SIGHT      战术瞄准镜</li>
     * <li>SCOPE      大瞄准镜</li>
     * </ul>
     * <p>
     * 使用 sight 和 scope 混合的组合瞄具时，
     * 若选中放大倍率最低的一档，则返回 SIGHT，否则返回 SCOPE。
     *
     * @return 当前使用的瞄准镜类型
     * @since 6.1
     */
    String get_scope_type();
}
