package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.common.GunsmithLibGunProperties;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;

/**
 * @since 6.2
 */
@EventBusSubscriber
public final class SafetyDistanceSystem {
    public static final String PDK_SAFETY_DISTANCE = GunsmithLib.loc("safety_distance").toString();
    public static final String PDK_SAFE_EXPLODE = GunsmithLib.loc("safe_explode").toString();

    /**
     * 检测给定的距离是否处于安全距离外。
     *
     * @return 给定的距离是否处于安全距离外。
     */
    @SuppressWarnings("unused")
    public static boolean isOutSafeDistance(GunInfo gun, double distance, SafetyDistanceFlag flag, @Nullable LivingEntity shooter) {
        return !isInSafeDistance(gun, distance, flag, shooter);
    }

    /**
     * 检测给定的距离是否处于安全距离内。
     *
     * @return 给定的距离是否处于安全距离内。
     */
    @SuppressWarnings("unused")
    public static boolean isInSafeDistance(GunInfo gun, double distance, SafetyDistanceFlag flag, @Nullable LivingEntity shooter) {
        return checkSafetyDistance(gun, distance, flag, shooter) > 0;
    }

    /**
     * 检测安全距离。
     *
     * @return 给定的距离是处于安全内，如果是则返回安全距离，否则返回 {@code 0}
     */
    public static double checkSafetyDistance(GunInfo gun, double distance, SafetyDistanceFlag flag, @Nullable LivingEntity shooter) {
        return GunExplosiveData.fromGun(gun)
                .filter(ged -> ged.hasSafetyDistanceFlag(flag))
                .map(ged -> computeModifiedSafetyDistance(gun, ged, shooter))
                .filter(safeDistance -> distance < safeDistance)
                .orElse(0.0);
    }

    /**
     * 获取安全距离。
     *
     * @return 这把武器配置的安全距离，默认值为 {@code 0.0}
     */
    public static double getSafetyDistance(GunInfo gun, @Nullable LivingEntity shooter) {
        return GunExplosiveData.fromGun(gun)
                .map(ged -> computeModifiedSafetyDistance(gun, ged, shooter))
                .orElse(0.0);
    }

    public static double getSafetyDistanceOf(Entity bullet) {
        return bullet.getPersistentData().getDouble(PDK_SAFETY_DISTANCE);
    }

    public static boolean isSafetyDistanceAffectingVanillaExplosion(Entity bullet) {
        return bullet.getPersistentData().getBoolean(PDK_SAFE_EXPLODE);
    }

    public static boolean isVanillaExplosionInSafeDistance(Entity bullet, Vec3 hitPos) {
        if (!(bullet instanceof EnhancedKineticBullet accessor)) {
            return false;
        }
        if (!isSafetyDistanceAffectingVanillaExplosion(bullet)) {
            return false;
        }
        var safetyBudget = SafetyDistanceSystem.getSafetyDistanceOf(bullet) - accessor.gunsmithlib$getMovedDistance();
        var safetyBudgetSqr = Math.copySign(safetyBudget * safetyBudget, safetyBudget);
        return bullet.position().distanceToSqr(hitPos) < safetyBudgetSqr;
    }

    @SubscribeEvent
    private static void onBulletCreate(BulletCreateEvent event) {
        var pd = event.getBullet().getPersistentData();
        var distance = getSafetyDistance(event.getGunInfo(), event.getShooter());
        if (distance > 0) {
            pd.putDouble(PDK_SAFETY_DISTANCE, distance);
        }
        if (event.getBullet() instanceof EnhancedKineticBullet accessor && accessor.isExplosion()) {
            var safelyExplode = GunExplosiveData.fromGun(event.getGun())
                    .filter(ged -> ged.hasSafetyDistanceFlag(SafetyDistanceFlags.PREVENTS_EXPLOSION))
                    .isPresent();
            if (safelyExplode) {
                pd.putBoolean(PDK_SAFE_EXPLODE, true);
            }
        }
    }

    private static double computeModifiedSafetyDistance(GunInfo gun, GunExplosiveData explosive, @Nullable LivingEntity shooter) {
        if (shooter == null) {
            return explosive.getSafetyDistance();
        }
        return GsHelper.modifyProperty(gun, shooter, GunsmithLibGunProperties.SAFETY_DISTANCE, Double.class, explosive.getSafetyDistance());
    }

    private SafetyDistanceSystem() {
    }
}
