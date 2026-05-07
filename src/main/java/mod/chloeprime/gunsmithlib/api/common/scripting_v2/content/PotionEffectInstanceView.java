package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

/**
 * 药水效果实例。
 *
 * @since 6.0.0
 */
public interface PotionEffectInstanceView {
    /**
     * 获取药水效果实例的剩余持续时间，单位为刻。
     *
     * @return 药水效果实例的剩余持续时间，单位为刻
     */
    int duration_ticks();

    /**
     * 获取药水效果实例的剩余持续时间，单位为秒。
     *
     * @return 药水效果实例的剩余持续时间，单位为秒
     */
    default float duration_seconds() {
        return duration_ticks() / 20F;
    }

    /**
     * 获取药水效果实例等级 - 1
     *
     * @return 药水效果实例等级 - 1
     */
    int amplifier();

    /**
     * 获取药水效果实例等级
     *
     * @return 药水效果实例等级
     */
    default int level() {
        return amplifier() + 1;
    }

    /**
     * 获取药水效果实例是否为环境效果。
     * 环境效果在原版由信标给予。环境效果的粒子透明度会变淡。
     *
     * @return 药水效果实例等级
     */
    boolean is_ambient();

    /**
     * 获取药水效果实例是否显示粒子。
     *
     * @return 如果为 false，那么药水效果的粒子将完全不显示
     */
    boolean is_visible();

    /**
     * 获取药水效果实例是否在玩家 HUD 中显示。
     *
     * @return 如果为 false，那么药水效果将不会在玩家的 HUD 中显示。
     */
    boolean shows_icon();
}
