package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy.EnergyWeaponData;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

public final class GunsmithLibGunDataExtension extends GunsmithLibSharedDataExtension {
    /**
     * 电池数据，
     * 为非 null 时该武器可以充电。
     *
     * @since 3.3.0
     */
    @GunpackProperty
    private @Nullable EnergyWeaponData battery;

    /**
     * 是否开启过热的视听反馈。
     * 为 true 时，武器过热后会发出滋滋声并冒烟
     *
     * @since 3.3.0
     */
    @GunpackProperty
    private boolean enable_overheat_feedback;

    /**
     * 枪盾数据
     *
     * @since 3.4.0
     */
    @GunpackProperty
    private @Nullable ShieldData shield;

    /**
     * 制导数据
     *
     * @since 3.5.0
     */
    @GunpackProperty
    private @Nullable FireControlData fire_control;

    /**
     * 只在枪械数据中生效的爆炸物扩展
     *
     * @since 4.9.0
     */
    @GunpackProperty
    private @Nullable GunExplosiveData gun_explosive;

    // 下面是代码

    public static Optional<GunsmithLibGunDataExtension> of(ItemStack stack) {
        return Gunsmith.getGunInfo(stack).flatMap(GunsmithLibGunDataExtension::of);
    }

    public static Optional<GunsmithLibGunDataExtension> of(GunInfo gunInfo) {
        return ((EnhancedGunData) gunInfo.index().getGunData()).gunsmith$getGunsmithLibExtension();
    }

    public @Nullable EnergyWeaponData battery() {
        return battery;
    }

    public boolean enableOverheatFeedback() {
        return enable_overheat_feedback;
    }

    public @Nullable ShieldData getShieldData() {
        return shield;
    }

    public @Nullable FireControlData getFireControlData() {
        return fire_control;
    }

    public @Nullable GunExplosiveData getGunExplosiveData() {
        return gun_explosive;
    }
}
