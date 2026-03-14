package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

@SuppressWarnings("unused")
public interface VanillaCooldownExtension {
    /**
     * 获取当前物品的冷却时间
     * @return 当前物品的冷却时间，单位为秒
     */
    float get_cooldown_seconds();

    /**
     * 获取当前物品的冷却时间百分比
     * @return 当前物品的冷却时间百分比，0 为冷却完毕可以使用，1 为刚开始冷却
     */
    float get_cooldown_percent();
}
