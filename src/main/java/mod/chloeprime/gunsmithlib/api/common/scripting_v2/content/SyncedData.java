package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

/**
 * 枪械的同步数据（可写）。
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface SyncedData extends SyncedDataView {
    /**
     * 存入一个同步的 32 位整数。
     * 警告：请不要用这个方法存入时间戳，否则自 2038 年起你的枪包可能会失效。
     *
     * @param key 数据的 id
     * @param value 该 id 对应的新 32 整数值
     */
    void set_int(String key, int value);

    /**
     * 存入一个同步的双精度浮点数。
     * 可用于存储时间戳。
     *
     * @param key 数据的 id
     * @param value 该 id 对应的新双精度浮点数值
     */
    void set_number(String key, double value);

    /**
     * 存入一个同步的字符串。
     * 想存什么都可以哦
     *
     * @param key 数据的 id
     * @param value 该 id 对应的新字符串值
     */
    void set_string(String key, String value);
}
