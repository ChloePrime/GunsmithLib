package mod.chloeprime.gunsmithlib.mixin.client.tacz.bugfix;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import mod.chloeprime.gunsmithlib.client.bugfix.tacz.ContentItemParticleTextureFixSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.lang.reflect.Proxy;

@Mixin(ItemOverrides.class)
public abstract class ContentItemParticleTextureFixMixin {
    private static final @Unique String gunsmithlib$M_GET_PARTICLE_ICON = "getParticleIcon";

    @WrapMethod(method = "resolve")
    private BakedModel dontUseBlackPurpleBlockForTaCZContentItems(
            BakedModel model, ItemStack itemStack, ClientLevel level, LivingEntity entity, int seed, Operation<BakedModel> original
    ) {
        var originalResolvedModel = original.call(model, itemStack, level, entity, seed);
        var particleStack = ContentItemParticleTextureFixSystem.getParticleStack(itemStack);
        if (particleStack == itemStack) {
            return originalResolvedModel;
        }
        var particleItemModel = Minecraft.getInstance().getItemRenderer().getModel(particleStack, level, entity, seed);
        var resolvedParticleModel = original.call(particleItemModel, particleStack, level, entity, seed);
        return (BakedModel) Proxy.newProxyInstance(getClass().getClassLoader(), new Class[]{BakedModel.class}, ((instance, method, args) -> {
            if (gunsmithlib$M_GET_PARTICLE_ICON.equals(method.getName())) {
                return resolvedParticleModel.getParticleIcon(ModelData.EMPTY);
            } else {
                return method.invoke(originalResolvedModel, args);
            }
        }));
    }
}
