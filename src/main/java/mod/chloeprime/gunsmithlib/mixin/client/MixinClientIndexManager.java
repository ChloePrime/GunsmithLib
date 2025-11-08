package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.client.resource.ClientIndexManager;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ClientIndexManager.class, remap = false)
public class MixinClientIndexManager {
    @Inject(method = "loadGunIndex", at = @At("RETURN"))
    private static void refreshDisplayInstanceRedirectionData(CallbackInfo ci) {
        GunsmithLibClient.applyDisplayRedirectionData();
    }
}
