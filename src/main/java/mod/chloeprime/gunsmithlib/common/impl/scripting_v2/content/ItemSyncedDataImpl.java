package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import it.unimi.dsi.fastutil.objects.Object2DoubleOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.SyncedData;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @since 6.0.0
 */
public record ItemSyncedDataImpl(
        ItemStack stack,
        boolean readonly
) implements SyncedData {
    @Override
    public @Nullable Integer optional_get_int(String key) {
        return Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_INTS))
                .map(map -> (Map<String, Integer>) map)
                .map(ints -> ints.get(key))
                .orElse(null);
    }

    @Override
    public @Nullable Double optional_get_number(String key) {
        return Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_NUMBERS))
                .map(map -> (Map<String, Double>) map)
                .map(numbers -> numbers.get(key))
                .orElse(null);
    }

    @Override
    public String optional_get_string(String key) {
        return Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_STRINGS))
                .map(strings -> strings.get(key))
                .orElse(null);
    }

    @Override
    public void set_int(String key, int value) {
        checkWriteAccess();
        var map = Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_INTS))
                .map(Object2IntOpenHashMap::new)
                .orElseGet(Object2IntOpenHashMap::new);
        map.put(key, value);
        stack.set(GunsmithLib.DataComponents.SYNCED_INTS, map);
    }

    @Override
    public void set_number(String key, double value) {
        checkWriteAccess();
        var map = Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_NUMBERS))
                .map(Object2DoubleOpenHashMap::new)
                .orElseGet(Object2DoubleOpenHashMap::new);
        map.put(key, value);
        stack.set(GunsmithLib.DataComponents.SYNCED_NUMBERS, map);
    }

    @Override
    public void set_string(String key, String value) {
        checkWriteAccess();
        var map = Optional.ofNullable(stack.get(GunsmithLib.DataComponents.SYNCED_STRINGS))
                .map(HashMap::new)
                .orElseGet(HashMap::new);
        map.put(key, value);
        stack.set(GunsmithLib.DataComponents.SYNCED_STRINGS, map);
    }

    private void checkWriteAccess() {
        if (readonly) {
            throw new UnsupportedOperationException("Can't write to synced data on client side");
        }
    }
}
