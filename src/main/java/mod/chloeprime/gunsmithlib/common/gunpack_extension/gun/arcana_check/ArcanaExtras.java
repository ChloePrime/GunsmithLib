package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check;

import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;

public class ArcanaExtras {
    @GunpackProperty
    private ArcanaAmmoType[] ammo_types;

    public ArcanaAmmoType[] getAmmoTypes() {
        return ammo_types;
    }
}
