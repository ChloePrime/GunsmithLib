package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.network.S2CSyncLockedTarget;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.PacketDistributor;
import org.joml.Vector3d;

import java.util.Optional;
import java.util.UUID;

public class HomingProjectileBehavior {
    private static final String PDKEY_PREFIX = GunsmithLib.loc("homing.").toString();
    public static final String PDKEY_ENABLED = PDKEY_PREFIX + "enabled";
    public static final String PDKEY_TORQUE = PDKEY_PREFIX + "torque";
    public static final String PDKEY_TARGET = PDKEY_PREFIX + "target";

    public static void onBulletCreate(Entity shooter, Projectile bullet, double torque, Entity target) {
        bullet.getPersistentData().putBoolean(PDKEY_ENABLED, true);
        bullet.getPersistentData().putDouble(PDKEY_TORQUE, torque);
        if (!bullet.level().isClientSide) {
            bullet.getPersistentData().putUUID(PDKEY_TARGET, target.getUUID());
            PacketDistributor.sendToPlayersTrackingEntityAndSelf(shooter, new S2CSyncLockedTarget(bullet.getId(), target.getId()));
        }
    }

    public static void onBulletTick(Projectile bullet) {
        var data = bullet.getPersistentData();
        if (!data.getBoolean(PDKEY_ENABLED)) {
            return;
        }
        var torque = data.getDouble(PDKEY_TORQUE);
        if (torque <= 1e-4) {
            return;
        }
        var oldVelocity = bullet.getDeltaMovement();
        var oldSpeed = oldVelocity.length();
        if (oldSpeed <= 1e-4) {
            return;
        }
        var target = loadUUIDSafely(data, PDKEY_TARGET)
                .flatMap(uuid -> ClientProxy.getEntityByUuid(bullet.level(), uuid))
                .orElse(null);
        if (target == null || !target.isAlive()) {
            return;
        }
        var oldDirection = oldVelocity.scale(1 / oldSpeed);
        var targetDirection = target.getBoundingBox().getCenter().subtract(bullet.position()).normalize();
        if (targetDirection.lengthSqr() <= 1e-8) {
            return;
        }
        var unitOffset = Math.abs(angleTo(oldDirection, targetDirection));
        if (unitOffset <= 1e-8) {
            return;
        }
        var newDirection = slerp(oldDirection, targetDirection, Math.min(1, Math.toRadians(torque) / unitOffset));
        if (newDirection.lengthSqr() >= 1e-4) {
            Vec3 newVelocity = newDirection.normalize().scale(oldSpeed);
            bullet.setDeltaMovement(newVelocity);
            double x = newVelocity.x;
            double y = newVelocity.y;
            double z = newVelocity.z;
            double distance = newVelocity.horizontalDistance();
            bullet.setYRot((float) Math.toDegrees(Mth.atan2(x, z)));
            bullet.setXRot((float) Math.toDegrees(Mth.atan2(y, distance)));
        }
    }

    /**
     * modified from <a href="https://github.com/godotengine/godot/blob/master/core/math/vector3.h#L239">Godot's implementation</a>
     */
    private static Vec3 slerp(Vec3 start, Vec3 end, double percent) {
        double start_length_sq = start.lengthSqr();
        double end_length_sq = end.lengthSqr();
        if (start_length_sq == 0 || end_length_sq == 0) {
            return start.lerp(end, percent);
        }
        Vec3 axis = start.cross(end);
        double axis_length_sq = axis.lengthSqr();
        if (axis_length_sq == 0) {
            return start.lerp(end, percent);
        }
        axis = axis.scale(1 / Math.sqrt(axis_length_sq));
        double start_length = Math.sqrt(start_length_sq);
        double result_length = Mth.lerp(percent, start_length, Math.sqrt(end_length_sq));
        double angle = angleTo(start, end);
        var result = new Vector3d(start.x(), start.y(), start.z())
                .rotateAxis(angle * percent, axis.x(), axis.y(), axis.z())
                .mul(result_length / start_length);
        return new Vec3(result.x(), result.y(), result.z());
    }

    private static double angleTo(Vec3 start, Vec3 end) {
        return Math.atan2(start.cross(end).length(), start.dot(end));
    }

    @SuppressWarnings("SameParameterValue")
    private static Optional<UUID> loadUUIDSafely(CompoundTag compound, String key) {
        if (!compound.hasUUID(key)) {
            return Optional.empty();
        }
        try {
            return Optional.of(compound.getUUID(key));
        } catch (IllegalArgumentException exception) {
            return Optional.empty();
        }
    }
}
