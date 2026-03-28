package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import net.neoforged.fml.ModList;

public class ArcanaCheckSystem {
    public static final String ARCANA_MOD_ID = "taczexpands";
    public static final boolean ARCANA_INSTALLED = ModList.get().isLoaded(ARCANA_MOD_ID);

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
