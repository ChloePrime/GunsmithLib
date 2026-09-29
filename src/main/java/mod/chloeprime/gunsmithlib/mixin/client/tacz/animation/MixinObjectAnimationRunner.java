package mod.chloeprime.gunsmithlib.mixin.client.tacz.animation;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.client.animation.ObjectAnimationRunner;
import mod.chloeprime.gunsmithlib.client.animation.AnimationSpeedScaler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ObjectAnimationRunner.class, remap = false)
public class MixinObjectAnimationRunner {
    private @Unique double gunsmithlib$reminder;

    @ModifyReturnValue(
            method = "elapsedSinceLastUpdate",
            at = @At(value = "RETURN"))
    private long timeScaler(long original) {
        var newDelta = original * AnimationSpeedScaler.getAnimationSpeedScale();
        gunsmithlib$reminder += newDelta - (long) newDelta;
        newDelta += (long) gunsmithlib$reminder;
        gunsmithlib$reminder -= (long) gunsmithlib$reminder;
        return (long) newDelta;
    }
}
