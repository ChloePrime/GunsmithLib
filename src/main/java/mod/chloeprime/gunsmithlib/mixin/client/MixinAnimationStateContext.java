package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import mod.chloeprime.gunsmithlib.client.gui.GunVariantSelectWheelScreen;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AnimationStateContext.class, remap = false)
public class MixinAnimationStateContext {
    @ModifyReturnValue(method = "shouldHideCrossHair", at = @At("RETURN"))
    private boolean hideCrosshairWhenSwitchingVariant(boolean original) {
        return original || Minecraft.getInstance().screen instanceof GunVariantSelectWheelScreen;
    }
}
