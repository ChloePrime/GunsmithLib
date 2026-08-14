package mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment;

import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * @since 3.4.0
 */
@SuppressWarnings("unused")
public class GunsmithLibAttachmentDataExtension extends GunsmithLibSharedDataExtension {
    /**
     * 枪盾数据
     *
     * @since 3.4.0
     */
    @GunpackProperty
    private ShieldData shield;

    public ShieldData getShieldData() {
        return shield;
    }

    // 下面是代码

    public static Optional<GunsmithLibAttachmentDataExtension> of(ItemStack stack) {
        return Gunsmith.getAttachmentInfo(stack).flatMap(GunsmithLibAttachmentDataExtension::of);
    }

    public static Optional<GunsmithLibAttachmentDataExtension> of(AttachmentInfo attachInfo) {
        return of(attachInfo.index().getData());
    }

    /**
     * @since 6.5
     */
    public static Optional<GunsmithLibAttachmentDataExtension> of(AttachmentData attachmentData) {
        return ((EnhancedAttachmentData) attachmentData).gunsmith$getGunsmithLibExtension();
    }
}
