package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.google.gson.annotations.SerializedName;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.GunsmithLibAttachmentDataExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import javax.annotation.Nullable;
import java.util.Optional;

@Mixin(AttachmentData.class)
public class MixinAttachmentData implements EnhancedAttachmentData {
    @SuppressWarnings("unused")
    @SerializedName("gunsmithlib_extension")
    private @Unique
    @Nullable GunsmithLibAttachmentDataExtension gunsmith$extension;

    @Override
    public Optional<GunsmithLibAttachmentDataExtension> gunsmith$getGunsmithLibExtension() {
        return Optional.ofNullable(gunsmith$extension);
    }
}
