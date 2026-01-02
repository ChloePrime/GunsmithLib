package mod.chloeprime.gunsmithlib.mixin.client.animation;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.renderer.item.GunItemRendererWrapper;
import com.tacz.guns.client.resource.GunDisplayInstance;
import mod.chloeprime.gunsmithlib.client.internal.EnhancedBedrockGunModel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(value = GunItemRendererWrapper.class, remap = false)
public class MixinGunItemRendererWrapper {
    @WrapMethod(method = "renderFirstPerson")
    private void sendContextToModel(LocalPlayer player, ItemStack stack, ItemDisplayContext ctx, PoseStack poseStack, MultiBufferSource bufferSource, int light, float partialTick, Operation<Void> original) {
        Runnable wrapped = () -> original.call(player, stack, ctx, poseStack, bufferSource, light, partialTick);
        var model = TimelessAPI.getGunDisplay(stack).map(GunDisplayInstance::getGunModel).orElse(null);
        if (model instanceof EnhancedBedrockGunModel enhanced) {
            var holder = enhanced.gunsmithlib$getExtension().holderContext;
            try {
                holder.push(player);
                wrapped.run();
            } finally {
                holder.pop();
            }
        } else {
            wrapped.run();
        }
    }
}
