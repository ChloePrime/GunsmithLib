package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractGunItem.class, remap = false)
public class MixinAbstractGunItem {
    @ModifyReturnValue(method = "canReload", at = @At("TAIL"))
    private boolean canReloadIfHasAmmoInBackpacks(boolean original, LivingEntity shooter, ItemStack gunItem) {
        return original || (shooter.level().isClientSide
                ? CapabilityBasedModCompat.getClientSyncedAmmoCountInBackpack(shooter) > 0
                : CapabilityBasedModCompat.hasAmmoToConsume(shooter, gunItem));
    }
}
