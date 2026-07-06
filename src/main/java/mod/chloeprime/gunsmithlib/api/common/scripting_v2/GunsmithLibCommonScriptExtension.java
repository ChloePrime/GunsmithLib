package mod.chloeprime.gunsmithlib.api.common.scripting_v2;

import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.*;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.EnergyWeaponV2Data;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.GunEnergyStorage;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.LongEnergyStorage;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.ItemSyncedDataImpl;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.ToLongFunction;

@SuppressWarnings("unused")
public class GunsmithLibCommonScriptExtension
        implements
        VanillaCooldownExtension,
        RangefinderExtension,
        BetterAsyncExtension,
        BatteryExtension {
    /**
     * 三元表达式，给 lua 用的。
     *
     * @param condition 条件
     * @param whenTrue 条件为 true 时返回的值
     * @param whenFalse 条件为 false 时返回的值
     * @return {@code condition ? whenTrue: whenFalse}
     * @param <T> 返回值的类型。
     */
    public <T> T ternary_op(boolean condition, T whenTrue, T whenFalse) {
        return condition ? whenTrue : whenFalse;
    }

    /**
     * 获取当前脚本对应的枪械的 id
     */
    public String get_gun_id() {
        return v1.gunsmith_getGunId();
    }

    /**
     * 获取射手的各种状态。
     * 这个方法在逻辑脚本中永远不会返回 {@code nil}。
     *
     * @return 获取射手的各种状态的接口
     * @see ServerShootStates 逻辑脚本中调用时实际返回的值
     * @see ClientShootStates 客户端返回的
     * @see ShooterStates
     * @see EntityStates
     * @since 6.0.0
     */
    public @Nullable ShooterStates shooter_states() {
        return v1.gunsmithlib$getShooter()
                .map(BaseShooterStatesImpl::new)
                .orElse(null);
    }

    /**
     * 获取同步数据接口。
     * 在服务端（逻辑脚本）中反馈的是可写入的，在客户端中返回的是只读的，不可写入。
     *
     * @return 基于枪械物品 NBT 的同步数据接口
     * @see SyncedData 逻辑机调用时返回的，可写入的接口
     * @since 6.0.0
     */
    public SyncedDataView synced_data() {
        return new ItemSyncedDataImpl(v1.gunsmithlib$getCurrentItem(), true);
    }

    // 旧版 API

    /**
     * 获取武器蓄力时间，单位为秒。
     *
     * @return 未定义行为（UB）
     * @deprecated 请使用 {@link GunAnimationStateContext#getChargeProgress} 或 {@link ModernKineticGunScriptAPI#getChargeProgress}
     */
    @Deprecated(since = "6.0.0")
    public double get_charge_time() {
        return v1.gunsmith_getChargingTime();
    }

    @Override
    public float get_cooldown_seconds() {
        return v1.gunsmith_getCooldownSeconds();
    }

    @Override
    public float get_cooldown_percent() {
        return v1.gunsmith_getCooldownPercent();
    }

    @Override
    public double get_estimated_range() {
        return v1.gunsmith_getEstimatedRange();
    }

    @Override
    public double get_estimated_range(int pierce) {
        return v1.gunsmith_getEstimatedRange(pierce);
    }

    @Override
    public void async_run_delayed(LuaValue callback, int delayTicks, Object... params) {
        v1.gunsmith_asyncRunDelayed(callback, delayTicks, params);
    }

    @Override
    public void async_run_cycled(LuaValue callback, int period, int count, Object... params) {
        v1.gunsmith_asyncRunCycled(callback, period, count, params);
    }

    // 电池 API

    @Override
    public long get_energy_stored() {
        return mapEnergyV2Cap(LongEnergyStorage::getEnergyStoredL, IEnergyStorage::getEnergyStored);
    }

    @Override
    public long get_configured_battery_capacity() {
        return gunInfo()
                .flatMap(EnergyWeaponV2Data::of)
                .map(EnergyWeaponV2Data::getStaticCapacity)
                .orElse(0L);
    }

    @Override
    public long get_configured_max_energy_input_speed() {
        return gunInfo()
                .flatMap(EnergyWeaponV2Data::of)
                .map(EnergyWeaponV2Data::getStaticMaxInputSpeed)
                .orElse(0L);
    }

    @Override
    public long get_configured_max_energy_output_speed() {
        return gunInfo()
                .flatMap(EnergyWeaponV2Data::of)
                .map(EnergyWeaponV2Data::getStaticMaxOutputSpeed)
                .orElse(0L);
    }

    // 下面是内部 API

    private final AbstractCommonScriptingExtension v1;

    protected Optional<GunInfo> gunInfo() {
        return GsHelper.unpack(v1.gunsmithlib$getGunItemInterface(), v1.gunsmithlib$getCurrentItem());
    }

    protected long mapEnergyV2Cap(ToLongFunction<GunEnergyStorage> code, ToLongFunction<IEnergyStorage> fallback) {
        var cap = v1.gunsmithlib$getCurrentItem().getCapability(Capabilities.EnergyStorage.ITEM);
        if (cap == null) {
            return 0;
        }
        return cap instanceof GunEnergyStorage loong
                ? code.applyAsLong(loong)
                : fallback.applyAsLong(cap);
    }

    @ApiStatus.Internal
    public GunsmithLibCommonScriptExtension(AbstractCommonScriptingExtension v1) {
        this.v1 = v1;
    }
}
