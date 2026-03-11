package mod.chloeprime.gunsmithlib.api.common;

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
