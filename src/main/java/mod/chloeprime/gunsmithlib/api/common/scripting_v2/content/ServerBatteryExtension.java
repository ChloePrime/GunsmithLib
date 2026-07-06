package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import org.jetbrains.annotations.ApiStatus;

/**
 * 服务端，可写入的新版电池数据 API。
 *
 * @since 6.1.0
 */
public interface ServerBatteryExtension extends BatteryExtension {
    /**
     * 设置电池内存储的电量。
     *
     * @param value 电池内存储的电量
     */
    void set_energy_stored(long value);

    /**
     * 从电池中抽取电量。
     * 将能量用于开火时请使用 {@link #privileged_extract_energy(long)}，否则不能向电容器放电的武器会射不出来。
     *
     * @param wantedAmount 计划抽走的电量
     * @return 实际抽走的电量
     */
    @ApiStatus.NonExtendable
    default long extract_energy(long wantedAmount) {
        return extract_energy(wantedAmount, false);
    }

    /**
     * 从电池中抽取电量。
     * 将能量用于开火时请使用 {@link #privileged_extract_energy(long, boolean)}，否则不能向电容器放电的武器会射不出来。
     *
     * @param wantedAmount 计划抽走的电量
     * @param simulate 如果为 true，那么这次抽取将作为模拟抽取，返回可抽取的电量，但不改变电池内实际存储的电量。
     * @return 实际抽走的电量
     */
    long extract_energy(long wantedAmount, boolean simulate);

    /**
     * 向电池中存入电量。
     *
     * @param givenAmount 提供的电量
     * @return 实际存入的电量
     */
    @ApiStatus.NonExtendable
    default long receive_energy(long givenAmount) {
        return receive_energy(givenAmount, false);
    }

    /**
     * 向电池中存入电量。
     *
     * @param givenAmount 提供的电量
     * @param simulate 如果为 true，那么这次存入将作为模拟存入，返回可存入的电量，但不改变电池内实际存储的电量。
     * @return 实际存入的电量
     */
    long receive_energy(long givenAmount, boolean simulate);

    /**
     * 从电池中抽取电量，无视放电速度上限。
     * 将能量用于开火时请使用这个方法。
     *
     * @param wantedAmount 计划抽走的电量
     * @return 实际抽走的电量
     */
    @ApiStatus.NonExtendable
    default long privileged_extract_energy(long wantedAmount) {
        return privileged_extract_energy(wantedAmount, false);
    }

    /**
     * 从电池中抽取电量，无视放电速度上限。
     * 将能量用于开火时请使用这个方法。
     *
     * @param wantedAmount 计划抽走的电量
     * @param simulate 如果为 true，那么这次抽取将作为模拟抽取，返回可抽取的电量，但不改变电池内实际存储的电量。
     * @return 实际抽走的电量
     */
    long privileged_extract_energy(long wantedAmount, boolean simulate);

    /**
     * 向电池中存入电量，无视充电速度上限。
     *
     * @param givenAmount 提供的电量
     * @return 实际存入的电量
     */
    @ApiStatus.NonExtendable
    default long privileged_receive_energy(long givenAmount) {
        return privileged_receive_energy(givenAmount, false);
    }

    /**
     * 向电池中存入电量，无视充电速度上限。
     *
     * @param givenAmount 提供的电量
     * @param simulate 如果为 true，那么这次存入将作为模拟存入，返回可存入的电量，但不改变电池内实际存储的电量。
     * @return 实际存入的电量
     */
    long privileged_receive_energy(long givenAmount, boolean simulate);
}
