package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

/**
 * 自带的安全距离的用途 / 影响范围
 *
 * @since 6.2
 */
public final class SafetyDistanceFlags {
    /**
     * 阻止低于安全距离的空爆测距仪结果。
     */
    public static final SafetyDistanceFlag PREVENTS_AIRBURST_RANGEFINDING = new SafetyDistanceFlag("prevents_airburst_rangefindinng");

    /**
     * 子弹飞行距离低于安全距离时阻止近炸引信工作。
     */
    public static final SafetyDistanceFlag PREVENTS_PROXIMITY_FUSE = new SafetyDistanceFlag("prevents_proximity_fuse");

    /**
     * 子弹飞行距离低于安全距离时阻止子弹爆炸和产生破片。
     */
    public static final SafetyDistanceFlag PREVENTS_EXPLOSION = new SafetyDistanceFlag("prevents_explosion");

    private SafetyDistanceFlags() {
    }
}
