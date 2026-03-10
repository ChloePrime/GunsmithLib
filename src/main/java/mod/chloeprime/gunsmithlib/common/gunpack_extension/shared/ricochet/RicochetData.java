package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.ricochet;

import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * 跳弹功能
 *
 * @since 5.2.0
 */
public class RicochetData {
    /**
     * 最小入射角，单位为角度。
     * 如果命中方块时入射角小于这个数值，则不会触发跳弹。
     */
    @GunpackProperty
    private double min_angle_of_incidence;

    /**
     * 最大跳弹次数。
     * 跳弹超过这个次数则必定爆炸。
     */
    @GunpackProperty
    private int max_ricochet_times = 1;

    /**
     * 第一次跳弹后对子弹重力的乘数，
     * 用于解决默认重力在跳弹后看着过大的问题。
     */
    @GunpackProperty
    private double gravity_scale = 1;

    /**
     * 最小弹力（0° 垂直入射时的弹力）
     */
    @GunpackProperty
    private double min_bounciness = 0.25;

    /**
     * 最大弹力（90° 入射角时的理论弹力）
     */
    @GunpackProperty
    private double max_bounciness = 1;

    // 下面是代码

    public static final float DEFAULT_MATERIAL_BOUNCINESS = 0.5F;

    /**
     * 单位为弧度
     * @return 最小入射角，单位为弧度
     */
    public final double getMinAngleOfIncidence() {
        return Math.toRadians(min_angle_of_incidence);
    }

    public final int getMaxRicochetTimes() {
        return max_ricochet_times;
    }

    public final double getGravityScale() {
        return gravity_scale;
    }

    public final double getMinBounciness() {
        return min_bounciness;
    }

    public final double getMaxBounciness() {
        return max_bounciness;
    }

    @SuppressWarnings("OptionalIsPresent")
    public static Optional<RicochetData> of(ItemStack gun) {
        var gunInfo = Gunsmith.getGunInfo(gun).orElse(null);
        if (gunInfo == null) {
            return Optional.empty();
        }
        var onGun = GunsmithLibSharedDataExtension
                .forGun(gunInfo)
                .map(GunsmithLibSharedDataExtension::getRicochetData);
        if (onGun.isPresent()) {
            return onGun;
        }
        var onAmmo = Gunsmith.getAmmoInfo(Gunsmith.createAmmoItemFromId(gunInfo.index().getGunData().getAmmoId()))
                .flatMap(GunsmithLibSharedDataExtension::forAmmo)
                .map(GunsmithLibSharedDataExtension::getRicochetData);
        if (onAmmo.isPresent()) {
            return onAmmo;
        }
        return Optional.empty();
    }
}
