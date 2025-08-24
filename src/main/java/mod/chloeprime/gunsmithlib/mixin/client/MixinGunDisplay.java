package mod.chloeprime.gunsmithlib.mixin.client;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(GunDisplay.class)
public class MixinGunDisplay implements EnhancedGunDisplay {
    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique @Nullable GunsmithLibGunDisplayExtension gunsmith$extension;

    @Override
    public Optional<GunsmithLibGunDisplayExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }
}
