package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.event.FirstPersonRenderEvent;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.AmmoVariantSystem;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = FirstPersonRenderEvent.class, remap = false)
public class MixinFirstPersonRenderEventHandler {
    @WrapOperation(
            method = "onRenderHand",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/item/ItemStack;matches(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemStack;)Z"))
    private static boolean doNotDrawWeaponOnVariantSwitch(ItemStack a, ItemStack b, Operation<Boolean> original) {
        return original.call(a, b) || AmmoVariantSystem.hasVariantConnection(a, b);
    }
}
