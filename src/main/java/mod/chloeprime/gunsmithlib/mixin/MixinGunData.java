package mod.chloeprime.gunsmithlib.mixin;

import com.google.gson.annotations.SerializedName;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.common.MiscAttributeAdapter;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.OldFireControlData;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(value = GunData.class, remap = false)
public class MixinGunData implements EnhancedGunData {
    @ModifyExpressionValue(
            method = "getShootInterval",
            at = @At(value = "INVOKE", target = "Lcom/tacz/guns/resource/modifier/AttachmentCacheProperty;getCache(Ljava/lang/String;)Ljava/lang/Object;")
    )
    private Object adjustRpmByAttribute(Object baseRpm, LivingEntity shooter, FireMode fireMode) {
        return (int) MiscAttributeAdapter.rpm(shooter);
    }

    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique @Nullable GunsmithLibGunDataExtension gunsmith$extension;

    @Deprecated
    @SuppressWarnings("unused")
    @SerializedName("tacz_fire_control_extension:fire_control_system")
    private @Unique @Nullable OldFireControlData gunsmith$oldFireControlData;

    @Override
    public Optional<GunsmithLibGunDataExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }

    @Override
    @Deprecated
    public Optional<OldFireControlData> gunsmith$getOldFireControlSystemData() {
        return Optional.ofNullable(gunsmith$oldFireControlData);
    }
}
