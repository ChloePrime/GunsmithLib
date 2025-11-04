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
}
