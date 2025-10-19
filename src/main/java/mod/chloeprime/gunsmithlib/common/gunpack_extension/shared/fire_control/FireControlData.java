package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
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
    @GunpackProperty
    private double angular_range = 0;

    /**
     * 覆盖默认锁定距离。<p>
     * 若为小于 0 的值（默认值 -1 就是）则使用默认锁定距离，即武器的有效射程
     */
    @GunpackProperty
    private double range_override = -1;

    /**
     * 扭矩。<p>
     * 为空时子弹将在射出时指向目标。适用于高速动能子弹。<p>
     * 不为空时，子弹将在运动过程中逐渐指向目标。适用于导弹/毒刺/标枪等武器。
     */
    @GunpackProperty
    private @Nullable Double torque = null;

    /**
     * 强扭矩。
     * <p>
     * 强扭矩会随着角度差给制导系统带来额外扭矩。
     * 强扭矩固定（且大于 0）时，导弹运动方向和目标方向的角度差越大，
     * 强扭矩带来的额外扭矩就绝大，且和角度差成正比。
     * <p>
     * 基础扭矩为 0 时不生效
     *
     * @since 4.8.0
     */
    @GunpackProperty
    private double torque_lerp_rate = 0.0;

    public static Optional<FireControlData> fromGun(ItemStack stack) {
        return Gunsmith.getGunInfo(stack).flatMap(FireControlData::fromGun);
    }

    public static Optional<FireControlData> fromGun(GunInfo gun) {
        return ((EnhancedGunData) gun.index().getGunData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibGunDataExtension::getFireControlData);
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

    public double getTorqueLerpRate() {
        return torque_lerp_rate;
    }
}
