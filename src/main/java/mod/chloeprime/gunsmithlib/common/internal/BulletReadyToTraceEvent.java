package mod.chloeprime.gunsmithlib.common.internal;

import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.Vec3;
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
}
