package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

import javax.annotation.Nullable;

/**
 * 目标搜索器。自瞄锁定目标的目标搜索的核心实现。
 *
 * @since 6.2.0
 */
public interface TargetSearcherExtension {
    interface Result {
        EntityStates target();
        Entity target_entity();
        Vector3d pos();
        Vector3d relative_pos();
    }

    /**
     * 使用枪械自带的火控系统搜索目标。
     *
     * @return 搜索结果，如果没有搜索到结果则返回 {@code nil}
     */
    @Nullable Result search();

    /**
     * 使用指定的参数搜索目标。
     *
     * @param range 最大搜索距离
     * @param angularRadius 最大搜索角度（半径）
     * @return 搜索结果，如果没有搜索到结果则返回 {@code nil}
     */
    @Nullable Result search(double range, double angularRadius);
}
