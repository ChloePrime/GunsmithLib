package mod.chloeprime.gunsmithlib.mixin.client.tacz;

import com.tacz.guns.client.input.FireSelectKey;
import com.tacz.guns.util.InputExtraCheck;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.AirburstSelectInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = FireSelectKey.class, remap = false)
public class MixinFireSelectKey {
    @Inject(method = "onFireSelectPress", at = @At("HEAD"), cancellable = true)
    private static void selectAirburstIndex(CallbackInfoReturnable<Boolean> cir) {
        if (!InputExtraCheck.isInGame()) {
            return;
        }
        AirburstSelectInput.onFireSelectInput(() -> cir.setReturnValue(true));
    }
}
