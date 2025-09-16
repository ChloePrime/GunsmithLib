package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.expression.Definition;
import com.llamalad7.mixinextras.expression.Expression;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import mod.chloeprime.gunsmithlib.common.entity.AreaEffectCloud3D;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(AreaEffectCloud.class)
public abstract class MixinAreaEffectCloud extends Entity {
    @Inject(
            method = "tick", cancellable = true,
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lnet/minecraft/world/entity/Entity;tick()V"))
    @SuppressWarnings("ConstantValue")
    private void doNotSpawn2DParticlesOn3DCloud(CallbackInfo ci) {
        if ((level().isClientSide) && (Object) this instanceof AreaEffectCloud3D) {
            ci.cancel();
        }
    }

    private @Unique LivingEntity gunsmithlib$capturedVictim;

    @WrapOperation(
            method = "tick",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/LivingEntity;isAffectedByPotions()Z"))
    private boolean captureCurrentVictim(LivingEntity victim, Operation<Boolean> original) {
        gunsmithlib$capturedVictim = victim;
        return original.call(victim);
    }

    @Inject(method = "tick", at = @At("RETURN"))
    private void clearCapturedCurrentVictimRef(CallbackInfo ci) {
        gunsmithlib$capturedVictim = null;
    }

    @Definition(id = "getX2", method = "Lnet/minecraft/world/entity/AreaEffectCloud;getX()D")
    @Definition(id = "getX1", method = "Lnet/minecraft/world/entity/LivingEntity;getX()D")
    @Definition(id = "livingentity", local = @Local(type = LivingEntity.class))
    @Expression("livingentity.getX1() - this.getX2()")
    @ModifyExpressionValue(method = "tick", at = @At("MIXINEXTRAS:EXPRESSION"))
    private double makeRange3D(double dx) {
        if ((Object) this instanceof AreaEffectCloud3D) {
            var victim = Objects.requireNonNull(gunsmithlib$capturedVictim);
            var dy = victim.getY() - (this.getY() + this.getBbHeight() / 2);
            return Math.sqrt(dx * dx + dy * dy);
        } else {
            return dx;
        }
    }

    public MixinAreaEffectCloud(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}
