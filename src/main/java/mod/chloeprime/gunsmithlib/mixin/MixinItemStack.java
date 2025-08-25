package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunAttachmentAttributeAggregator;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
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
    private void releaseCapturedTooltipLines(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        gunsmithlib$capturedTooltipList = null;
    }

    @Inject(
            method = "getTooltipLines",
            at = @At(value = "INVOKE", target = "Lnet/neoforged/neoforge/common/util/AttributeUtil;addAttributeTooltips(Lnet/minecraft/world/item/ItemStack;Ljava/util/function/Consumer;Lnet/neoforged/neoforge/common/util/AttributeTooltipContext;)V"))
    private void attachmentModifierTooltip(Item.TooltipContext tooltipContext, @Nullable Player player, TooltipFlag tooltipFlag, CallbackInfoReturnable<List<Component>> cir) {
        var self = (ItemStack) (Object) this;
        GunAttachmentAttributeAggregator.attachmentAttributeModifierTooltip(self, gunsmithlib$capturedTooltipList);
    }
}
