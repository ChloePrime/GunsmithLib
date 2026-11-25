package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.extensions.IForgeItem;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = Item.class, priority = 0)
public abstract class MixinItemIntrinsics implements IForgeItem {
    @Override
    public boolean isDamageable(ItemStack stack) {
        return IForgeItem.super.isDamageable(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return IForgeItem.super.getMaxDamage(stack);
    }
}
