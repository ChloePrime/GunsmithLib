package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.lang4.StringName;

/**
 * 安全距离的用途 / 影响范围
 *
 * @param name 用途的 id
 * @since 6.2
 */
public record SafetyDistanceFlag(
        StringName name
) {
    public SafetyDistanceFlag(String name) {
        this(StringName.of(name));
    }
}
