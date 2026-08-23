package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.common.MiscAttributeAdapter;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = AttachmentDataUtils.class, remap = false)
public class MixinAttachmentDataUtils {
    @ModifyReturnValue(method = "getAmmoCountWithAttachment", at = @At("RETURN"))
    private static int ammoCapacityAttribute(int original, ItemStack gunItem, GunData gunData) {
        return MiscAttributeAdapter.ammoCapacity(original, gunItem);
    }
}
