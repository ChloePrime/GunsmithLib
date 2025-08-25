package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import mod.chloeprime.gunsmithlib.client.AbstractGunAnimationStateContextExtension;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Optional;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public class MixinGunAnimationStateContext implements AbstractGunAnimationStateContextExtension {
    @Shadow private ItemStack currentGunItem;
    @Shadow private IGun iGun;

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
}
