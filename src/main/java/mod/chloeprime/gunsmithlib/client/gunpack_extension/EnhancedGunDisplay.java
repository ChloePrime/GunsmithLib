package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public interface EnhancedGunDisplay {
    Optional<GunsmithLibGunDisplayExtension> gunsmith$getGunsmithLibExtension();
}
