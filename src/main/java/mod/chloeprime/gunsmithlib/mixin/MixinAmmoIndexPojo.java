package mod.chloeprime.gunsmithlib.mixin;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.pojo.AmmoIndexPOJO;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo.EnhancedAmmoData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo.GunsmithLibAmmoDataExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AmmoIndexPOJO.class)
public class MixinAmmoIndexPojo implements EnhancedAmmoData {
    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique @Nullable GunsmithLibAmmoDataExtension gunsmith$extension;

    @Override
    public Optional<GunsmithLibAmmoDataExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }
}
