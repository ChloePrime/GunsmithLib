package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.AirburstSystem;
import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

public class AirburstDistancePapi implements Function<ItemStack, String> {
    public static final String NAME = GunsmithLib.loc("airburst_distance").toString();
    public static final AirburstDistancePapi INSTANCE = new AirburstDistancePapi();

    @Override
    public String apply(ItemStack stack) {
        return Gunsmith.getGunInfo(stack)
                .map(AirburstSystem::getSelectedDistance)
                .map(od -> od.orElse(0))
                .map("%.1f"::formatted)
                .orElse("0.0");
    }

    private AirburstDistancePapi() {
    }
}
