package mod.chloeprime.gunsmithlib.mixin;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.resource.CommonAssetsManager;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check.ArcanaExtrasExcludeStrategy;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = CommonAssetsManager.class, remap = false)
public class MixinCommonAssetBuilder {
    @WrapOperation(
            method = "<clinit>",
            at = @At(value = "INVOKE", target = "Lcom/google/gson/GsonBuilder;create()Lcom/google/gson/Gson;"))
    private static Gson injectGsonExcludeStrategy(GsonBuilder gsonBuilder, Operation<Gson> original) {
        return original.call(gsonBuilder.setExclusionStrategies(ArcanaExtrasExcludeStrategy.INSTANCE));
    }
}
