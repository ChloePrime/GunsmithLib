package mod.chloeprime.gunsmithlib.compat.aaap;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.AAAParticleData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.ModList;

import javax.annotation.Nullable;

public class AaaParticleProxy {
    public static final boolean INSTALLED = ModList.get().isLoaded("aaa_particles");

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
}
