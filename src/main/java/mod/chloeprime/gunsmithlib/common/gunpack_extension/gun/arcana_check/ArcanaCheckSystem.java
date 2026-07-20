package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;

public class ArcanaCheckSystem {
    public static final boolean ARCANA_INSTALLED = ModInstallationStatus.ARCANA_INSTALLED;

    public static boolean shouldHintArcanaInstallation(GunInfo gun) {
        if (ARCANA_INSTALLED) {
            return false;
        }
        return ((EnhancedGunData) gun.index().getGunData())
                .gunsmith$getArcanaExtras()
                .map(ArcanaExtras::getAmmoTypes)
                .filter(arr -> arr.length > 0)
                .isPresent();
    }
}
