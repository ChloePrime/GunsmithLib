package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldBehavior;
import mod.chloeprime.gunsmithlib.common.internal.GunAttributeSyncState;
import mod.chloeprime.gunsmithlib.common.internal.MobEffectForceApplicable;
import mod.chloeprime.gunsmithlib.common.util.FloatConsumer;
import mod.chloeprime.gunsmithlib.common.util.HurtFunction1;
import mod.chloeprime.gunsmithlib.common.util.HurtFunction2;
import mod.chloeprime.gunsmithlib.common.util.SpecialHurtable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(LivingEntity.class)
public abstract class MixinLivingEntity implements
        SpecialHurtable,
        GunAttributeSyncState,
        MobEffectForceApplicable {

    // 枪盾
    @Inject(
            method = "isDamageSourceBlocked",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isBlocking()Z"),
            cancellable = true)
    private void gunShield(DamageSource source, CallbackInfoReturnable<Boolean> cir) {
        var self = (LivingEntity) (Object) this;
        var sourcePos = ShieldBehavior.getBetterSourcePosition(source);
        if (sourcePos == null) {
            return;
        }
        ItemStack weapon = self.getMainHandItem();
        if (ShieldBehavior.canBlockVanillaDamage(self, sourcePos, weapon)) {
            cir.setReturnValue(true);
        } else if (IGun.getIGunOrNull(weapon) != null) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "isBlocking", at = @At("HEAD"), cancellable = true)
    private void gunShield(CallbackInfoReturnable<Boolean> cir) {
        var self = (LivingEntity) (Object) this;
        var testSourcePos = self.getEyePosition().add(self.getLookAngle());
        if (ShieldBehavior.canBlockVanillaDamage(self, testSourcePos, self.getMainHandItem())) {
            cir.setReturnValue(true);
        }
    }

    // forceAddEffect Prime

    @Override
    public void gunsmith$forceAddEffectPrime(MobEffectInstance effect, @Nullable Entity cause) {
        var existing = this.activeEffects.get(effect.getEffect());
        var addedEvent = new MobEffectEvent.Added((LivingEntity) (Object) this, existing, effect, cause);
        MinecraftForge.EVENT_BUS.post(addedEvent);

        if (existing == null) {
            this.activeEffects.put(effect.getEffect(), effect);
            this.onEffectAdded(effect, cause);
        } else if (existing.update(effect)) {
            this.onEffectUpdated(existing, true, cause);
        }
    }

    @Shadow @Final private Map<MobEffect, MobEffectInstance> activeEffects;
    @Shadow protected abstract void onEffectUpdated(MobEffectInstance pEffectInstance, boolean pForced, @Nullable Entity pEntity);
    @Shadow protected abstract void onEffectAdded(MobEffectInstance pEffectInstance, @Nullable Entity pEntity);

    // 特殊伤害机制
    @Shadow public abstract void setHealth(float value);

    private @Unique boolean gunsmith$isInGunAttributeMode;
    private @Unique boolean gunsmith$isDoingSpecialHurtProcedure;

    @WrapOperation(
            method = "hurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;actuallyHurt(Lnet/minecraft/world/damagesource/DamageSource;F)V"))
    private void useSpecialActuallyHurtWhenNeeded(LivingEntity entity, DamageSource source, float amount, Operation<Void> original) {
        var injected = (SpecialHurtable) entity;
        HurtFunction2 method = gunsmith$isDoingSpecialHurtProcedure
                ? injected.getSpecialHurtFunction2()
                : ((source1, amount1) -> original.call(entity, source1, amount1));
        method.invoke(source, amount);
    }

    @WrapOperation(
            method = "actuallyHurt",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;setHealth(F)V"))
    private void useSpecialSetHealthWhenNeeded(LivingEntity entity, float value, Operation<Void> original) {
        var injected = (SpecialHurtable) entity;
        FloatConsumer method = gunsmith$isDoingSpecialHurtProcedure
                ? injected.getSpecialSetHealthFunction()
                : ((v) -> original.call(entity, v));
        method.accept(value);
    }

    @Override
    public boolean gunsmith$usingSpecialHurt() {
        return false;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public HurtFunction1 getSpecialHurtFunction1() {
        return this::hurt;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public HurtFunction2 getSpecialHurtFunction2() {
        return this::actuallyHurt;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public FloatConsumer getSpecialSetHealthFunction() {
        return this::setHealth;
    }

    @Override
    public void gunsmith$beginSpecialHurt() {
        gunsmith$isDoingSpecialHurtProcedure = true;
    }

    @Override
    public void gunsmith$endSpecialHurt() {
        gunsmith$isDoingSpecialHurtProcedure = false;
    }

    @Override
    public boolean gunsmith$isInGunMode() {
        return gunsmith$isInGunAttributeMode;
    }

    @Override
    public void gunsmith$setInGunMode(boolean value) {
        gunsmith$isInGunAttributeMode = value;
    }

    @Shadow public abstract boolean hurt(@NotNull DamageSource p_21016_, float p_21017_);
    @Shadow protected abstract void actuallyHurt(DamageSource p_21240_, float p_21241_);
}
