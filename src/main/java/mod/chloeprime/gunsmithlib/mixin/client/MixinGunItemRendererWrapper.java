package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = GunItemRendererWrapper.class, remap = false)
public abstract class MixinGunItemRendererWrapper {
    @WrapOperation(
            method = "/^lambda\\$renderFirstPerson\\$\\d+/",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/client/renderer/RenderType;entityCutout(Lnet/minecraft/resources/ResourceLocation;)Lnet/minecraft/client/renderer/RenderType;"))
    private RenderType unlockTransparency(ResourceLocation id, Operation<RenderType> original, ItemStack gun) {
        var enabled = GunsmithLibGunDisplayExtension
                .of(gun)
                .filter(GunsmithLibGunDisplayExtension::unlocksTransparency)
                .isPresent();
        if (enabled) {
            return RenderType.entityTranslucent(getTextureLocation(gun));
        } else {
            return original.call(id);
        }
    }

    @Shadow public abstract ResourceLocation getTextureLocation(ItemStack stack);
}
