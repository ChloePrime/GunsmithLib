package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@Mixin(value = Item.class, priority = 0)
public abstract class MixinItemIntrinsics implements IItemExtension {
    @Override
    public boolean isDamageable(ItemStack stack) {
        return IItemExtension.super.isDamageable(stack);
    }

    @Override
    public int getMaxDamage(ItemStack stack) {
        return IItemExtension.super.getMaxDamage(stack);
    }
}
