package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.gui.overlay.HeatBarOverlay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = HeatBarOverlay.class, remap = false)
public class MixinHeatBarOverlay {
    @WrapOperation(
            method = "render",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/api/item/IGun;hasHeatData(Lnet/minecraft/world/item/ItemStack;)Z"))
    private boolean disableHeatBarIfConfigured(IGun gunInterface, ItemStack stack, Operation<Boolean> original) {
        var isDisabled = TimelessAPI.getGunDisplay(stack)
                .map(instance -> ((EnhancedGunDisplay) instance))
                .flatMap(EnhancedGunDisplay::gunsmith$getGunsmithLibExtension)
                .filter(GunsmithLibGunDisplayExtension::hideHeatBarOverlay)
                .isPresent();
        return !isDisabled && original.call(gunInterface, stack);
    }
}
