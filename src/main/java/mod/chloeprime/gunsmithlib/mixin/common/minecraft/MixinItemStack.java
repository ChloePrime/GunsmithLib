package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunAttachmentAttributeAggregator;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

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
        GunAttachmentAttributeAggregator.attachmentAttributeModifierTooltip(self, gunsmithlib$capturedTooltipList, player, tooltipContext, tooltipFlag);
    }

    // 耐久条

    /**
     * 根据配置决定是否在耐久耗尽后销毁物品。
     */
    @Inject(
            method = "hurtAndBreak(ILnet/minecraft/server/level/ServerLevel;Lnet/minecraft/world/entity/LivingEntity;Ljava/util/function/Consumer;)V",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"),
            cancellable = true)
    private void dontBreakIfDurabilityExhausted(int damage, ServerLevel level, LivingEntity entity, Consumer<Item> onBreak, CallbackInfo ci) {
        var originalWillBreak = getDamageValue() >= getMaxDamage();
        var self = (ItemStack) (Object) this;
        if (!originalWillBreak) {
            return;
        }
        var durData = DurabilityData.of(self).orElse(null);
        if (durData == null) {
            ci.cancel();
            return;
        }
        var transformTo = durData.getTransformToOnDurabilityExhausted().orElse(null);
        if (transformTo != null) {
            Gunsmith.getGunInfo(self).ifPresent(gun -> gun.gunItem().setGunId(gun.gunStack(), transformTo));
            // 配件耐久只能通过 Gunsmith 的 API 消耗，
            // 且消耗后会写回，所以这里不写回武器也不要紧。
            Gunsmith.getAttachmentInfo(self).ifPresent(attach -> attach.attachmentItem().setAttachmentId(attach.attachmentStack(), transformTo));
            ci.cancel();
            return;
        }
        var unbreak = !durData.breaksOnDurabilityExhausted();
        if (unbreak) {
            var maxDamage = getMaxDamage();
            if (getDamageValue() > maxDamage) {
                setDamageValue(maxDamage);
            }
            ci.cancel();
        }
    }

    //

    @Shadow public abstract void setDamageValue(int pDamage);
    @Shadow public abstract int getMaxDamage();
    @Shadow public abstract int getDamageValue();
}
