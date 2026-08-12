package mod.chloeprime.gunsmithlib.client.papi;

import com.tacz.guns.resource.pojo.data.gun.GunHeatData;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

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
                .flatMap(HeatPercentPapi::getHeatPercentageText)
                .orElse(FALLBACK_TEXT);
    }

    private static Optional<String> getHeatPercentageText(GunInfo gi) {
        float cur = gi.gunItem().getHeatAmount(gi.gunStack());
        float max = Optional.ofNullable(gi.index().getGunData().getHeatData())
                .map(GunHeatData::getHeatMax)
                .orElse(Float.NaN);
        if (Float.isNaN(max)) {
            return Optional.empty();
        }
        return Optional.of(String.valueOf((int) Math.floor(cur / max + 1e-7f)));
    }
}
