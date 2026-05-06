package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.ApiStatus;

import java.util.*;
import java.util.function.Supplier;

/**
 * @since 6.0.0
 */
public final class GunVariantSorting {
    @ApiStatus.Internal
    public interface Prioritized {
        String priority();
    }

    static void sortChecked(List<? extends Prioritized> collection, Supplier<String> debugLabel) {
        collection.sort(SORTER);
        checkPriorityConflict(collection, debugLabel);
    }

    static Object variantEq(GunAmmoVariantSet.Variant variant) {
        if (variant instanceof GunAmmoVariantSet.FireModeInvariantVariant(ResourceLocation gunId)) {
            return gunId;
        }
        if (variant instanceof GunAmmoVariantSet.FireModeVariantVariant dynamic) {
            return dynamic.getGunIds();
        }
        throw new IllegalArgumentException("Unknown variant type: " + variant.getClass().getCanonicalName());
    }

    private static final Comparator<Prioritized> SORTER = Comparator.comparing(set -> transformPriorityStrings(set.priority()));

    private static final Map<String, String> NS_PRIORITY_MAP_TABLE = Map.of(
            "tacz", "   ",
            "std", "    ");

    private static final Map<String, String> PATH_PRIORITY_MAP_TABLE = Map.of(
            "master", " ",
            "main", "  ");

    private static final Map<String, String> PRIORITY_MAP_TABLE = Map.copyOf(Util.make(new HashMap<>(), table -> {
        table.putAll(NS_PRIORITY_MAP_TABLE);
        table.putAll(PATH_PRIORITY_MAP_TABLE);
    }));

    private static String transformPriorityStrings(String masterPriorityString) {
        var rl = ResourceLocation.tryParse(masterPriorityString);
        if (rl == null) {
            return PRIORITY_MAP_TABLE.getOrDefault(masterPriorityString, masterPriorityString);
        }
        var ns0 = rl.getNamespace();
        var ns = NS_PRIORITY_MAP_TABLE.getOrDefault(ns0, ns0);
        var path0 = rl.getPath();
        var path = PATH_PRIORITY_MAP_TABLE.getOrDefault(path0, path0);
        return "%s:%s".formatted(ns, path);
    }

    private static void checkPriorityConflict(Collection<? extends Prioritized> collection, Supplier<String> debugLabel) {
        var unsortedCount = collection.size();
        var sortedCount = collection.stream().map(Prioritized::priority).distinct().count();
        if (sortedCount != unsortedCount) {
            var msg = "Variant set priority conflict found for weapon %s with %d duplicate entries".formatted(debugLabel.get(), unsortedCount - sortedCount);
            throw new GunVariantSetPriorityConflictException(msg);
        }
    }

    private GunVariantSorting() {
    }
}
