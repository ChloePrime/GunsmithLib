package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.client.tooltip.ClientGunTooltip;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = ClientGunTooltip.class, remap = false)
public class MixinClientGunTooltip {
    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getDamageWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedDamageConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        var gunIndex = this.gunIndex;
        if (gun == null || gunIndex == null || gun.isEmpty()) {
            return original;
        }
        var shrapnel = gunIndex.getBulletData().getBulletAmount();
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.BULLET_DAMAGE, original / shrapnel) * shrapnel;
    }

    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getArmorIgnoreWithAttachment(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedArmorPiercingConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        if (gun == null || gun.isEmpty()) {
            return original;
        }
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.BULLET_DAMAGE, original);
    }

    @ModifyExpressionValue(
            method = "getText",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/util/AttachmentDataUtils;getHeadshotMultiplier(Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;)D"))
    private double makeDisplayedHeadshotMultiplierConsiderAttributeModifiers(double original) {
        var gun = this.gun;
        if (gun == null || gun.isEmpty()) {
            return original;
        }
        return GsHelper.evaluateItemAttribute(gun, GunAttributes.BULLET_DAMAGE, original);
    }

    @Shadow @Final private ItemStack gun;
    @Shadow @Final private CommonGunIndex gunIndex;
}
