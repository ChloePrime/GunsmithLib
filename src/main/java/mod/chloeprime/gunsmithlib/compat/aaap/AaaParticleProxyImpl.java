package mod.chloeprime.gunsmithlib.compat.aaap;

import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.AAAParticleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

class AaaParticleProxyImpl {
    public static void addParticle(
            Level level,
            boolean force,
            ResourceLocation id,
            Vec3 pos,
            Vec3 normal,
            @Nullable AAAParticleData aaaParticleData
    ) {
        double dx = normal.x();
        double dy = normal.y();
        double dz = normal.z();
        double xz = Math.sqrt(dx * dx + dz * dz);
        double rx = wrapRadians(Mth.atan2(dy, xz) - Math.PI / 2);
        double ry = wrapRadians(Mth.atan2(dz, dx) - Math.PI / 2);

        var pei = ParticleEmitterInfo.create(level, id)
                .position(pos)
                .rotation((float) -rx, (float) -ry, 0);
        if (aaaParticleData != null) {
            pei.scale(aaaParticleData.getScale());

            var parameters = aaaParticleData.getParameters();
            for (int i = 0; i < parameters.size(); i++) {
                pei.parameter(i, parameters.getFloat(i));
            }
            var triggers = aaaParticleData.getTriggers();
            for (int i = 0; i < triggers.size(); i++) {
                pei.trigger(triggers.getInt(i));
            }
        }
        AAALevel.addParticle(level, force, pei);
    }

    private static double wrapRadians(double radians) {
        return Math.toRadians(Mth.wrapDegrees(Math.toDegrees(radians)));
    }
}
