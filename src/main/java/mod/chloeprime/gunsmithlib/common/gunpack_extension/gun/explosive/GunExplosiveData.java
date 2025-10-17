package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;

public class GunExplosiveData {
    /**
     * 空爆距离挡位
     *
     * @since 4.9.0
     */
    @SuppressWarnings("unused")
    private @Nullable double[] airburst_distances;

    /**
     * 空爆距离随机比例
     *
     * @since 4.9.0
     */
    @SuppressWarnings("unused")
    private double airburst_distances_distribution;

    public @Nonnull DoubleList getAirburstDistances() {
        return Optional.ofNullable(airburst_distances)
                .map(DoubleList::of)
                .orElse(DoubleList.of());
    }

    public double getAirburstDistancesDistribution() {
        return airburst_distances_distribution;
    }

    public static Optional<GunExplosiveData> fromGun(ItemStack stack) {
        return Gunsmith.getGunInfo(stack).flatMap(GunExplosiveData::fromGun);
    }

    public static Optional<GunExplosiveData> fromGun(GunInfo gun) {
        return ((EnhancedGunData) gun.index().getGunData())
                .gunsmith$getGunsmithLibExtension()
                .map(GunsmithLibGunDataExtension::getGunExplosiveData);
    }
}
