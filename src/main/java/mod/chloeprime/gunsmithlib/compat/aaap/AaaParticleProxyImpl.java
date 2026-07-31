package mod.chloeprime.gunsmithlib.compat.aaap;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.AAAParticleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

class AaaParticleProxyImpl {
    /**
     * 该方法定义的前方为 Effekseer 编辑器内的 Y 轴正上方。
     */
    public static void addParticle(
            Level level,
            boolean force,
            ResourceLocation id,
            Vec3 pos,
            Vec3 normal,
            float scale,
            @Nullable AAAParticleData aaaParticleData
    ) {
        double dx = normal.x();
        double dy = normal.y();
        double dz = normal.z();
        double xz = Math.sqrt(dx * dx + dz * dz);
        double rx = wrapRadians(Mth.atan2(dy, xz) - Math.PI / 2);
        double ry = wrapRadians(Mth.atan2(dz, dx) - Math.PI / 2);
        addParticle0(level, force, id, pos, (float) -rx, (float) -ry, scale, aaaParticleData);
    }

    /**
     * 该方法定义的前方为 Effekseer 编辑器内的 Z 轴正方向。
     */
    public static void addParticleZP(
            Level level,
            boolean force,
            ResourceLocation id,
            Vec3 pos,
            Vec3 normal,
            float scale,
            @Nullable AAAParticleData aaaParticleData
    ) {
        Vec2 rot = rotationFromForward(normal);
        addParticle0(level, force, id, pos, rot.x, rot.y, scale, aaaParticleData);
    }

    public static void bindParticleZP(
            Entity entity,
            ResourceLocation id,
            Vec3 pos,
            float scale,
            @Nullable AAAParticleData aaaParticleData
    ) {
        Objects.requireNonNull(entity);
        Objects.requireNonNull(id);

        var pei = ParticleEmitterInfo.create(entity.level(), id)
                .bindOnEntity(entity)
                .entitySpaceRelativePosition(pos)
                .useEntityVelocityAsRotation();
        if (aaaParticleData != null) {
            pei.scale(aaaParticleData.getScale() * scale);
            processAaaParticleData(aaaParticleData, pei);
        } else {
            pei.scale(scale);
        }
        AAALevel.addParticle(entity.level(), true, pei);
    }

    private static Vec2 rotationFromForward(Vec3 forward) {
        Vec2 rot = forward2rot(forward);
        return new Vec2(wrapRadians(-Mth.PI / 2 - rot.x), wrapRadians(rot.y + Mth.PI));
    }

    private static Vec2 forward2rot(Vec3 forward) {
        double dx = forward.x();
        double dy = forward.y();
        double dz = forward.z();
        double xz = Math.sqrt(dx * dx + dz * dz);
        double rx = -wrapRadians(Mth.atan2(dy, xz) - Math.PI / 2);
        double ry = -wrapRadians(Mth.atan2(dz, dx) - Math.PI / 2);
        return new Vec2((float) rx, (float) ry);
    }

    private static void addParticle0(
            Level level,
            boolean force,
            ResourceLocation id,
            Vec3 pos,
            float rx,
            float ry,
            float scale,
            @Nullable AAAParticleData aaaParticleData
    ) {
        var pei = ParticleEmitterInfo.create(level, id)
                .position(pos)
                .rotation(rx, ry, 0);
        if (aaaParticleData != null) {
            pei.scale(aaaParticleData.getScale() * scale);
            processAaaParticleData(aaaParticleData, pei);
        } else {
            pei.scale(scale);
        }
        AAALevel.addParticle(level, force, pei);
    }

    private static void processAaaParticleData(@Nonnull AAAParticleData data, @Nonnull ParticleEmitterInfo pei) {
        Objects.requireNonNull(data);
        Objects.requireNonNull(pei);

        var parameters = data.getParameters();
        for (int i = 0; i < parameters.size(); i++) {
            pei.parameter(i, parameters.getFloat(i));
        }
        var triggers = data.getTriggers();
        for (int i = 0; i < triggers.size(); i++) {
            pei.trigger(triggers.getInt(i));
        }
    }

    private static float wrapRadians(float radians) {
        return (float) wrapRadians((double) radians);
    }

    private static double wrapRadians(double radians) {
        return Math.toRadians(Mth.wrapDegrees(Math.toDegrees(radians)));
    }
}
