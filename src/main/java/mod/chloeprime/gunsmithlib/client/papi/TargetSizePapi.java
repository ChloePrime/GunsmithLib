package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.EntityStates;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import mod.chloeprime.gunsmithlib.client.papi.framework.PapiTargetCache;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 瞄准目标的大小
 * <p>
 * {@code %gunsmithlib:target_size%}
 * <p>
 * 一位小数
 *
 * @since 6.4
 */
public enum TargetSizePapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("target_size").toString();

    @Override
    public String apply(ItemStack stack) {
        return PapiTargetCache.current()
                .map(Rangefinder.Result::asHitResult)
                .map(r -> r instanceof EntityHitResult er ? er.getEntity() : null)
                .map(EntityStates::of)
                .map(EntityStates::referential_size)
                .map("%.1f"::formatted)
                .orElse(FALLBACK_TEXT);
    }
}
