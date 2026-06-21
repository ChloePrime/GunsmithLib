package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunItem;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy.EnergyWeaponData;
import mod.chloeprime.gunsmithlib.common.scripting.attachment.AttachmentScripting;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ModernKineticGunItem.class, remap = false)
public abstract class MixinModernKineticGunItem extends AbstractGunItem {

    // 能量武器

    @Inject(method = "tickHeat", at = @At("HEAD"))
    private void configuredEnergyWeaponLockHeatForeverUntilReload(
            ShooterDataHolder dataHolder,
            ItemStack gunItem,
            LivingEntity shooter,
            CallbackInfo ci
    ) {
        EnergyWeaponData.runtime(gunItem).ifPresent(data -> {
            if (data.energy().needsReloadOnFullHeat()) {
                if (data.gun().gunItem().isOverheatLocked(data.gun().gunStack())) {
                    dataHolder.heatTimestamp = Long.MAX_VALUE;
                }
            }
        });
    }

    // 配件脚本

    @ModifyReturnValue(method = "modifyProperty", at = @At("RETURN"))
    private <T> T letAttachmentsModifyProperty(
            T original,
            ShooterDataHolder dataHolder, ItemStack gunItem, LivingEntity shooter,
            String luaMethodName, String id, Class<T> type, T initial
    ) {
        return AttachmentScripting.modifyProperty(
                original, this,
                dataHolder, gunItem, shooter,
                luaMethodName, id, type);
    }

    @ApiStatus.Internal
    public MixinModernKineticGunItem(Properties pProperties) {
        super(pProperties);
    }
}
