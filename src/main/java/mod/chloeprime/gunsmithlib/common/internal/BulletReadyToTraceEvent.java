package mod.chloeprime.gunsmithlib.common.internal;

import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nonnull;

public class BulletReadyToTraceEvent extends EntityEvent {
    private final Projectile bullet;
    private final Vec3 start, end;

    @ApiStatus.Internal
    public BulletReadyToTraceEvent(Projectile bullet, Vec3 start, Vec3 end) {
        super(bullet);
        this.bullet = bullet;
        this.start = start;
        this.end = end;
    }

    @Override
    public @Nonnull Projectile getEntity() {
        return bullet;
    }

    public Vec3 getStartPos() {
        return start;
    }

    public Vec3 getEndPos() {
        return end;
    }

    @ApiStatus.Internal
    public static void onBulletTick(Projectile bullet, int pierce) {
        if (bullet.level().isClientSide()) {
            return;
        }

        var velocity = bullet.getDeltaMovement();
        if (velocity.lengthSqr() <= 1e-6) {
            return;
        }
        var direction = velocity.normalize();
        var estimated = Rangefinder.clip(bullet, bullet.position(), direction, pierce, velocity.length());

        var start = bullet.position();
        var end = start.add(estimated.asHitResult().getType() == HitResult.Type.MISS
                ? velocity
                : direction.scale(estimated.getLength()));
        var event = new BulletReadyToTraceEvent(bullet, start, end);
        NeoForge.EVENT_BUS.post(event);
    }
}
