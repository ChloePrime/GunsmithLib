package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.item.gun.AbstractGunItem;
import com.tacz.guns.entity.shooter.LivingEntityFireSelect;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.AmmoVariantSystem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = LivingEntityFireSelect.class, remap = false)
public class MixinLivingEntityFireSelect {

    @WrapOperation(
            method = "fireSelect",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/gun/AbstractGunItem;fireSelect(Lcom/tacz/guns/entity/shooter/ShooterDataHolder;Lnet/minecraft/world/item/ItemStack;)V"))
    private void postFireSelect(AbstractGunItem gun, ShooterDataHolder shooterDataHolder, ItemStack stack, Operation<Void> original) {
        original.call(gun, shooterDataHolder, stack);
        AmmoVariantSystem.onFireModeSelect(stack, gun, shooter);
    }

    @Shadow @Final private LivingEntity shooter;
}
