package mod.chloeprime.gunsmithlib.common.util;

import net.minecraft.util.RandomSource;

import java.util.random.RandomGenerator;

/**
 * @since 6.0.0
 */
public record MojangRandomGenerator(RandomSource source) implements RandomGenerator {
    @Override
    public boolean nextBoolean() {
        return source.nextBoolean();
    }

    @Override
    public int nextInt() {
        return source.nextInt();
    }

    @Override
    public long nextLong() {
        return source.nextLong();
    }

    @Override
    public float nextFloat() {
        return source.nextFloat();
    }

    @Override
    public double nextDouble() {
        return source.nextDouble();
    }

    @Override
    public double nextGaussian() {
        return source.nextGaussian();
    }
}
