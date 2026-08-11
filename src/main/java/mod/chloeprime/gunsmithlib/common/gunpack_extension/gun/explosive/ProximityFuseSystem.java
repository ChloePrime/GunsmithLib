package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import com.google.common.base.Predicates;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitEntityEvent;
import mod.chloeprime.gunsmithlib.api.common.GunsmithLibGunProperties;
import mod.chloeprime.gunsmithlib.common.internal.AmmoHitAnythingEventPoster;
import mod.chloeprime.gunsmithlib.common.internal.BulletReadyToTraceEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Math.*;

@Mod.EventBusSubscriber
public class ProximityFuseSystem {
    public static final int MAX_CAST_RESOLUTION = 64;
    public static final String PDK_PROX_DISTANCE = GunsmithLib.loc("proximity_fuse_distance").toString();
    public static final String PDK_AFFECTED_SAFETY_DISTANCE = GunsmithLib.loc("affected_by_safety_distance").toString();

    @SubscribeEvent
    public static void onBulletCreate(InternalBulletCreateEvent eventWrapper) {
        var event = eventWrapper.getImpl();
        var data = GunExplosiveData.fromGun(event.getGunInfo()).orElse(null);
        var dataDistance = data == null ? 0 : data.getProximityFuseDistance();
        double distance = GsHelper.modifyProperty(
                event.getGunInfo(), event.getShooter(),
                GunsmithLibGunProperties.PROXIMITY_FUSE_DISTANCE, Double.class, dataDistance);
        if (distance <= 0) {
            return;
        }
        var safety = data != null && data.hasSafetyDistanceFlag(SafetyDistanceFlags.PREVENTS_PROXIMITY_FUSE);
        event.getBullet().getPersistentData().putDouble(PDK_PROX_DISTANCE, distance);
        event.getBullet().getPersistentData().putBoolean(PDK_AFFECTED_SAFETY_DISTANCE, safety);
    }

    @SubscribeEvent
    public static void onBulletReadyToTrace(BulletReadyToTraceEvent event) {
        if (event.getSide().isClient()) {
            return;
        }
        var bullet = event.getEntity();
        if (!bullet.isAlive() || !(bullet instanceof EntityKineticBulletAccessor accessor)) {
            return;
        }
        var scanRange = bullet.getPersistentData().getDouble(PDK_PROX_DISTANCE);
        if (scanRange <= 0) {
            return;
        }

        var posBefore = event.getStartPos();
        var posAfter = event.getEndPos();
        var level = bullet.level();
        if (!isStrongLoaded(level, posBefore)) {
            return;
        }

        var deltaPosSqr = posBefore.distanceToSqr(posAfter);
        // 检查安全距离
        var safetyBudget = SafetyDistanceSystem.getSafetyDistanceOf(bullet) - accessor.gunsmithlib$getMovedDistance();
        var safetyBudgetSqr = Math.copySign(safetyBudget * safetyBudget, safetyBudget);
        // 位移终点依旧在安全距离内，这一次位移不可能触发近炸
        if (safetyBudgetSqr > deltaPosSqr) {
            return;
        }

        @Nullable Entity shooter = bullet.getOwner();
        var bulletBB = bullet.getBoundingBox();
        var entityTest = (Predicate<Entity>) et -> testEntity(et, shooter);
        var front = posAfter.subtract(posBefore).normalize();
        var frontChunkLoadProbeOffset = front.scale(scanRange + 4);

        int slices = max(1, (int) ceil(posBefore.distanceTo(posAfter) / scanRange) * 2);
        for (int i = 0; i < slices; i++) {
            var delta = (double) (i + 1) / slices;
            // 安全距离内不检测
            if (deltaPosSqr * delta * delta < safetyBudgetSqr) {
                continue;
            }
            var rayCastStart = posBefore.lerp(posAfter, delta);
            // 末端超出加载距离时停止检测
            if (!isStrongLoaded(level, rayCastStart.add(frontChunkLoadProbeOffset))) {
                return;
            }
            var aabb = AABB.ofSize(rayCastStart, bulletBB.getXsize(), bulletBB.getYsize(), bulletBB.getZsize()).inflate(scanRange + 4);
            // 使用 AABB 进行粗略探测，如果所选范围内没有实体则跳过球形追踪
            var anyHitCullBuffer = ROUGH_CULL_BUFFER.get();
            try {
                level.getEntities(EntityTypeTest.forClass(LivingEntity.class), aabb, Predicates.alwaysTrue(), anyHitCullBuffer);
                if (anyHitCullBuffer.isEmpty()) {
                    continue;
                }
            } finally {
                anyHitCullBuffer.clear();
            }
            // 执行球形追踪
            var hit = sphericalTrace(bullet, rayCastStart, scanRange, aabb, entityTest).orElse(null);
            if (hit != null) {
                AmmoHitEntityEvent hitEntityEvent;
                boolean canceled;
                // 发布 AmmoHitEntityEvent 事件以触发命中粒子效果
                if (bullet instanceof EntityKineticBullet ekb) {
                    hitEntityEvent = new AmmoHitEntityEvent(level, hit, hit.getEntity(), ekb, false);
                    canceled = AmmoHitAnythingEventPoster.entityPre(hitEntityEvent).isCanceled();
                } else {
                    hitEntityEvent = null;
                    canceled = false;
                }
                if (canceled) {
                    return;
                }
                // 爆炸！
                GsHelper.syncBulletExplodePos(bullet, rayCastStart);
                accessor.setExplosionDelayCount(0);
                // 防止爆炸粒子放两遍
                AmmoHitAnythingEventPoster.exemptFromSelfExplodeEvent(bullet);
                if (hitEntityEvent != null) {
                    AmmoHitAnythingEventPoster.entityPost(hitEntityEvent);
                }
                return;
            }
        }
    }

    private static final ThreadLocal<List<Entity>> ROUGH_CULL_BUFFER = ThreadLocal.withInitial(ArrayList::new);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isStrongLoaded(Level level, Vec3 pos) {
        var intPos = BlockPos.containing(pos);
        if (level instanceof ServerLevel srvLevel) {
            return srvLevel.isPositionEntityTicking(intPos);
        } else {
            throw new IllegalArgumentException(level.getClass().getSimpleName());
        }
    }

    private static Optional<EntityHitResult> sphericalTrace(Projectile bullet, Vec3 center, double distance, AABB aabb, Predicate<Entity> entityTest) {
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
                var hit = ProjectileUtil.getEntityHitResult(bullet, center, end, aabb, entityTest, 0);
                if (hit != null && hit.getType() != HitResult.Type.MISS) {
                    return Optional.of(hit);
                }
            }
        }
        return Optional.empty();
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
