package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.AirburstSystem;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.HomingProjectileBehavior;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.common.util.HurtFunction1;
import mod.chloeprime.gunsmithlib.common.util.SpecialHurtable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(value = EntityKineticBullet.class)
public abstract class MixinBullet extends Projectile implements EnhancedKineticBullet {
    private @Unique List<PotionEffectData> gunsmithlib$effects = List.of();
    private @Unique int gunsmithlib$aecDuration = 0;
    private @Unique float gunsmithlib$aecMinSize = 0;

    // 空爆系统

    @WrapMethod(method = "tick")
    private void tickAirburst(Operation<Void> original) {
        AirburstSystem.onBulletTick(this, original::call);
    }

    // 药水效果

    @Override
    @Accessor(remap = false) public abstract boolean isExplosion();

    @Override
    @Accessor(remap = false) public abstract float getExplosionRadius();

    @Unique @Override
    public List<PotionEffectData> gunsmithlib$getPotionEffects() {
        return gunsmithlib$effects;
    }

    @Unique @Override
    public void gunsmithlib$setPotionEffects(List<PotionEffectData> value) {
        gunsmithlib$effects = value;
    }

    @Unique @Override
    public int gunsmithlib$getPotionCloudDuration() {
        return gunsmithlib$aecDuration;
    }

    @Unique @Override
    public void gunsmithlib$setPotionCloudDuration(int value) {
        gunsmithlib$aecDuration = value;
    }

    @Override
    public float gunsmithlib$getPotionCloudMinSizeRate() {
        return gunsmithlib$aecMinSize;
    }

    @Override
    public void gunsmithlib$setPotionCloudMinSizeRate(float value) {
        gunsmithlib$aecMinSize = value;
    }

    // 跟踪弹

    @WrapOperation(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType$Builder;updateInterval(I)Lnet/minecraft/world/entity/EntityType$Builder;"))
    private static <T extends Entity> EntityType.Builder<T> adjustBulletUpdateIntervalToFixHomingBulletDrifting(
            EntityType.Builder<T> instance,
            int originalInterval,
            Operation<EntityType.Builder<T>> original
    ) {
        return original.call(instance, Math.min(originalInterval, 2));
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void tickHoming(CallbackInfo ci) {
        if (!isRemoved()) {
            HomingProjectileBehavior.onBulletTick(this);
        }
    }

    @WrapOperation(
            method = "tacAttackEntity", remap = false,
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean useSpecialHurtByTag(Entity victim, DamageSource source, float amount, Operation<Boolean> original) {
        if (!(victim instanceof SpecialHurtable injected)){
            return original.call(victim, source, amount);
        }
        HurtFunction1 method = injected.gunsmith$usingSpecialHurt()
                ? injected.getSpecialHurtFunction1()
                : ((source1, amount1) -> original.call(victim, source1, amount1));
        return method.invoke(source, amount);
    }

    public MixinBullet(EntityType<? extends Projectile> type, Level level) {
        super(type, level);
    }
}
