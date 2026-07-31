package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

/**
 * 过热条百分比
 * <p>
 * {@code %gunsmithlib:heat_percent%}
 * <p>
 * 整数，0-100，不含百分号
 *
 * @since 6.4
 */
public enum HeatPercentPapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("heat_percent").toString();

    @Override
    public String apply(ItemStack stack) {
        return Gunsmith.getGunInfo(stack)
                .filter(gi -> gi.gunItem().hasHeatData(gi.gunStack()))
                .map(HeatPercentPapi::getHeatPercentageText)
                .orElse(FALLBACK_TEXT);
    }

    private static String getHeatPercentageText(GunInfo gi) {
        float cur = gi.gunItem().getHeatAmount(gi.gunStack());
        float max = Objects.requireNonNull(gi.index().getGunData().getHeatData()).getHeatMax();
        return String.valueOf((int) Math.floor(cur / max + 1e-7f));
    }
}
