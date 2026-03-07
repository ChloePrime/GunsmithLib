package mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo;

import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public interface EnhancedAmmoData {
    Optional<GunsmithLibAmmoDataExtension> gunsmith$getGunsmithLibExtension();
}
