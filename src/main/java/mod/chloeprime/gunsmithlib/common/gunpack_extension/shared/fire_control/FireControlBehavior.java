package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;
import java.util.OptionalDouble;

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
            // 让高速子弹直接指向目标
            var fixedTargetPos = fixTargetPos(bulletPos, aimResult, bulletSpeed);
            var newBulletMotion = fixedTargetPos.subtract(bulletPos).normalize().scale(bulletSpeed);
            bullet.setDeltaMovement(newBulletMotion);
        } else {
            // 让低速子弹缓慢转向目标
            HomingProjectileBehavior.onBulletCreate(event.getShooter(), bullet, torque.getAsDouble(), fcData.get().getTorqueLerpRate(), aimResult.entity());
        }
    }

    private static Vec3 fixTargetPos(Vec3 bulletPos, TargetSearcher.SearchResult result, double bulletSpeed) {
        if (bulletSpeed <= 1e-3) {
            return result.pos();
        }
        var hitTime = Math.floor(result.pos().subtract(bulletPos).length() / bulletSpeed) - 1;
        var enemyMotion = result.entity() instanceof FlyingMob || result.entity().isNoGravity()
                ? result.entity().getDeltaMovement()
                : result.entity().getDeltaMovement().with(Direction.Axis.Y, 0);
        return result.pos().add(enemyMotion.scale(hitTime));
    }
}
