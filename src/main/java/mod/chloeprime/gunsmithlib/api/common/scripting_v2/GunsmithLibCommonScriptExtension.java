package mod.chloeprime.gunsmithlib.api.common.scripting_v2;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.BetterAsyncExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.RangefinderExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.VanillaCooldownExtension;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;

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

    // 旧版 API

    /**
     * 获取武器蓄力时间，单位为秒。
     */
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
    public void async_run_delayed(LuaValue func, int delayTicks) {
        v1.gunsmith_asyncRunDelayed(func, delayTicks);
    }

    @Override
    public void async_run_cycled(LuaValue func, int period, int count) {
        v1.gunsmith_asyncRunCycled(func, period, count);
    }

    // 下面是内部 API

    private final AbstractCommonScriptingExtension v1;

    @ApiStatus.Internal
    public GunsmithLibCommonScriptExtension(AbstractCommonScriptingExtension v1) {
        this.v1 = v1;
    }
}
