package mod.chloeprime.gunsmithlib.common.internal;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public interface MobEffectForceApplicable {
    void gunsmith$forceAddEffectPrime(MobEffectInstance effect, @Nullable Entity cause);
}
