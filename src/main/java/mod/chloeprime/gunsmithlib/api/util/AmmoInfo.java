package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

public record AmmoInfo(
        ItemStack ammoStack,
        IAmmo ammoItem,
        ResourceLocation ammoId,
        CommonAmmoIndex index
) {
    /**
     * @since 4.12.0
     */
    public static Optional<AmmoInfo> of(ItemStack gun) {
        return Gunsmith.getAmmoInfo(gun);
    }
}
