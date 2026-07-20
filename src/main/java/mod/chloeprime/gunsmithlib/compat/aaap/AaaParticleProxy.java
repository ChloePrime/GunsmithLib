package mod.chloeprime.gunsmithlib.compat.aaap;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.AAAParticleData;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

public class AaaParticleProxy {
    public static final boolean INSTALLED = ModInstallationStatus.AAA_PARTICLES_INSTALLED;

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
        if (!INSTALLED) {
            return;
        }
        AaaParticleProxyImpl.addParticle(
                level,
                force,
                id,
                pos,
                normal,
                scale,
                aaaParticleData);
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
        if (!INSTALLED) {
            return;
        }
        AaaParticleProxyImpl.addParticleZP(
                level,
                force,
                id,
                pos,
                normal,
                scale,
                aaaParticleData);
    }
}
