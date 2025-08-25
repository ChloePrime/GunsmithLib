package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.event.CameraSetupEvent;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import net.minecraft.client.player.LocalPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CameraSetupEvent.class, remap = false)
public class RecoilAttributeMixin {
    @WrapOperation(
            method = "applyCameraRecoil",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/player/LocalPlayer;setXRot(F)V")
    )
    private static void modifyVRot(LocalPlayer player, float v, Operation<Void> original) {
        var delta = player.getXRot() - v;
        delta *= (float) player.getAttributeValue(GunAttributes.V_RECOIL);
        original.call(player, player.getXRot() - delta);
    }

    @WrapOperation(
            method = "applyCameraRecoil",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/player/LocalPlayer;setYRot(F)V")
    )
    private static void modifyHRot(LocalPlayer player, float v, Operation<Void> original) {
        var delta = player.getYRot() - v;
        delta *= (float) player.getAttributeValue(GunAttributes.H_RECOIL);
        original.call(player, player.getYRot() - delta);
    }
}
