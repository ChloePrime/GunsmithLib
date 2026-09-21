package mod.chloeprime.gunsmithlib.client.bugfix.tacz;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.item.ItemStack;

public final class ContentItemParticleTextureFixSystem {
    public static ItemStack getParticleStack(ItemStack itemStack) {
        if (Gunsmith.getGunInfo(itemStack).isPresent() || Gunsmith.getAttachmentInfo(itemStack).isPresent()) {
            return GunsmithLib.Items.GUN_STEEL_BLOCK.get().getDefaultInstance();
        } else if (Gunsmith.getAmmoInfo(itemStack).isPresent()) {
            return GunsmithLib.Items.GIGA_AMMO_BLOCK.get().getDefaultInstance();
        } else {
            return itemStack;
        }
    }

    private ContentItemParticleTextureFixSystem() {
    }
}
