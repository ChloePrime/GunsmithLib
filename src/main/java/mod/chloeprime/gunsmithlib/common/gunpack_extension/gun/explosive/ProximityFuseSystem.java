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
import net.minecraft.commands.arguments.EntityAnchorArgument;
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
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Matrix3d;
import org.joml.Matrix3f;
import org.joml.Vector3d;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import static java.lang.Math.*;

@EventBusSubscriber
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

        int slices = max(1, (int) ceil(posBefore.distanceTo(posAfter) / Math.min(0.25, scanRange / 2)));
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
            // 执行圆形追踪
            var traceResult = circleTrace(bullet, rayCastStart, front, scanRange, aabb, entityTest).orElse(null);
            if (traceResult != null) {
                AmmoHitEntityEvent hitEntityEvent;
                boolean canceled;
                // 先同步位置，以让事件计算时子弹处于正确的位置中。
                var bulletPosBackup = bullet.position();
                var movedDistanceBackup = accessor.gunsmithlib$getMovedDistance();
                accessor.gunsmith$onMovedToPos(rayCastStart);
                bullet.setPos(rayCastStart);
                // 发布 AmmoHitEntityEvent 事件以触发命中粒子效果
                if (bullet instanceof EntityKineticBullet ekb) {
                    var rayCastMid = posBefore.lerp(posAfter, delta + 0.5 / slices);
                    var hit = new EntityHitResult(traceResult.getEntity(), rayCastMid);
                    hitEntityEvent = new AmmoHitEntityEvent(level, hit, hit.getEntity(), ekb, false);
                    canceled = AmmoHitAnythingEventPoster.entityPre(hitEntityEvent).isCanceled();
                } else {
                    hitEntityEvent = null;
                    canceled = false;
                }
                if (canceled) {
                    bullet.setPos(bulletPosBackup);
                    accessor.gunsmithlib$setMovedDistance(movedDistanceBackup);
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
    private static final ThreadLocal<Matrix3f> MODEL_MATRIX_F_BUFFER = ThreadLocal.withInitial(Matrix3f::new);
    private static final ThreadLocal<Matrix3d> MODEL_MATRIX_D_BUFFER = ThreadLocal.withInitial(Matrix3d::new);
    private static final ThreadLocal<Vector3d> LOCAL_POS_BUFFER = ThreadLocal.withInitial(Vector3d::new);

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private static boolean isStrongLoaded(Level level, Vec3 pos) {
        var intPos = BlockPos.containing(pos);
        if (level instanceof ServerLevel srvLevel) {
            return srvLevel.isPositionEntityTicking(intPos);
        } else {
            throw new IllegalArgumentException(level.getClass().getSimpleName());
        }
    }

    private static Optional<EntityHitResult> circleTrace(Projectile bullet, Vec3 center, Vec3 front, double distance, AABB aabb, Predicate<Entity> entityTest) {
        bullet.lookAt(EntityAnchorArgument.Anchor.FEET, bullet.position().add(bullet.getDeltaMovement()));
        int resolution = Mth.clamp((int) ceil(8 * distance), 1, MAX_CAST_RESOLUTION);
        var modelMatrixF = GsHelper.getModelMatrix(bullet.getYRot(), front, MODEL_MATRIX_F_BUFFER.get());
        var modelMatrixD = MODEL_MATRIX_D_BUFFER.get().set(modelMatrixF);
        var pos = LOCAL_POS_BUFFER.get();
        for (int i = 0; i < resolution; i++) {
            var angle = 2 * PI * i / resolution;
            pos.x = cos(angle);
            pos.y = sin(angle);
            pos.z = 0;
            pos.mul(modelMatrixD);
            pos.mul(distance);
            var end = center.add(pos.x(), pos.y(), pos.z());
            var hit = ProjectileUtil.getEntityHitResult(bullet, center, end, aabb, entityTest, 0);
            if (hit != null && hit.getType() != HitResult.Type.MISS) {
                return Optional.of(hit);
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
