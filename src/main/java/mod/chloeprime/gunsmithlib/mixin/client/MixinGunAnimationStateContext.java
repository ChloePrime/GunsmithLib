package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import mod.chloeprime.gunsmithlib.client.AbstractGunAnimationStateContextExtension;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;
import java.util.function.Function;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public abstract class MixinGunAnimationStateContext implements AbstractGunAnimationStateContextExtension {
    @Shadow private ItemStack currentGunItem;
    @Shadow private IGun iGun;

    // 扩展 API

    @Shadow
    protected abstract <T> Optional<T> processCameraEntity(Function<Entity, T> processor);

    @Override
    public float gunsmith_getCooldownSeconds() {
        if (currentGunItem == null || currentGunItem.isEmpty()) {
            return 0;
        }
        var MC = Minecraft.getInstance();
        var player = MC.player;
        if (player == null) {
            return 0;
        }
        var gunItem = currentGunItem.getItem();
        var cooldowns = player.getCooldowns();
        return cooldowns.getCooldownPercent(gunItem, ClientProxy.getPartialTick())
                * GsHelper.getCooldownDuration(cooldowns, gunItem)
                / 20;
    }

    @Override
    public float gunsmith_getCooldownPercent() {
        if (currentGunItem == null || currentGunItem.isEmpty()) {
            return 0;
        }
        var MC = Minecraft.getInstance();
        var player = MC.player;
        if (player == null) {
            return 0;
        }
        return player.getCooldowns().getCooldownPercent(currentGunItem.getItem(), ClientProxy.getPartialTick());
    }

    @Override
    public ItemStack gunsmithlib$getCurrentItem() {
        return currentGunItem;
    }

    @Override
    public IGun gunsmithlib$getGunItemInterface() {
        return iGun;
    }

    @Override
    public Optional<LivingEntity> gunsmithlib$getShooter() {
        return Optional.ofNullable(Minecraft.getInstance().player);
    }

    // 镜喵背包供弹实现

    @ModifyReturnValue(method = "hasAmmoToConsume", at = @At("TAIL"))
    private boolean makeHasAmmoToConsumeConsiderContainerItems(boolean original) {
        return original || processCameraEntity(this::gunsmithlib$makeHasAmmoToConsumeConsiderContainerItems0).orElse(false);
    }

    @Unique
    private boolean gunsmithlib$makeHasAmmoToConsumeConsiderContainerItems0(Entity entity) {
        if (!(entity instanceof LivingEntity user)) {
            return false;
        }
        var gunItem = currentGunItem;
        if (gunItem == null) {
            return false;
        }
        return CapabilityBasedModCompat.hasAmmoToConsume(user, gunItem);
    }
}
