package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import cn.chloeprime.commons.math.LinearAlgebraTypes;
import com.tacz.guns.util.HitboxHelper;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.EntityStates;
import mod.chloeprime.gunsmithlib.common.util.MojangRandomGenerator;
import mod.chloeprime.gunsmithlib.mixin.EntityAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.random.RandomGenerator;

/**
 * @since 6.0.0
 */
public class EntityStatesImpl implements EntityStates {
    private final Entity entity;

    public EntityStatesImpl(Entity entity) {
        Objects.requireNonNull(entity);
        this.entity = entity;
    }

    @Override
    public Vector3d position() {
        return LinearAlgebraTypes.moj2joml(entity.position());
    }

    @Override
    public Vector3d eye_position() {
        return LinearAlgebraTypes.moj2joml(entity.getEyePosition());
    }

    @Override
    public Vector3d center_position() {
        return LinearAlgebraTypes.moj2joml(entity.getBoundingBox().getCenter());
    }

    @Override
    public Vector2f rotation() {
        return rotation_degrees().mul(Mth.PI / 180);
    }

    @Override
    public Vector2f rotation_degrees() {
        return new Vector2f(entity.getXRot(), entity.getYRot());
    }

    @Override
    public Vector3d velocity_per_tick() {
        return LinearAlgebraTypes.moj2joml(entity.getDeltaMovement());
    }

    @Override
    public Vector3d velocity_per_second() {
        return velocity_per_tick().mul(20);
    }

    @Override
    public Vector3d get_look_direction() {
        return LinearAlgebraTypes.moj2joml(entity.getLookAngle());
    }

    @Override
    public RandomGenerator get_random_generator() {
        return new MojangRandomGenerator(((EntityAccessor) entity).getRandom());
    }

    @Override
    public boolean is_alive() {
        return entity.isAlive();
    }

    @Override
    public boolean is_removed() {
        return entity.isRemoved();
    }

    @Override
    public boolean is_on_ground() {
        return entity.onGround();
    }

    @Override
    public boolean is_silent() {
        return entity.isSilent();
    }

    @Override
    public boolean is_affected_by_gravity() {
        return !entity.isNoGravity();
    }

    @Override
    public boolean is_fire_immune() {
        return entity.fireImmune();
    }

    @Override
    public boolean is_in_water() {
        return entity.isInWater();
    }

    @Override
    public boolean is_in_rain() {
        return ((EntityAccessor) entity).invokeIsInRain();
    }

    @Override
    public boolean is_in_bubble() {
        return ((EntityAccessor) entity).invokeIsInBubbleColumn();
    }

    @Override
    public boolean is_in_water_or_rain() {
        return entity.isInWaterOrRain();
    }

    @Override
    public boolean is_in_water_or_bubble() {
        return entity.isInWaterOrBubble();
    }

    @Override
    public boolean is_in_water_or_rain_or_bubble() {
        return entity.isInWaterRainOrBubble();
    }

    @Override
    public boolean is_under_water() {
        return entity.isUnderWater();
    }

    @Override
    public boolean is_in_lava() {
        return entity.isInLava();
    }

    @Override
    public boolean is_moving() {
        double distance = Math.abs(entity.walkDist - entity.walkDistO);
        if (entity instanceof Player player) {
            distance = HitboxHelper.getPlayerVelocity(player).length();
        }
        return distance > 0.05;
    }

    @Override
    public boolean is_sprinting() {
        return entity.isSprinting();
    }

    @Override
    public boolean is_crouching() {
        return entity.isCrouching();
    }

    @Override
    public boolean is_crawling() {
        return !entity.isSwimming() && entity.getPose() == Pose.SWIMMING;
    }

    @Override
    public boolean is_swimming() {
        return entity.isSwimming();
    }

    @Override
    public boolean is_on_fire() {
        return entity.isOnFire();
    }

    @Override
    public long remaining_fire_ticks() {
        return entity.getRemainingFireTicks();
    }

    @Override
    public Pose get_entity_pose_object() {
        return entity.getPose();
    }
}
