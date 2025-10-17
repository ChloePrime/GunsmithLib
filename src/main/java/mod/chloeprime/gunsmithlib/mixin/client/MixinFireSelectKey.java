package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.client.input.FireSelectKey;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.AirburstSelectInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = FireSelectKey.class, remap = false)
public class MixinFireSelectKey {
    @Inject(method = "doFireSelectLogic", at = @At("HEAD"), cancellable = true)
    private static void selectAirburstIndex(CallbackInfo ci) {
        AirburstSelectInput.onFireSelectInput(ci::cancel);
    }
}
