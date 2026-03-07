package mod.chloeprime.gunsmithlib.api.common;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.ricochet.RicochetData;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.fml.LogicalSide;

import javax.annotation.Nonnull;
import java.util.Objects;

@Cancelable
public class RicochetEvent extends EntityEvent {
    private final @Nonnull Projectile bullet;
    private final @Nonnull Level level;
    private final @Nonnull HitResult hitResult;
    private final @Nonnull Vec3 normal;
    private float targetBounciness = RicochetData.DEFAULT_MATERIAL_BOUNCINESS;

    public RicochetEvent(
            @Nonnull Projectile bullet,
            Level level,
            @Nonnull HitResult hitResult,
            @Nonnull Vec3 normal
    ) {
        super(bullet);
        this.bullet = Objects.requireNonNull(bullet);
        this.level = Objects.requireNonNullElse(level, bullet.level());
        this.hitResult = Objects.requireNonNull(hitResult);
        this.normal = Objects.requireNonNull(normal);
    }

    @Override
    public @Nonnull Projectile getEntity() {
        return this.bullet;
    }

    public @Nonnull Projectile getBulletEntity() {
        return bullet;
    }

    public @Nonnull Level getLevel() {
        return level;
    }

    public @Nonnull LogicalSide getLogicalSide() {
        return level.isClientSide() ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public @Nonnull HitResult getHitResult() {
        return hitResult;
    }

    public @Nonnull Vec3 getNormal() {
        return normal;
    }

    public float getMaterialBouncinessOfHitTarget() {
        return targetBounciness;
    }

    public void setMaterialBouncinessOfHitTarget(float targetBounciness) {
        this.targetBounciness = targetBounciness;
    }
}
