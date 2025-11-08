package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.AmmoVariantSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AbstractGunItem.class, remap = false)
public class MixinAbstractGunItem {
    @ModifyReturnValue(method = {"canReload", "hasInventoryAmmo"}, at = @At("TAIL"))
    private boolean canReloadIfHasAmmoInBackpacks(boolean original, LivingEntity shooter, ItemStack gunItem) {
        return original || (shooter.level().isClientSide
                ? CapabilityBasedModCompat.getClientSyncedAmmoCountInBackpack(shooter) > 0
                : CapabilityBasedModCompat.hasAmmoToConsume(shooter, gunItem));
    }

    /**
     * 用于阻止切换枪械 id 时播放收枪拔枪动画
     */
    @ModifyReturnValue(method = "isSame", at = @At("RETURN"))
    private boolean isSameAcrossVariantSwitching(boolean original, ItemStack a, ItemStack b) {
        return original || AmmoVariantSystem.hasVariantConnection(a, b);
    }
}
