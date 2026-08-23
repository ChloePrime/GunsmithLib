package mod.chloeprime.gunsmithlib.mixin.client.tacz.animation;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import mod.chloeprime.gunsmithlib.client.animation.AnimationSpeedScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public class MixinGunAnimationStateContext {
    private final @Unique AnimationSpeedScaler.TimeTracker gunsmithlib$timeTracker = AnimationSpeedScaler.TimeTracker.createMillisTracker();

    @ModifyReturnValue(method = "getCurrentTimestamp", at = @At("RETURN"))
    private long timeScaler(long original) {
        return gunsmithlib$timeTracker.updateAndGet(original, AnimationSpeedScaler.getAnimationSpeedScale());
    }
}
