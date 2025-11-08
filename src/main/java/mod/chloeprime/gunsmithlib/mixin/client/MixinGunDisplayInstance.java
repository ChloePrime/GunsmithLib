package mod.chloeprime.gunsmithlib.mixin.client;

import com.google.gson.annotations.SerializedName;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.client.model.BedrockGunModel;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.pojo.display.gun.GunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplayInstance;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(value = GunDisplayInstance.class, remap = false)
public class MixinGunDisplayInstance implements EnhancedGunDisplayInstance {
    // 枪包显示数据扩展

    @Inject(method = "<init>", at = @At("TAIL"))
    private void copyExtensionInfo(GunDisplay display, CallbackInfo ci) {
        gunsmith$extension = ((EnhancedGunDisplay) display).gunsmith$getGunsmithLibExtension().orElse(null);
    }

    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique @Nullable GunsmithLibGunDisplayExtension gunsmith$extension;

    @Override
    public Optional<GunsmithLibGunDisplayExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }

    // 状态机覆盖

    private @Unique GunDisplayInstance gunsmith$override;

    @ModifyReturnValue(method = "getGunModel", at = @At("RETURN"))
    private BedrockGunModel makeModelSameForVariantFamily(BedrockGunModel original) {
        var override = this.gunsmith$override;
        return override != null ? override.getGunModel() : original;
    }

    @ModifyReturnValue(method = "getAnimationStateMachine", at = @At("RETURN"))
    private LuaAnimationStateMachine<GunAnimationStateContext> makeStateMachineSameForVariantFamily(LuaAnimationStateMachine<GunAnimationStateContext> original) {
        var override = this.gunsmith$override;
        return override != null ? override.getAnimationStateMachine() : original;
    }

    @Override
    public void gunsmith$acceptOverride(GunDisplayInstance value) {
        gunsmith$override = value;
    }
}
