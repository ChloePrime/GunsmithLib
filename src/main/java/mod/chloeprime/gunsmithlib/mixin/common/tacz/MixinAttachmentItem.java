package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.tacz.guns.item.AttachmentItem;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = AttachmentItem.class, remap = false)
public class MixinAttachmentItem {

    // 耐久条

    /**
     * 让配置过耐久 data 的配件耐久设置为配置中的值，或脚本修改后的值。
     */
    @Dynamic
    @Inject(method = "getMaxDamage", remap = false, at = @At("HEAD"), cancellable = true)
    public void getMaxDamage(ItemStack stack, CallbackInfoReturnable<Integer> cir) {
        AttachmentInfo.of(stack)
                .flatMap(DurabilityData::of)
                .ifPresent(durData -> cir.setReturnValue(durData.getMaxDurability()));
    }
}
