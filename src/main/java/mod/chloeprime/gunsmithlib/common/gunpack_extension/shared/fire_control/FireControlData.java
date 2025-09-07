package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.GunsmithLibAttachmentDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalDouble;

/**
 * @since 3.5.0
 */
public final class FireControlData {
    /**
     * 锁定角范围
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double angular_range = 0;

    /**
     * 覆盖默认锁定距离。<p>
     * 若为小于 0 的值（默认值 -1 就是）则使用默认锁定距离，即武器的有效射程
     */
    @SuppressWarnings({"FieldMayBeFinal", "FieldCanBeLocal"})
    private double range_override = -1;

    /**
     * 扭矩。<p>
     * 为空时子弹将在射出时指向目标。适用于高速动能子弹。<p>
     * 不为空时，子弹将在运动过程中逐渐指向目标。适用于导弹/毒刺/标枪等武器。
     */
    @SuppressWarnings("FieldMayBeFinal")
    private @Nullable Double torque = null;

    public static Optional<FireControlData> fromGun(ItemStack stack) {
        return Gunsmith.getGunInfo(stack).flatMap(FireControlData::fromGun);
    }

    public static Optional<FireControlData> fromGun(GunInfo gun) {
          return ((EnhancedGunData) gun.index().getGunData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibGunDataExtension::getFireControlData);
    }

    public static Optional<FireControlData> fromAttachment(ItemStack stack) {
        return Gunsmith.getAttachmentInfo(stack).flatMap(FireControlData::fromAttachment);
    }

    public static Optional<FireControlData> fromAttachment(AttachmentInfo gunInfo) {
        return ((EnhancedAttachmentData) gunInfo.index().getData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibAttachmentDataExtension::getFireControlData);
    }

    public double getAngularRange() {
        return angular_range;
    }

    public double getRangeOverride() {
        return range_override;
    }

    public OptionalDouble getTorque() {
        return torque != null ? OptionalDouble.of(torque) : OptionalDouble.empty();
    }
}
