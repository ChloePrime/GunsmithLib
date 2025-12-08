package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun;

import net.minecraft.world.item.ItemStack;

public class HideFromCreativeTabSystem {
    public static boolean shouldHide(ItemStack stack) {
        return GunsmithLibGunDataExtension.of(stack).filter(GunsmithLibGunDataExtension::isHidden).isPresent();
    }
}
