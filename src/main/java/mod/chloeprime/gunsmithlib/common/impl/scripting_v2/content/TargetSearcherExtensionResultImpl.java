package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.EntityStates;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.TargetSearcherExtension;
import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import net.minecraft.world.entity.Entity;
import org.joml.Vector3d;

import static cn.chloeprime.commons.math.LinearAlgebraTypes.moj2joml;

/**
 * @since 6.2
 */
public record TargetSearcherExtensionResultImpl(
        EntityStates target,
        Entity target_entity,
        Vector3d pos,
        Vector3d relative_pos
) implements TargetSearcherExtension.Result {
    public TargetSearcherExtensionResultImpl(TargetSearcher.SearchResult result) {
        this(EntityStates.of(result.entity()), result.entity(), moj2joml(result.pos()), moj2joml(result.relativePos()));
    }
}
