package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.AirburstSystem;
import net.minecraft.world.item.ItemStack;

public enum AirburstDistancePapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("airburst_distance").toString();

    @Override
    public String apply(ItemStack stack) {
        return Gunsmith.getGunInfo(stack)
                .map(AirburstSystem::getSelectedDistance)
                .map(od -> od.orElse(0))
                .map("%.1f"::formatted)
                .orElse("0.0");
    }
}
