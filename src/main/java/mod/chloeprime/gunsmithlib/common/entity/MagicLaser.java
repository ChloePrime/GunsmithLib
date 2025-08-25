package mod.chloeprime.gunsmithlib.common.entity;

import mod.chloeprime.gunsmithlib.client.laser.LaserInstance;
import net.minecraft.commands.arguments.EntityAnchorArgument.Anchor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class MagicLaser extends Projectile {
    public static final EntityType<MagicLaser> TYPE = EntityType.Builder
            .<MagicLaser>of(MagicLaser::new, MobCategory.MISC)
            .noSummon()
            .noSave()
            .clientTrackingRange(0)
            .build("magic_laser");

    private LaserInstance instance;

    public MagicLaser(Level level) {
        this(TYPE, level);
    }

    public MagicLaser(EntityType<? extends MagicLaser> type, Level level) {
        super(type, level);
    }

    public void beginRendering(LaserInstance instance) {
        this.instance = instance;
        setPos(instance.startPos);
        lookAt(Anchor.FEET, getHitLocation());
    }

    public void endRendering() {
        this.instance = null;
    }

    public Vec3 getHitLocation() {
        return instance == null ? Vec3.ZERO : instance.hitLocation;
    }

    public float getRoll() {
        return instance == null ? 0 : instance.roll;
    }

    public float getLength() {
        return instance == null ? 0 : instance.length;
    }

    public long getLocalSpawnTime() {
        return instance == null ? 0 : instance.localSpawnTime;
    }

    @Override
    public void tick() {
        if (!(level().isClientSide())) {
            discard();
        }
    }

    @Override
    protected void defineSynchedData(@Nonnull SynchedEntityData.Builder builder) {
    }

    public @Nullable LivingEntity getShooter() {
        return instance == null ? null : instance.getShooter()
                .map(entity -> entity instanceof LivingEntity living ? living : null)
                .orElse(null);
    }
}
