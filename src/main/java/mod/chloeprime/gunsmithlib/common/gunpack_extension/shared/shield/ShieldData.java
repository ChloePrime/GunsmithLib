package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield;

import com.google.common.base.MoreObjects;
import com.google.gson.annotations.SerializedName;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.GunsmithLibAttachmentDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @since 3.4.0
 */
public class ShieldData {
    /**
     * 格挡原版伤害的范围，单位为角度°。<p>
     * 原版盾牌的格挡范围为 180°
     */
    @GunpackProperty
    private double block_vanilla_damage_angle = 0;

    /**
     * 格挡子弹的范围，单位为角度（°）
     */
    @GunpackProperty
    private double block_bullet_damage_angle = 0;

    @SuppressWarnings("unused")
    public enum Condition {
        /**
         * 瞄准时可以格挡伤害。
         */
        @SerializedName("when_aiming")
        WHEN_AIMING,
        /**
         * 没有瞄准时格挡伤害。
         */
        @SerializedName("when_not_aiming")
        WHEN_NOT_AIMING,
        /**
         * 总是能格挡伤害。<p>
         * 有点超模，慎用
         */
        @SerializedName("always")
        ALWAYS
    }

    /**
     * 该枪盾什么时候可以格挡伤害
     */
    @GunpackProperty
    private @Nullable Condition condition = Condition.WHEN_AIMING;

    /**
     * 是否在装弹时停用枪盾，无论 {@link #condition} 如何。
     */
    @GunpackProperty
    private boolean disable_shield_when_reloading = true;

    /**
     * 若为 true，则该枪盾无法被斧头破盾
     * @since 3.6.0
     */
    @GunpackProperty
    private boolean undisableable_by_axes;

    // 下面是代码

    /**
     * 格挡原版伤害的单侧范围（总范围 / 2），
     * 单位为弧度。
     */
    public double blockVanillaDamageAngle() {
        return Math.toRadians(block_vanilla_damage_angle / 2);
    }

    /**
     * 格挡子弹的单侧范围（总范围 / 2），
     * 单位为弧度。
     */
    public double blockBulletDamageAngle() {
        return Math.toRadians(block_bullet_damage_angle / 2);
    }

    public Condition getCondition() {
        return MoreObjects.firstNonNull(condition, Condition.WHEN_AIMING);
    }

    public boolean disableShieldWhenReloading() {
        return disable_shield_when_reloading;
    }

    public boolean canBeDisabledByAxes() {
        return !undisableable_by_axes;
    }

    public static Optional<ShieldData> fromGun(ItemStack stack) {
        return Gunsmith.getGunInfo(stack).flatMap(ShieldData::fromGun);
    }

    public static Optional<ShieldData> fromGun(GunInfo gunInfo) {
        return ((EnhancedGunData) gunInfo.index().getGunData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibGunDataExtension::getShieldData);
    }

    public static Optional<ShieldData> fromAttachment(ItemStack stack) {
        return Gunsmith.getAttachmentInfo(stack).flatMap(ShieldData::fromAttachment);
    }

    public static Optional<ShieldData> fromAttachment(AttachmentInfo gunInfo) {
        return ((EnhancedAttachmentData) gunInfo.index().getData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibAttachmentDataExtension::getShieldData);
    }

}
