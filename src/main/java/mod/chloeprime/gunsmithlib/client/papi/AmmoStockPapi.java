package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import mod.chloeprime.gunsmithlib.mixin.client.GunHudOverlayAccessor;
import net.minecraft.world.item.ItemStack;

/**
 * 储备弹药量
 * <p>
 * {@code %gunsmithlib:ammo_stock%}
 * <p>
 * 整数
 *
 * @since 6.4
 */
public enum AmmoStockPapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("ammo_stock").toString();

    @Override
    public String apply(ItemStack stack) {
        return String.valueOf(GunHudOverlayAccessor.getCacheInventoryAmmoCount());
    }
}
