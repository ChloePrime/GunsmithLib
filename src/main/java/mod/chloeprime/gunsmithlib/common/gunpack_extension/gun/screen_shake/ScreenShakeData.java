package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @since 7.0
 */
public class ScreenShakeData {
    /**
     * 开火时对周边实体（包括射手）的屏幕震动配置
     */
    @GunpackProperty
    private @Nullable ScreenShakeEntryData on_shoot;

    /**
     * 弹药命中时对命中目标的屏幕震动配置
     */
    @GunpackProperty
    private @Nullable ScreenShakeEntryData on_hit;

    /**
     * 弹药命中时对周围实体的屏幕震动配置
     */
    @GunpackProperty
    private @Nullable ScreenShakeEntryData on_impact;

    /**
     * 弹药爆炸时的屏幕震动配置
     * 如果为空则使用 {@link #on_impact}。
     */
    @GunpackProperty
    private @Nullable ScreenShakeEntryData on_explosion;

    // ↓ For Java ↓

    public static Optional<ScreenShakeData> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun).flatMap(ScreenShakeData::of);
    }

    public static Optional<ScreenShakeData> of(GunInfo gun) {
        return GunsmithLibGunDataExtension.of(gun).flatMap(GunsmithLibGunDataExtension::getScreenShakeData);
    }

    public Optional<ScreenShakeEntryData> getOnShoot() {
        return Optional.ofNullable(on_shoot);
    }

    public Optional<ScreenShakeEntryData> getOnHit() {
        return Optional.ofNullable(on_hit);
    }

    public Optional<ScreenShakeEntryData> getOnImpact() {
        return Optional.ofNullable(on_impact);
    }

    public Optional<ScreenShakeEntryData> getOnExplosionRaw() {
        return Optional.ofNullable(on_explosion);
    }

    public Optional<ScreenShakeEntryData> getOnExplosion() {
        return getOnExplosionRaw().or(this::getOnImpact);
    }
}
