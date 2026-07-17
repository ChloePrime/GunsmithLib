package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.GunDisplayProperties;
import mod.chloeprime.gunsmithlib.client.EnergyWeaponVisuals;
import mod.chloeprime.gunsmithlib.client.GunsmithClientConfig;
import mod.chloeprime.gunsmithlib.client.gui.AmmoTypeHud;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.AirburstHUD;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks.ModifyDisplayPropertyHook;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.apache.commons.lang3.mutable.MutableInt;
import org.jetbrains.annotations.Nullable;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;
import java.util.regex.Pattern;

@Mixin(value = GunHudOverlay.class, remap = false)
public class MixinGunHudOverlay {
    /**
     * {@link CapabilityBasedModCompat#MAX_DISPLAYED_AMMO_SCANNED}
     */
    @Shadow @Final private static int MAX_AMMO_COUNT;

    // 剩余弹药数量包括背包内的弹药
    @Inject(
            method = "handleCacheCount",
            at = @At(value = "INVOKE", shift = At.Shift.AFTER, target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;handleInventoryAmmo(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/entity/player/Inventory;)V"))
    private static void addRemainingAmmoInBackpackToTotalRemainingAmmoCount(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo, CallbackInfo ci) {
        cacheInventoryAmmoCount += CapabilityBasedModCompat.getClientSyncedAmmoCountInBackpack(player);
        cacheInventoryAmmoCount = Math.min(MAX_AMMO_COUNT, cacheInventoryAmmoCount);
    }

    // at的drawString方法是forge加的，所以不remap
    @WrapOperation(
            method = "render",
            at = @At(ordinal = 0, value = "INVOKE", target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;FFIZ)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/gui/overlay/GunHudOverlay;handleCacheCount(Lnet/minecraft/client/player/LocalPlayer;Lnet/minecraft/world/item/ItemStack;Lcom/tacz/guns/resource/pojo/data/gun/GunData;Lcom/tacz/guns/api/item/IGun;Z)V"),
                    to = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/SharedConstants;getCurrentVersion()Lnet/minecraft/WorldVersion;")
            ))
    private static int energyWeaponShowHeat(
            GuiGraphics gui, Font pFont, @Nullable String pText, float pX, float pY, int pColor, boolean pDropShadow, Operation<Integer> original,
            GuiGraphics graphics, DeltaTracker delta
    ) {
        return EnergyWeaponVisuals.HUD.modifyCurrentAmmoDisplay(gui, pX, pY, graphics.guiWidth(), graphics.guiHeight(), () -> original.call(gui, pFont, pText, pX, pY, pColor, pDropShadow));
    }

    @Inject(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/client/resource/GunDisplayInstance;getHUDTexture()Lnet/minecraft/resources/ResourceLocation;"))
    private static void renderAirburstDistance(
            GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci
    ) {
        AirburstHUD.render(graphics, graphics.guiWidth(), graphics.guiHeight());
    }

    @ModifyArg(
            method = "render", index = 0,
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/gui/Font;width(Ljava/lang/String;)I"))
    private static String fixWidthForBatteryDisplayWhenAmmoIsAbove1000(String originalCounterText) {
        return EnergyWeaponVisuals.HUD.isEnabled() && gunsmithlib$COUNTER_PATTERN.matcher(originalCounterText).matches()
                ? "000"
                : originalCounterText;
    }

    @Inject(method = "handleCacheCount", at = @At("TAIL"))
    private static void energyWeaponShowTotalAmmo(LocalPlayer player, ItemStack stack, GunData gunData, IGun iGun, boolean useInventoryAmmo, CallbackInfo ci) {
        var cache = new MutableInt(cacheInventoryAmmoCount);
        EnergyWeaponVisuals.HUD.modifyBackupAmmoDisplay(stack, cache);
        cacheInventoryAmmoCount = cache.getValue();
    }

    // Modify Properties

    @ModifyExpressionValue(
            method = "render",
            at = @At(value = "INVOKE", ordinal = 0, target = "Ljava/lang/Math;min(II)I"),
            slice = @Slice(from = @At(value = "CONSTANT", args = "intValue=9999")))
    private int modifyDisplayedAmmoCount(int original) {
        var gun = Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .orElse(ItemStack.EMPTY);
        if (gun.isEmpty()) {
            return original;
        }
        return ModifyDisplayPropertyHook.modifyProperty(gun, GunDisplayProperties.AMMO_AMOUNT, Integer.class, original);
    }

    // 弹种显示

    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", remap = true, ordinal = 0, target = "Lnet/minecraft/client/gui/GuiGraphics;drawString(Lnet/minecraft/client/gui/Font;Ljava/lang/String;III)I"),
            slice = @Slice(from = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/SharedConstants;getCurrentVersion()Lnet/minecraft/WorldVersion;")))
    private int replaceDebugInfoWithAmmoTypeText(GuiGraphics graphics, Font font, String taczVersion, int x, int y, int oldColor, Operation<Integer> original) {
        var mode = GunsmithClientConfig.HUD_AMMO_TYPE_NAME_OPTION.get();
        if (mode == GunsmithClientConfig.AmmoTypeHudMode.DISABLE) {
            return original.call(graphics, font, taczVersion, x, y, oldColor);
        }
        var rawAmmoName = AmmoTypeHud.getAmmoName().orElse(null);
        if (rawAmmoName == null) {
            return original.call(graphics, font, taczVersion, x, y, oldColor);
        }
        var isStyled = mode == GunsmithClientConfig.AmmoTypeHudMode.STYLED;
        var ammoName = isStyled
                ? Objects.requireNonNull(ChatFormatting.stripFormatting(rawAmmoName))
                : rawAmmoName;
        float maxWidth = 34 + (AmmoTypeHud.isIconEnabled() ? AmmoTypeHud.totalIconWidth() : 0);
        int width = font.width(ammoName);
        int color = isStyled ? 0xff_d0_d0_d0 : 0xff_ff_ff_ff;
        if (width <= maxWidth) {
            return original.call(graphics, font, ammoName, x, y, color);
        } else {
            float scale = maxWidth / width;
            var pose = graphics.pose();
            pose.pushPose();
            {
                var useScale = scale >= 0.75f;
                if (useScale) {
                    pose.scale(scale, scale, 1);
                    original.call(graphics, font, ammoName, (int) (x / scale), (int) (y / scale), color);
                } else {
                    var parentScale = 0.25f;
                    for (FormattedCharSequence line : font.split(Component.literal(ammoName), (int) (maxWidth / parentScale))) {
                        graphics.drawString(font, line, x, y, color);
                        pose.translate(0, 10, 0);
                    }
                }
            }
            pose.popPose();
            return (int) maxWidth;
        }
    }

    @WrapMethod(method = "render")
    private void offsetForAmmoTypeDisplay(GuiGraphics graphics, DeltaTracker delta, Operation<Void> original) {
        var pose = graphics.pose();
        pose.pushPose();
        {
            if (AmmoTypeHud.isIconEnabled()) {
                pose.translate(-AmmoTypeHud.totalIconWidth(), 0, 0);
            }
            original.call(graphics, delta);
        }
        pose.popPose();
    }

    @Inject(method = "render", at = @At("TAIL"))
    private void renderAmmoType(GuiGraphics graphics, DeltaTracker delta, CallbackInfo ci) {
        int width = graphics.guiWidth();
        int height = graphics.guiHeight();
        AmmoTypeHud.renderIcon(graphics, width, height);
    }

    private static final @Unique Pattern gunsmithlib$COUNTER_PATTERN = Pattern.compile("^\\d+%?$");
    @Shadow private static int cacheInventoryAmmoCount;
}
