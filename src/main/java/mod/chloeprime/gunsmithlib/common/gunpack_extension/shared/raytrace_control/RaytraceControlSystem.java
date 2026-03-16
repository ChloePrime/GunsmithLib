package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.raytrace_control;

import com.google.common.collect.MapMaker;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.TagKeyOr;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;

public class RaytraceControlSystem {
    public static final Map<ClipContext, RaytraceControlData> RC_DATA_FIELD = new MapMaker().weakKeys().makeMap();

    public static void setupFor(ClipContext context, ResourceLocation gunId) {
        Objects.requireNonNull(context);
        Objects.requireNonNull(gunId);

        var data = RaytraceControlData.of(Gunsmith.createGunItemFromId(gunId)).orElse(null);
        if (data == null) {
            return;
        }
        RC_DATA_FIELD.put(context, data);
    }

    public static Optional<Predicate<BlockState>> makePredicate(ClipContext context) {
        var data = RC_DATA_FIELD.get(context);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(state -> shouldIgnore(data, state));
    }

    public static boolean shouldIgnore(RaytraceControlData data, BlockState state) {
        for (TagKeyOr<Block> matcher : data.getObstructList()) {
            if (matcher.match(state::is, state::is)) {
                return false;
            }
        }
        for (TagKeyOr<Block> matcher : data.getIgnoreList()) {
            if (matcher.match(state::is, state::is)) {
                return true;
            }
        }
        return false;
    }
}
