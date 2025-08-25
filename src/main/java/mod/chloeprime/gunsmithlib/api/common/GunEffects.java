package mod.chloeprime.gunsmithlib.api.common;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.world.effect.MobEffect;
import net.neoforged.neoforge.registries.DeferredHolder;

public class GunEffects {
    /**
     * 交叉火力（射击伤害提升）
     */
    public static final DeferredHolder<MobEffect, MobEffect> GUN_DAMAGE = GunsmithLib.MobEffects.GUN_DAMAGE;
}
