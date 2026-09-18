package mod.chloeprime.gunsmithlib.client.bugfix.tacz;

import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public final class ContentItemParticleTextureFixSystem {
    public static ItemStack getParticleStack(ItemStack itemStack) {
        if (Gunsmith.getGunInfo(itemStack).isPresent() || Gunsmith.getAttachmentInfo(itemStack).isPresent()) {
            return Items.NETHERITE_BLOCK.getDefaultInstance();
        } else if (Gunsmith.getAmmoInfo(itemStack).isPresent()) {
            return Items.COPPER_BLOCK.getDefaultInstance();
        } else {
            return itemStack;
        }
    }

    private ContentItemParticleTextureFixSystem() {
    }
}
