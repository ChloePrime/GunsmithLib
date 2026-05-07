package mod.chloeprime.gunsmithlib.api.common.scripting_v2;

import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.BetterAsyncExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.EntityStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.RangefinderExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ServerShootStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ShooterStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.VanillaCooldownExtension;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;

import javax.annotation.Nullable;

@SuppressWarnings("unused")
public class GunsmithLibCommonScriptExtension
        implements
        VanillaCooldownExtension,
        RangefinderExtension,
        BetterAsyncExtension {
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
     */
    public @Nullable ShooterStates shooter_states() {
        return v1.gunsmithlib$getShooter()
                .map(BaseShooterStatesImpl::new)
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

    // 下面是内部 API

    private final AbstractCommonScriptingExtension v1;

    @ApiStatus.Internal
    public GunsmithLibCommonScriptExtension(AbstractCommonScriptingExtension v1) {
        this.v1 = v1;
    }
}
