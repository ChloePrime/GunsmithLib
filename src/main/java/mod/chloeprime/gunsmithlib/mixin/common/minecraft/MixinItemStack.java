package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunAttachmentAttributeAggregator;
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



    @Shadow
    private static boolean shouldShowInTooltip(int pHideFlags, ItemStack.TooltipPart pPart) {
        return true;
    }

    @Shadow protected abstract int getHideFlags();
}
