package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.tacz.guns.item.AttachmentItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Intrinsic;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = AttachmentItem.class, remap = false, priority = 0)
public class MixinAttachmentItemIntrinsics extends Item {

    // 耐久条

    @Intrinsic
    @Override
    public int getMaxDamage(ItemStack stack) {
        return super.getMaxDamage(stack);
    }

    //

    public MixinAttachmentItemIntrinsics(Properties pProperties) {
        super(pProperties);
    }
}
