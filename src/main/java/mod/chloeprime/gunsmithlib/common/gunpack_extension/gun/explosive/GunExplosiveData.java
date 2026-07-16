package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.lang4.StringName;
import com.google.common.base.Suppliers;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Supplier;
import java.util.stream.Collectors;

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

    /**
     * 破片系统配置
     *
     * @since 5.9
     */
    @GunpackProperty
    private @Nullable GunExplosiveFragData fragments;

    /**
     * 安全距离，默认只影响近炸引信。
     * <p>
     * 参阅 {@link #safety_distance_flags} 以查看更多可影响的功能。
     *
     * @since 6.2
     */
    @GunpackProperty
    private double safety_distance;

    /**
     * 安全距离的作用对象。
     * 默认只影响近炸引信（阻止近炸引信在安全距离内工作）。
     * <p>
     * 可选值：
     * <table>
     *   <tr>
     *     <th>id</th>
     *     <th>用途</th>
     *   </tr>
     *   <tr>
     *     <td>{@code prevents_airburst_rangefindinng}</td>
     *     <td>阻止低于安全距离的空爆测距仪结果</td>
     *   </tr>
     *   <tr>
     *     <td>{@code prevents_proximity_fuse}</td>
     *     <td>子弹飞行距离低于安全距离时阻止近炸引信工作</td>
     *   </tr>
     *   <tr>
     *     <td>{@code prevents_explosion}</td>
     *     <td>子弹飞行距离低于安全距离时阻止子弹爆炸和产生破片</td>
     *   </tr>
     * </table>
     *
     * @since 6.2
     */
    @GunpackProperty
    private StringName[] safety_distance_flags = null;

    // 下面是代码 :)

    private static final StringName[] DEFAULT_SAFETY_DISTANCE_FLAGS = {
            SafetyDistanceFlags.PREVENTS_PROXIMITY_FUSE.name()
    };

    private final Supplier<Set<SafetyDistanceFlag>> safetyDistanceFlags = Suppliers.memoize(() -> {
        var arr = Objects.requireNonNullElse(safety_distance_flags, DEFAULT_SAFETY_DISTANCE_FLAGS);
        return Arrays.stream(arr)
                .map(SafetyDistanceFlag::new)
                .collect(Collectors.toUnmodifiableSet());
    });

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

    public double getSafetyDistance() {
        return safety_distance;
    }

    public @Nonnull Set<SafetyDistanceFlag> getSafetyDistanceFlags() {
        return safetyDistanceFlags.get();
    }

    public boolean hasSafetyDistanceFlag(SafetyDistanceFlag flag) {
        return getSafetyDistanceFlags().contains(flag);
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
