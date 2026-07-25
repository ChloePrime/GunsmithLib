package mod.chloeprime.gunsmithlib.common.util;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.LightLayer;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.OptionalInt;

public final class LightHelper {
    public static OptionalInt getLightAt(Level level, BlockPos pos, @Nonnull LightType type) {
        Objects.requireNonNull(type);
        if (!level.isLoaded(pos)) {
            return OptionalInt.empty();
        }
        return OptionalInt.of(dispatchGetLight(pos, level, type));
    }

    private static int dispatchGetLight(BlockPos pos, LevelReader level, @Nonnull LightType type) {
        Objects.requireNonNull(type);
        return switch (type) {
            case THEORETICAL -> level.getRawBrightness(pos, 0);
            case BLOCK -> level.getBrightness(LightLayer.BLOCK, pos);
            case SKY -> level.getBrightness(LightLayer.SKY, pos);
            case REALTIME -> level.getMaxLocalRawBrightness(pos);
            case REALTIME_SKY -> Math.max(0, level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken());
        };
    }

    private LightHelper() {
    }
}
