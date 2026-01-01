package mod.chloeprime.gunsmithlib.mixin.dfu;

import me.muksc.taczpackupgrader.Upgrader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

@Mixin(Upgrader.class)
public class MixinGunpackUpgrader {
    @Inject(method = "run", at = @At("HEAD"), cancellable = true)
    private void disableIntrinsicAutoUpdateBehavior(Path path, CallbackInfo ci) {
        ci.cancel();
    }
}
