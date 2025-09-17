package mod.chloeprime.gunsmithlib.common.entity;

import net.minecraft.core.particles.ColorParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import org.joml.Vector3d;

import javax.annotation.Nonnull;

public class AreaEffectCloud3D extends AreaEffectCloud {
    public static final EntityType<AreaEffectCloud3D> TYPE = EntityType.Builder
            .<AreaEffectCloud3D>of(AreaEffectCloud3D::new, MobCategory.MISC)
            .fireImmune()
            .sized(6, 6)
            .clientTrackingRange(10)
            .updateInterval(Integer.MAX_VALUE)
            .build("area_effect_cloud_3d");

    public AreaEffectCloud3D(EntityType<? extends AreaEffectCloud> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        super.setRadius(0);
        this.setWaitTime(0);
    }

    public AreaEffectCloud3D(Level level, double x, double y, double z) {
        this(TYPE, level);
        this.setPos(x, y, z);
    }

    public static AreaEffectCloud3D createAtCenter(Level level, double x, double y, double z, float radius) {
        var cloud = new AreaEffectCloud3D(level, x, y, z);
        cloud.setRadius(radius);
        return cloud;
    }

    @Override
    public @Nonnull EntityDimensions getDimensions(@Nonnull Pose pPose) {
        var length = getRadius() * 2;
        return EntityDimensions.scalable(length, length);
    }

    @Override
    public void setRadius(float value) {
        var delta = value - getRadius();
        super.setRadius(value);
        // 让半径更改时以碰撞立方体中心为中心，
        // 而不是默认的以底部为中心
        setPos(getX(), getY() - delta, getZ());
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            return;
        }

        var waiting = this.isWaiting();
        var dataRadius = this.getRadius();
        if (waiting && this.random.nextBoolean()) {
            return;
        }

        var particle = this.getParticle();
        int count;
        float radius;
        if (waiting) {
            count = 2;
            radius = 0.2F;
        } else {
            // 球体的公式是(4/3)πr³，但是这个公式计算出来的粒子数量太多了，
            // 所以这里把前面的系数改小点（除以6），让半径为6时2D公式的粒子数量和3D公式的粒子数量相等
            count = Mth.ceil((1.0 / 6) * Math.PI * dataRadius * dataRadius * dataRadius);
            radius = dataRadius;
        }

        for(int i = 0; i < count; ++i) {
            Vector3d angle = randomUnitVector();
            double distance = Math.pow(this.random.nextFloat(), 0.7) * radius;
            double x = this.getX() + angle.x() * distance;
            double y = this.getY() + angle.y() * distance + this.getBbHeight() / 2;
            double z = this.getZ() + angle.z() * distance;
            if (particle.getType() == ParticleTypes.ENTITY_EFFECT) {
                if (waiting && this.random.nextBoolean()) {
                    this.level().addAlwaysVisibleParticle(ColorParticleOption.create(ParticleTypes.ENTITY_EFFECT, -1), x, y, z, 0, 0, 0);
                } else {
                    this.level().addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
                }
            } else if (waiting) {
                this.level().addAlwaysVisibleParticle(particle, x, y, z, 0, 0, 0);
            } else {
                this.level().addAlwaysVisibleParticle(
                        particle, x, y, z,
                        (0.5 - this.random.nextDouble()) * 0.15, 0.01, (0.5 - this.random.nextDouble()) * 0.15
                );
            }
        }
    }

    private static final Vector3d RANDOM_UNIT_BUFFER = new Vector3d();

    private Vector3d randomUnitVector() {
        RANDOM_UNIT_BUFFER.x = this.random.nextGaussian();
        RANDOM_UNIT_BUFFER.y = this.random.nextGaussian();
        RANDOM_UNIT_BUFFER.z = this.random.nextGaussian();
        RANDOM_UNIT_BUFFER.normalize();
        return RANDOM_UNIT_BUFFER;
    }
}
