package mod.chloeprime.gunsmithlib.api.common;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;

/**
 * 双机共享的扩展 API
 *
 * @since 4.4.0
 */
@SuppressWarnings("unused")
public interface CommonScriptingExtension extends
        VanillaCooldownAPI,
        RangefinderAPI,
        BetterAsyncAPI {
    /**
     * 获得扩展 API 包装器。
     * 推荐使用这个包装器而不是下面那堆有前缀的 api，
     * 这样能大量节省代码行的长度。
     *
     * @since 5.6.0
     */
    GunsmithLibCommonScriptExtension gunsmithlib_extension();

    /**
     * 获取当前脚本对应的枪械的 id
     *
     * @since 4.12.0
     */
    String gunsmith_getGunId();

    /**
     * 获取武器蓄力时间，单位为秒。
     *
     * @since 4.13.0
     */
    double gunsmith_getChargingTime();
}
