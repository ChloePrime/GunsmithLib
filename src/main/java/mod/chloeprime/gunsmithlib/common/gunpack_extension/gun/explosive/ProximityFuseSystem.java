package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.internal.BulletReadyToTraceEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;
import java.util.function.Predicate;

import static java.lang.Math.*;

@EventBusSubscriber
public class ProximityFuseSystem {
    public static final int MAX_CAST_RESOLUTION = 64;
    public static final String PDK_PROX_DISTANCE = GunsmithLib.loc("proximity_fuse_distance").toString();

    @SubscribeEvent
    public static void onBulletCreate(InternalBulletCreateEvent eventWrapper) {
        var event = eventWrapper.getImpl();
        var data = GunExplosiveData.fromGun(event.getGunInfo()).orElse(null);
        if (data == null || data.getProximityFuseDistance() <= 0) {
            return;
        }
        event.getBullet().getPersistentData().putDouble(PDK_PROX_DISTANCE, data.getProximityFuseDistance());
    }

    @SubscribeEvent
    public static void onBulletReadyToTrace(BulletReadyToTraceEvent event) {
        var bullet = event.getEntity();

        if (bullet.level().isClientSide || !bullet.isAlive() || !(bullet instanceof EntityKineticBulletAccessor accessor)) {
            return;
        }

        var posBefore = event.getStartPos();
        var posAfter = event.getEndPos();

        var distance = bullet.getPersistentData().getDouble(PDK_PROX_DISTANCE);
        if (distance <= 0) {
            return;
        }
        @Nullable Entity shooter = bullet.getOwner();
        var bulletBB = bullet.getBoundingBox();
        var entityTest = (Predicate<Entity>) et -> testEntity(et, shooter);

        int slices = max(1, (int) ceil(posBefore.distanceTo(posAfter) / 0.5));
        for (int i = 0; i < slices; i++) {
            var rayCastStart = posBefore.lerp(posAfter, (double) (i + 1) / slices);
            var aabb = AABB.ofSize(rayCastStart, bulletBB.getXsize(), bulletBB.getYsize(), bulletBB.getZsize()).inflate(distance + 4);
            if (sphericalTrace(bullet, rayCastStart, distance, aabb, entityTest)) {
                GsHelper.syncBulletExplodePos(bullet, rayCastStart);
                accessor.setExplosionDelayCount(0);
                return;
            }
        }
    }

    private static boolean sphericalTrace(Projectile bullet, Vec3 center, double distance, AABB aabb, Predicate<Entity> entityTest) {
        int resolution = Mth.clamp((int) ceil(8 * distance), 1, MAX_CAST_RESOLUTION);
        for (int rx = 0; rx < resolution; rx++) {
            var theta = 2 * PI * rx / resolution;
            var sinTheta = Math.sin(theta);
            var cosTheta = Math.cos(theta);
            for (int ry = 0; ry < resolution; ry++) {
                var phi = 2 * PI * ry / resolution;
                var x = distance * sinTheta * cos(phi);
                var y = distance * sinTheta * sin(phi);
                var z = distance * cosTheta;
                var end = center.add(new Vec3(x, y, z).scale(distance));
                var hit = ProjectileUtil.getEntityHitResult(bullet.level(), bullet, center, end, aabb, entityTest, 0);
                if (hit != null && hit.getType() != HitResult.Type.MISS) {
                    return true;
                }
            }
        }
        return false;
    }

    private static final TargetingConditions FOR_COMBAT = TargetingConditions.forCombat();

    private static boolean testEntity(Entity candidate, @Nullable Entity shooter) {
        if (shooter instanceof LivingEntity gunner && candidate instanceof LivingEntity victim) {
            return gunner.canAttack(victim, FOR_COMBAT);
        } else {
            return candidate instanceof Enemy;
        }
    }
}
