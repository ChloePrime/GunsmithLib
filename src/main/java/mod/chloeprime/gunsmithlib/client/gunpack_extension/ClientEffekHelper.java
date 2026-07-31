package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;

final class ClientEffekHelper {
    /**
     * 读取武器的 AAA 粒子数据
     */
    public static List<HitParticleData> get(
            ItemStack stack,
            Function<GunsmithLibSharedDataExtension, HitParticleData[]> getter
    ) {
        if (!AaaParticleProxy.INSTALLED) {
            return Collections.emptyList();
        }
        var buffer = PARTICLE_DATA_BUFFER;
        buffer.clear();
        GunsmithLibSharedDataExtension
                .forGunOrAmmo(stack, getter)
                .stream().flatMap(Arrays::stream)
                .filter(pd -> pd.isAaaParticle() == Boolean.TRUE)
                .forEach(buffer::add);
        return Collections.unmodifiableList(buffer);
    }

    private static final List<HitParticleData> PARTICLE_DATA_BUFFER = new ArrayList<>();

    private ClientEffekHelper() {
    }
}
