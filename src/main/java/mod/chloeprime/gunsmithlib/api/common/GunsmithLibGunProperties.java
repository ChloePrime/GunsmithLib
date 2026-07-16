package mod.chloeprime.gunsmithlib.api.common;

import com.tacz.guns.api.ValueModifiableAtRuntime;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.EnergyWeaponV2Data;

/**
 * @since 6.2
 */
public final class GunsmithLibGunProperties {
    /**
     * 测距仪测得的空爆距离
     * <p>
     * {@code gunsmithlib_measured_airburst_distance}
     */
    @ValueModifiableAtRuntime(Double.class)
    public static final String MEASURED_AIRBURST_DISTANCE = "%s_measured_airburst_distance".formatted(GunsmithLib.MOD_ID);

    /**
     * 火控计算机传给子弹的空爆距离
     * <p>
     * {@code gunsmithlib_programmed_airburst_distance}
     */
    @ValueModifiableAtRuntime(Double.class)
    public static final String PROGRAMMED_AIRBURST_DISTANCE = "%s_programmed_airburst_distance".formatted(GunsmithLib.MOD_ID);

    /**
     * 近炸引信探测距离
     * <p>
     * 即使 data 中没有配置近炸也可被修改。
     * <p>
     * {@code gunsmithlib_proximity_fuse_distance}
     */
    @ValueModifiableAtRuntime(Double.class)
    public static final String PROXIMITY_FUSE_DISTANCE = "%s_proximity_fuse_distance".formatted(GunsmithLib.MOD_ID);

    /**
     * 破片数量
     * <p>
     * 需要在 data 中配置破片系统才能被修改。
     * <p>
     * {@code gunsmithlib_frag_count}
     */
    @ValueModifiableAtRuntime(Integer.class)
    public static final String FRAG_COUNT = "%s_frag_count".formatted(GunsmithLib.MOD_ID);

    /**
     * 电池容量。需要在 data 中手动开启才能被脚本修改。
     * <p>
     * {@code gunsmithlib_energy_capacity}
     */
    @ValueModifiableAtRuntime(Long.class)
    @SuppressWarnings("unused")
    public static final String ENERGY_CAPACITY = EnergyWeaponV2Data.PROP_NAME_MAX_CAPACITY;

    /**
     * 最大充电速度。需要在 data 中手动开启才能被脚本修改。
     * <p>
     * {@code gunsmithlib_max_energy_input_speed}
     */
    @ValueModifiableAtRuntime(Long.class)
    @SuppressWarnings("unused")
    public static final String MAX_ENERGY_INPUT_SPEED = EnergyWeaponV2Data.PROP_NAME_MAX_INPUT_SPEED;

    /**
     * 最大放电速度。需要在 data 中手动开启才能被脚本修改。
     * <p>
     * {@code gunsmithlib_max_energy_output_speed}
     */
    @ValueModifiableAtRuntime(Long.class)
    @SuppressWarnings("unused")
    public static final String MAX_ENERGY_OUTPUT_SPEED = EnergyWeaponV2Data.PROP_NAME_MAX_OUTPUT_SPEED;

    private GunsmithLibGunProperties() {
    }
}
