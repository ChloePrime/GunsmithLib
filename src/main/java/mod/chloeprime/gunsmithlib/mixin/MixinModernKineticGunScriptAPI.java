package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.common.AbstractGunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.OverheatFeedback;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;
import java.util.Optional;

@Mixin(value = ModernKineticGunScriptAPI.class, remap = false)
public class MixinModernKineticGunScriptAPI implements AbstractGunScriptAPIExtension {
    // 背包供弹功能

    @ModifyReturnValue(method = "hasAmmoToConsume", at = @At("TAIL"))
    private boolean makeHasAmmoToConsumeConsiderContainerItems(boolean original) {
        return original || CapabilityBasedModCompat.hasAmmoToConsume(shooter, itemStack);
    }

    @ModifyReturnValue(method = "consumeAmmoFromPlayer", at = @At("TAIL"))
    private int makeConsumeAmmoFromPlayerConsiderContainerItems(int original, int requested) {
        if (original >= requested) {
            return original;
        }
        return original + CapabilityBasedModCompat.consumeAmmoFromPlayer(shooter, itemStack, requested - original, false);
    }

    // 换弹速度

    @ModifyReturnValue(method = "getReloadTime", at = @At("RETURN"))
    private long reloadSpeedScaler(long original) {
        return shooter != null ? (long) (original * shooter.getAttributeValue(GunAttributes.RELOAD_SPEED)) : original;
    }

    // 扩展 API

    private @Unique String gunsmith$gunIdString;

    @Inject(method = "initGunItem", at = @At("TAIL"))
    private void initGunIdString(CallbackInfo ci) {
        gunsmith$gunIdString = String.valueOf(gunId);
    }

    @Override
    public String gunsmith_getGunId() {
        return Objects.requireNonNullElseGet(gunsmith$gunIdString, this::gunsmith$getGunIdHelper);
    }

    @Override
    public void gunsmith_playOverheatSound() {
        if (shooter != null) {
            OverheatFeedback.playCooldownSound(shooter);
        }
    }

    @Override
    public float gunsmith_getCooldownSeconds() {
        if (itemStack == null || !(shooter instanceof Player playerShooter)) {
            return 0;
        }
        var gunItem = itemStack.getItem();
        var cooldowns = playerShooter.getCooldowns();
        return cooldowns.getCooldownPercent(gunItem, 1)
                * GsHelper.getCooldownDuration(cooldowns, gunItem)
                / 20;
    }

    @Override
    public float gunsmith_getCooldownPercent() {
        if (itemStack == null || !(shooter instanceof Player playerShooter)) {
            return 0;
        }
        return playerShooter.getCooldowns().getCooldownPercent(itemStack.getItem(), 1);
    }

    @Inject(method = "handleShootHeat", at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/gun/AbstractGunItem;setOverheatLocked(Lnet/minecraft/world/item/ItemStack;Z)V"))
    private void sfxFeedback(CallbackInfo ci) {
        if (!abstractGunItem.isOverheatLocked(itemStack)) {
            OverheatFeedback.tryPlayCooldownSound(shooter, itemStack);
        }
    }

    @Inject(method = "setOverheatLocked", at = @At("HEAD"))
    private void sfxFeedback(boolean locked, CallbackInfo ci) {
        if (shooter == null) {
            return;
        }
        if (!abstractGunItem.isOverheatLocked(itemStack) && locked) {
            OverheatFeedback.tryPlayCooldownSound(shooter, itemStack);
        }
    }

    @Shadow private AbstractGunItem abstractGunItem;
    @Shadow private ItemStack itemStack;
    @Shadow private LivingEntity shooter;
    @Shadow private ResourceLocation gunId;

    @Override
    public ItemStack gunsmithlib$getCurrentItem() {
        return itemStack;
    }

    @Override
    public IGun gunsmithlib$getGunItemInterface() {
        return abstractGunItem;
    }

    @Override
    public Optional<LivingEntity> gunsmithlib$getShooter() {
        return Optional.ofNullable(shooter);
    }

    @Override
    public LogicalSide gunsmithlib$getSide() {
        return LogicalSide.SERVER;
    }
}
