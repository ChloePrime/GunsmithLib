package mod.chloeprime.gunsmithlib.mixin.client.tacz.animation;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import mod.chloeprime.gunsmithlib.client.animation.AnimationSpeedScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ObjectAnimationRunner.class, remap = false)
public class MixinObjectAnimationRunner {
    private final @Unique AnimationSpeedScaler.TimeTracker gunsmithlib$timeTacker = AnimationSpeedScaler.TimeTracker.createNanosTracker();

    @ModifyExpressionValue(
            method = {"update", "updateSoundOnly"},
            at = @At(value = "INVOKE", target = "Ljava/lang/System;nanoTime()J"))
    private long timeScaler(long original) {
        return gunsmithlib$timeTacker.updateAndGet(original, AnimationSpeedScaler.getAnimationSpeedScale());
    }
}
