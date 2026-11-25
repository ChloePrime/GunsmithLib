package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability;

import cn.chloeprime.commons.lang4.StringName;
import mod.chloeprime.gunsmithlib.GunsmithLib;

@SuppressWarnings("unused")
public final class BuiltinDurabilityDamageCases {
    /**
     * 扣下扳机时消耗耐久
     */
    public static final StringName SHOOT = StringName.of(GunsmithLib.loc("shoot").toString());

    /**
     * 击发时消耗耐久
     */
    public static final StringName FIRE = StringName.of(GunsmithLib.loc("fire").toString());
}
