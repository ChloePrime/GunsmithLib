package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import mod.chloeprime.gunsmithlib.common.internal.BulletReadyToTraceEvent;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;
import java.util.OptionalDouble;
import java.util.WeakHashMap;

@EventBusSubscriber
public class FireControlBehavior {
    @SubscribeEvent
    public static void onBulletCreate(InternalBulletCreateEvent eventWrapper) {
        var event = eventWrapper.getImpl();
        var gun = event.getGunInfo();
        var bullet = event.getBullet();
        var bulletSpeed = bullet.getDeltaMovement().length();
        if (bulletSpeed <= 1e-3) {
            return;
        }
        var aimResult = TargetSearcher.search(event.getShooter(), gun, 1).orElse(null);
        if (aimResult == null) {
            return;
        }

        var bulletPos = bullet.position();
        if (aimResult.pos().distanceToSqr(bulletPos) < 1e-6) {
            return;
        }

        Optional<FireControlData> fcData = FireControlData.fromGun(gun);
        OptionalDouble torque = fcData
                .map(FireControlData::getTorque)
                .orElse(OptionalDouble.empty());

        if (torque.isEmpty()) {
            AIM_RESULTS.get().put(bullet, aimResult);
            if (bullet.level().isClientSide()) {
                return;
            }
            // 让高速子弹直接指向目标
            var fixedTargetPos = fixTargetPos(aimResult, bullet.getOwner());
            var newBulletMotion = fixedTargetPos.subtract(bulletPos).normalize().scale(bulletSpeed);
            updateBulletMotion(bullet, newBulletMotion);
        } else {
            // 让低速子弹缓慢转向目标
            HomingProjectileBehavior.onBulletCreate(event.getShooter(), bullet, torque.getAsDouble(), fcData.get().getTorqueLerpRate(), aimResult.entity());
        }
    }

    private static final ThreadLocal<Map<Entity, TargetSearcher.SearchResult>> AIM_RESULTS = ThreadLocal.withInitial(WeakHashMap::new);

    @SubscribeEvent(priority = EventPriority.HIGH)
    public static void beforeTrace(BulletReadyToTraceEvent event) {
        if (event.getSide().isClient()) {
            return;
        }
        var bullet = event.getEntity();
        var aimResult = AIM_RESULTS.get().get(bullet);
        if (aimResult == null) {
            return;
        }
        var target = aimResult.entity();
        if (!target.isAlive()) {
            return;
        }

        var oldMotion = bullet.getDeltaMovement();
        var speed = oldMotion.length();
        if (speed <= 1e-4) {
            return;
        }

        var bulletPos = bullet.position();
        var newTarget = fixTargetPos(aimResult, bullet.getOwner());
        var newMotion = newTarget.subtract(bulletPos).normalize().scale(speed);
        updateBulletMotion(bullet, newMotion);
    }

    private static void updateBulletMotion(Entity bullet, Vec3 motion) {
        bullet.setDeltaMovement(motion);
        var xz = motion.with(Direction.Axis.Y, 0).length();
        bullet.setYRot((float) Math.toDegrees(Mth.atan2(motion.x(), motion.z())));
        bullet.setXRot((float) Math.toDegrees(Mth.atan2(motion.y(), xz)));
    }

    private static Vec3 fixTargetPos(TargetSearcher.SearchResult result, @Nullable Entity shooter) {
        return result.realtimeHitPosition(shooter);
    }
}
