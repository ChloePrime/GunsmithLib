package mod.chloeprime.gunsmithlib.api.client;

import mod.chloeprime.gunsmithlib.GunsmithLib;

/**
 * @since 3.7.0
 */
public class GunsmithLibAnimationConstant {
    /**
     * 武器进入（原版）冷却时触发。
     * 枪盾被破盾时会触发这个。
     * "gunsmithlib:cooldown_start"
     */
    public static final String GUNSMITHLIB_INPUT_COOLDOWN_START = GunsmithLib.loc("cooldown_start").toString();

    /**
     * 枪盾挡住非子弹伤害时触发
     * "gunsmithlib:shield_blocks_damage"
     *
     * @since 4.11.0
     */
    public static final String GUNSMITHLIB_INPUT_SHIELD_BLOCKS_DAMAGE = GunsmithLib.loc("shield_blocks_damage").toString();

    /**
     * 枪盾挡住子弹时触发
     * "gunsmithlib:shield_blocks_bullet"
     *
     * @since 4.11.0
     */
    public static final String GUNSMITHLIB_INPUT_SHIELD_BLOCKS_BULLET = GunsmithLib.loc("shield_blocks_bullet").toString();

    /**
     * 当切换枪械当前部件（下挂武器等）时触发
     * "gunsmithlib:current_part_switched"
     *
     * @since 4.12.0
     */
    public static final String GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED = GunsmithLib.loc("current_part_switched").toString();

    /**
     * 当切换 variant（弹种，模式）时触发。
     * 切换弹种后需要重新换弹的情况下不会触发。
     * "gunsmithlib:variant_switched"
     *
     * @since 4.12.0
     */
    public static final String GUNSMITHLIB_INPUT_VARIANT_SWITCHED = GunsmithLib.loc("variant_switched").toString();

    /**
     * 蓄力武器开始蓄力时触发。
     * 警告：即使不符合射击条件（例如膛内没有子弹）也会触发
     * "gunsmithlib:begin_charging"
     *
     * @since 4.13.0
     */
    public static final String GUNSMITHLIB_INPUT_BEGIN_CHARGING = GunsmithLib.loc("begin_charging").toString();
}
