package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.tacz.guns.api.item.gun.AbstractGunItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

import javax.annotation.Nonnull;

@Mixin(value = AbstractGunItem.class, remap = false, priority = 0)
public class MixinAbstractGunItemIntrinsics extends Item {

    // 耐久条

    @Intrinsic
    @Override
    public int getMaxDamage(@Nonnull ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    //

    public MixinAbstractGunItemIntrinsics(Properties pProperties) {
        super(pProperties);
    }
}
