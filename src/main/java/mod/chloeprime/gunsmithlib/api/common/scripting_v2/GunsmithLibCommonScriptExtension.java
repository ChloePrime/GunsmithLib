package mod.chloeprime.gunsmithlib.api.common.scripting_v2;

import cn.chloeprime.commons.ContextUtil;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.*;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.EnergyWeaponV2Data;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.GunEnergyStorage;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2.LongEnergyStorage;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.ItemSyncedDataImpl;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.TargetSearcherExtensionResultImpl;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.core.RegistryAccess;
import net.minecraft.world.entity.Entity;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.ToLongFunction;

@SuppressWarnings("unused")
public class GunsmithLibCommonScriptExtension
        implements
        VanillaCooldownExtension,
        RangefinderExtension,
        TargetSearcherExtension,
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

    /**
     * 根据指定的 uuid 获取实体。
     *
     * @param uid 字符串形式的实体 uuid
     * @return 给定 uuid 对应的实体，如果实体未加载或不存在则返回 {@code nil}
     * @since 6.2
     */
    public final @Nullable Entity get_entity_by_uid(String uid) {
        return get_entity_by_uuid(UUID.fromString(uid));
    }

    /**
     * 根据指定的 uuid 获取实体。
     *
     * @param uuid 实体 uuid
     * @return 给定 uuid 对应的实体，如果实体未加载或不存在则返回 {@code nil}
     * @since 6.2
     */
    public @Nullable Entity get_entity_by_uuid(UUID uuid) {
        return v1.gunsmithlib$getShooter()
                .map(Entity::level)
                .flatMap(lvl -> ClientProxy.getEntityByUuid(lvl, uuid))
                .orElse(null);
    }

    /**
     * 根据指定的 uuid 获取实体状态。
     *
     * @param uid 字符串形式的实体 uuid
     * @return 给定 uuid 对应的实体状态，如果实体未加载或不存在则返回 {@code nil}
     * @since 6.2
     */
    public final @Nullable EntityStates get_entity_state_by_uid(String uid) {
        return get_entity_state_by_uuid(UUID.fromString(uid));
    }

    /**
     * 根据指定的 uuid 获取实体状态。
     *
     * @param uuid 实体 uuid
     * @return 给定 uuid 对应的实体状态，如果实体未加载或不存在则返回 {@code nil}
     * @since 6.2
     */
    public final @Nullable EntityStates get_entity_state_by_uuid(UUID uuid) {
        return Optional.ofNullable(get_entity_by_uuid(uuid))
                .map(EntityStates::of)
                .orElse(null);
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

    // 火控 API

    @Override
    public @Nullable TargetSearcherExtension.Result search() {
        var shooter = v1.gunsmithlib$getShooter().orElse(null);
        var gun = shooter == null ? null : gunInfo().orElse(null);
        if (gun == null) {
            return null;
        }
        var partialTicks = ClientProxy.getPartialTicks(shooter.level().isClientSide());
        return TargetSearcher.search(shooter, gun, partialTicks)
                .map(TargetSearcherExtensionResultImpl::new)
                .orElse(null);
    }

    @Override
    public @Nullable TargetSearcherExtension.Result search(double range, double angularRadius) {
        var shooter = v1.gunsmithlib$getShooter().orElse(null);
        var gun = shooter == null ? null : gunInfo().orElse(null);
        if (gun == null) {
            return null;
        }
        var partialTicks = ClientProxy.getPartialTicks(shooter.level().isClientSide());
        return TargetSearcher.search(shooter, range, angularRadius, partialTicks)
                .map(TargetSearcherExtensionResultImpl::new)
                .orElse(null);
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

    protected RegistryAccess registryAccess() {
        return v1.gunsmithlib$getShooter()
                .map(Entity::registryAccess)
                .orElseGet(ContextUtil::getRegistryAccess);
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
