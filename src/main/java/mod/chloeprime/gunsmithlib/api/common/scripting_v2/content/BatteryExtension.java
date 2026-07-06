package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import org.jetbrains.annotations.ApiStatus;

/**
 * 新版电池数据 API。
 *
 * @since 6.1.0
 */
public interface BatteryExtension {
    /**
     * 获取电池内存储的电量。
     * {@link #get_energy_stored()} 的别名
     *
     * @return 电池内存储的电量
     */
    @ApiStatus.NonExtendable
    default long energy_stored() {
        return get_energy_stored();
    }

    /**
     * 获取电池内存储的电量。
     *
     * @return 电池内存储的电量
     */
    long get_energy_stored();

    /**
     * 获取 data 文件中设置的电池容量上限。
     * 此方法返回的上限不受 modify property 影响。
     *
     * @return data 文件中设置的电池容量上限
     */
    long get_configured_battery_capacity();

    /**
     * 获取 data 文件中设置的输入速度上限。
     * 此方法返回的上限不受 modify property 影响。
     *
     * @return data 文件中设置的输入速度上限
     */
    long get_configured_max_energy_input_speed();

    /**
     * 获取 data 文件中设置的输出速度上限。
     * 此方法返回的上限不受 modify property 影响。
     *
     * @return data 文件中设置的输出速度上限
     */
    long get_configured_max_energy_output_speed();
}
