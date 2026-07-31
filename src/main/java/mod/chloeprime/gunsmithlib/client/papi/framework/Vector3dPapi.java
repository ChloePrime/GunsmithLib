package mod.chloeprime.gunsmithlib.client.papi.framework;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public interface Vector3dPapi extends Papi {
    static String format(Vec3 vec) {
        return "%.1f, %.1f, %.1f".formatted(vec.x(), vec.y(), vec.z());
    }

    Optional<Vec3> getVector();

    @Override
    default String apply(ItemStack stack) {
        return getVector()
                .map(Vector3dPapi::format)
                .orElse(FALLBACK_TEXT);
    }
}
