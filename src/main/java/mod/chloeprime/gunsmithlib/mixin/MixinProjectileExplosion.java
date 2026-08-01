package mod.chloeprime.gunsmithlib.mixin;

import com.tacz.guns.util.block.ProjectileExplosion;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.damage_source_control.DamageSourceControlSystem;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileExplosion.class)
public class MixinProjectileExplosion extends Explosion {
    public MixinProjectileExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction) {
        super(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction);
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDamageSourceTags(
            Level level, Entity owner, Entity exploder,
            DamageSource source, ExplosionDamageCalculator damageCalculator,
            double x, double y, double z, float power, float radius, boolean knockback,
            Explosion.BlockInteraction mode, CallbackInfo ci
    ) {
        DamageSourceControlSystem.injectExplosionDamageSource(exploder, getDamageSource());
    }
}
