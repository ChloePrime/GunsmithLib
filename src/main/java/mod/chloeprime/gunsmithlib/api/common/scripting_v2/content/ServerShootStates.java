package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

/**
 * 只在服务端有效的射手状态。
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface ServerShootStates extends ShooterStates {
    /**
     * 服务端，该操作者是否受弹药数影响。
     * 如果为 false，那么开火时不会检查弹药，无论是玩家背包内还是枪械内的。
     *
     * @return 该操作者是否受弹药数影响
     */
    boolean shoot_needs_ammo();

    /**
     * 服务端，开火是否消耗弹药
     *
     * @return 如果为 false，那么开火不会消耗枪械弹药
     */
    boolean shoot_consumes_ammo();
}
