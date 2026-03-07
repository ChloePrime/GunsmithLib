package mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo;

import mod.chloeprime.gunsmithlib.api.util.AmmoInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * 子弹 data 扩展。
 *
 * @since 5.2.0
 */
public class GunsmithLibAmmoDataExtension extends GunsmithLibSharedDataExtension {

    // 下面是代码

    public static Optional<GunsmithLibAmmoDataExtension> of(ItemStack stack) {
        return Gunsmith.getAmmoInfo(stack).flatMap(GunsmithLibAmmoDataExtension::of);
    }

    public static Optional<GunsmithLibAmmoDataExtension> of(AmmoInfo ammoInfo) {
        return ((EnhancedAmmoData) ammoInfo.index().getPojo()).gunsmith$getGunsmithLibExtension();
    }
}
