package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.OptionalDouble;

public class GunExplosiveData {
    /**
     * 空爆距离挡位
     *
     * @since 4.9.0
     */
    @GunpackProperty
    private @Nullable double[] airburst_distances;

    /**
     * 空爆距离随机比例
     *
     * @since 4.9.0
     */
    @GunpackProperty
    private double airburst_distances_distribution;

    /**
     * 空爆测距装表的距离上限。
     * 只有填了这一项后玩家才可以用中键测距装表。
     *
     * @since 4.10.0
     */
    @GunpackProperty
    private Double airburst_rangefinder_max_distance;

    /**
     * 近炸引信探测距离
     *
     * @since 4.9.0
     */
    @GunpackProperty
    private double proximity_fuse_distance;

    /**
     * 防止爆炸炸坏掉落物
     *
     * @since 4.9.0
     */
    @GunpackProperty
    private boolean prevent_destroying_loot_items;

    @GunpackProperty
    private @Nullable GunExplosiveFragData fragments;

    public @Nonnull DoubleList getAirburstDistances() {
        return Optional.ofNullable(airburst_distances)
                .map(DoubleList::of)
                .orElse(DoubleList.of());
    }

    public double getAirburstDistancesDistribution() {
        return airburst_distances_distribution;
    }

    /**
     * @since 4.10.0
     */
    public OptionalDouble getAirburstRangefinderMaxDistance() {
        Double distance = airburst_rangefinder_max_distance;
        return distance == null ? OptionalDouble.empty() : OptionalDouble.of(distance);
    }

    public double getProximityFuseDistance() {
        return proximity_fuse_distance;
    }

    public boolean willPreventDestroyingLootItems() {
        return prevent_destroying_loot_items;
    }

    public @Nullable GunExplosiveFragData getFragData() {
        return fragments;
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
