package mod.chloeprime.gunsmithlib.mixin.client.animation;

import com.tacz.guns.client.model.BedrockAnimatedModel;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.pojo.model.BedrockModelPOJO;
import com.tacz.guns.client.resource.pojo.model.BedrockVersion;
import mod.chloeprime.gunsmithlib.client.animation.BedrockGunModelExtension;
import mod.chloeprime.gunsmithlib.client.animation.SpecialBedrockModelBones;
import mod.chloeprime.gunsmithlib.client.internal.EnhancedBedrockGunModel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = BedrockGunModel.class, remap = false)
public class MixinBedrockGunModel extends BedrockAnimatedModel implements EnhancedBedrockGunModel {
    private final @Unique BedrockGunModelExtension gunsmithlib$extension = new BedrockGunModelExtension((BedrockGunModel) (Object) this);

    @Override
    public BedrockGunModelExtension gunsmithlib$getExtension() {
        return gunsmithlib$extension;
    }

    @Inject(method = "<init>", at = @At("TAIL"))
    private void initExtraGroupInfo(BedrockModelPOJO pojo, BedrockVersion version, CallbackInfo ci) {
        setFunctionalRenderer(SpecialBedrockModelBones.OFFHAND_ITEM_ON_LEFT, _part -> gunsmithlib$extension.offhandItemOnLeftSideRenderer);
        setFunctionalRenderer(SpecialBedrockModelBones.OFFHAND_ITEM_ON_RIGHT, _part -> gunsmithlib$extension.offhandItemOnRightSideRenderer);
    }

    public MixinBedrockGunModel(BedrockModelPOJO pojo, BedrockVersion version) {
        super(pojo, version);
    }
}
