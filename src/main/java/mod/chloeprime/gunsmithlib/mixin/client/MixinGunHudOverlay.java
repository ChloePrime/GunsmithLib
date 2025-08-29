package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.client.EnergyWeaponVisuals;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = GunHudOverlay.class, remap = false)
public class MixinGunHudOverlay {
    // 剩余弹药数量包括背包内的弹药
    @Inject(
            method = "handleCacheCount",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;handleInventoryAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Inventory;)V"))
    private static void addRemainingAmmoInBackpackToTotalRemainingAmmoCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo, CallbackInfo ci) {
        cacheInventoryAmmoCount += CapabilityBasedModCompat.getClientSyncedAmmoCountInBackpack(player);
        cacheInventoryAmmoCount = Math.min(9999, cacheInventoryAmmoCount);
    }

    // at的drawString方法是forge加的，所以不remap
    @WrapOperation(
            method = "render",
            at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;handleCacheCount(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;Lcom/tacz/guns/api/item/IGun;Z)V"),
                    to = @At(value = "INVOKE", remap = true, opcode = Opcodes.GETSTATIC, target = "Lnet/minecraft/SharedConstants;getCurrentVersion()Lnet/minecraft/WorldVersion;")
            ))
    private static int energyWeaponShowHeat(
            GuiGraphics gui, Font pFont, @Nullable String pText, float pX, float pY, int pColor, boolean pDropShadow, Operation<Integer> original,
            GuiGraphics graphics, float partialTick, int width, int height
    ) {
        return EnergyWeaponVisuals.HUD.modifyCurrentAmmoDisplay(gui, pX, pY, width, height, () -> original.call(gui, pFont, pText, pX, pY, pColor, pDropShadow));
    }

    @Inject(method = "handleCacheCount", at = @At("TAIL"))
    private static void energyWeaponShowTotalAmmo(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo, CallbackInfo ci) {
        var cache = new MutableInt(cacheInventoryAmmoCount);
        EnergyWeaponVisuals.HUD.modifyBackupAmmoDisplay(stack, cache);
        cacheInventoryAmmoCount = cache.getValue();
    }

    @Shadow private static int cacheInventoryAmmoCount;
}
