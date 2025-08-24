package mod.chloeprime.gunsmithlib.mixin.client;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(GunDisplayInstance.class)
public class MixinGunDisplayInstance implements EnhancedGunDisplay {
    @Inject(method = "<init>", at = @At("TAIL"))
    private void copyExtensionInfo(GunDisplay display, CallbackInfo ci) {
        gunsmith$extension = ((EnhancedGunDisplay) display).gunsmith$getGunsmithLibExtension().orElse(null);
    }

    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique @Nullable GunsmithLibGunDisplayExtension gunsmith$extension;

    @Override
    public Optional<GunsmithLibGunDisplayExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }
}
