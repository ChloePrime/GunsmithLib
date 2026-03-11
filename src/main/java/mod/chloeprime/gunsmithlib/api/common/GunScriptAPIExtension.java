package mod.chloeprime.gunsmithlib.api.common;

/**
 * @since 3.3.0
 */
@SuppressWarnings("unused")
public interface GunScriptAPIExtension extends CommonScriptingExtension {
    /**
     * 播放 GunsmithLib 内置的过热音效
     */
    void gunsmith_playOverheatSound();

    /**
     * 通知客户端触发状态机脚本状态转移（transition）
     *
     * @param input 状态机 transition 函数中的 input 参数
     * @since 5.4.0
     */
    void gunsmith_triggerAnimationStateTransition(String input);
}
