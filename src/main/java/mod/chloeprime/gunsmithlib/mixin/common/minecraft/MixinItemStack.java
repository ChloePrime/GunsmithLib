package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunAttachmentAttributeAggregator;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.List;

@Mixin(ItemStack.class)
public abstract class MixinItemStack {
    private @Unique List<Component> gunsmithlib$capturedTooltipList;

    @ModifyExpressionValue(
            method = "getTooltipLines",
            at = @At(value = "INVOKE", remap = false, target = "Lcom/google/common/collect/Lists;newArrayList()Ljava/util/ArrayList;"))
    private ArrayList<Component> captureTooltipLines(ArrayList<Component> original) {
        gunsmithlib$capturedTooltipList = original;
        return original;
    }


    @Inject(
            method = "getTooltipLines",
            at = @At("RETURN"))
    private void releaseCapturedTooltipLines(Player player, TooltipFlag isAdvanced, CallbackInfoReturnable<List<Component>> cir) {
        gunsmithlib$capturedTooltipList = null;
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/world/item/ItemStack$TooltipPart;MODIFIERS:Lnet/minecraft/world/item/ItemStack$TooltipPart;"))
    private void attachmentModifierTooltip(Player player, TooltipFlag isAdvanced, CallbackInfoReturnable<List<Component>> cir) {
        if (shouldShowInTooltip(getHideFlags(), ItemStack.TooltipPart.MODIFIERS)) {
            var self = (ItemStack) (Object) this;
            GunAttachmentAttributeAggregator.attachmentAttributeModifierTooltip(self, gunsmithlib$capturedTooltipList);
        }
    }

    // 耐久条

    /**
     * 根据配置决定是否在耐久耗尽后销毁物品。
     */
    @ModifyReturnValue(method = "hurt", at = @At("TAIL"))
    private boolean dontBreakIfDurabilityExhausted(boolean originalWillBreak) {
        var self = (ItemStack) (Object) this;
        if (!originalWillBreak) {
            return false;
        }
        var durData = DurabilityData.of(self).orElse(null);
        if (durData == null) {
            return true;
        }
        var transformTo = durData.getTransformToOnDurabilityExhausted().orElse(null);
        if (transformTo != null) {
            Gunsmith.getGunInfo(self).ifPresent(gun -> gun.gunItem().setGunId(gun.gunStack(), transformTo));
            // 配件耐久只能通过 Gunsmith 的 API 消耗，
            // 且消耗后会写回，所以这里不写回武器也不要紧。
            Gunsmith.getAttachmentInfo(self).ifPresent(attach -> attach.attachmentItem().setAttachmentId(attach.attachmentStack(), transformTo));
            return false;
        }
        var unbreak = !durData.breaksOnDurabilityExhausted();
        if (unbreak) {
            var maxDamage = getMaxDamage();
            if (getDamageValue() > maxDamage) {
                setDamageValue(maxDamage);
            }
        }
        return !unbreak;
    }

    //

    @Shadow public abstract void setDamageValue(int pDamage);
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract int getDamageValue();

    @Shadow
    private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart) {
        return true;
    }

    @Shadow protected abstract int getHideFlags();
}
