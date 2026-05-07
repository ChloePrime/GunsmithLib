package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * 枪械的同步数据（只读）。
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface SyncedDataView {
    /**
     * 获取一个同步的 32 位整数，如果不存在则崩溃。
     *
     * @param key 数据的 id
     * @return 该 id 对应的整数
     */
    default int get_int(String key) {
        return Objects.requireNonNull(optional_get_int(key), () -> "Int key %s does not exist".formatted(key));
    }

    /**
     * 获取一个同步的双精度浮点数，如果不存在则崩溃。
     *
     * @param key 数据的 id
     * @return 该 id 对应的双精度浮点数
     */
    default double get_number(String key) {
        return Objects.requireNonNull(optional_get_number(key), () -> "Number key %s does not exist".formatted(key));
    }

    /**
     * 获取一个同步的字符串，如果不存在则崩溃。
     *
     * @param key 数据的 id
     * @return 该 id 对应的字符串
     */
    default String get_string(String key) {
        return Objects.requireNonNull(optional_get_string(key), () -> "String key %s does not exist".formatted(key));
    }

    /**
     * 获取一个同步的 32 位整数，如果不存在则返回 {@code nil}。
     *
     * @param key 数据的 id
     * @return 该 id 对应的 32 位整数，或 {@code nil}
     */
    @Nullable Integer optional_get_int(String key);

    /**
     * 获取一个同步的双精度浮点数，如果不存在则返回 {@code nil}。
     *
     * @param key 数据的 id
     * @return 该 id 对应的双精度浮点数，或 {@code nil}
     */
    @Nullable Double optional_get_number(String key);

    /**
     * 获取一个同步的字符串，如果不存在则返回 {@code nil}。ao
     *
     * @param key 数据的 id
     * @return 该 id 对应的字符串，或 {@code nil}
     */
    @Nullable String optional_get_string(String key);
}
