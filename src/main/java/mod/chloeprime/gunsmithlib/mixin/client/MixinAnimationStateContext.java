package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import mod.chloeprime.gunsmithlib.client.gui.GunVariantSelectWheelScreen;
import mod.chloeprime.gunsmithlib.client.internal.InanimatableContext;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AnimationStateContext.class, remap = false)
public class MixinAnimationStateContext {
    @ModifyReturnValue(method = "shouldHideCrossHair", at = @At("RETURN"))
    private boolean hideCrosshairWhenSwitchingVariant(boolean original) {
        return original || Minecraft.getInstance().screen instanceof GunVariantSelectWheelScreen;
    }

    @Inject(method = "checkStateMachine", at = @At("HEAD"))
    private void throwErrorForInanimatableContext(CallbackInfoReturnable<AnimationStateMachine<?>> cir) {
        if (this instanceof InanimatableContext) {
            throw new UnsupportedOperationException("This context does support animating");
        }
    }
}
