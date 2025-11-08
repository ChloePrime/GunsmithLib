package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * @see Gunsmith#getAttachmentInfo(ItemStack)
 */
public record AttachmentInfo(
        ItemStack attachmentStack,
        IAttachment attachmentItem,
        ResourceLocation attachmentId,
        CommonAttachmentIndex index
) {
    /**
     * @since 4.12.0
     */
    public static Optional<AttachmentInfo> of(ItemStack gun) {
        return Gunsmith.getAttachmentInfo(gun);
    }
}
