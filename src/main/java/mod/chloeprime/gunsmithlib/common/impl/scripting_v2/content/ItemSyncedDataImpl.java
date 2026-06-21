package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.SyncedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * @since 6.0.0
 */
public record ItemSyncedDataImpl(
        ItemStack stack,
        boolean readonly,
        Consumer<ItemStack> writeback
) implements SyncedData {
    public ItemSyncedDataImpl(
            ItemStack stack,
            boolean readonly
    ) {
        this(stack, readonly, result -> {});
    }

    public static final String PDK_INT = GunsmithLib.loc("synced_ints").toString();
    public static final String PDK_DBL = GunsmithLib.loc("synced_doubles").toString();
    public static final String PDK_STR = GunsmithLib.loc("synced_strings").toString();

    @Override
    public @Nullable Integer optional_get_int(String key) {
        return getStorage(PDK_INT)
                .filter(ints -> ints.contains(key, Tag.TAG_INT))
                .map(ints -> ints.getInt(key))
                .orElse(null);
    }

    @Override
    public @Nullable Double optional_get_number(String key) {
        return getStorage(PDK_DBL)
                .filter(numbers -> numbers.contains(key, Tag.TAG_DOUBLE))
                .map(numbers -> numbers.getDouble(key))
                .orElse(null);
    }

    @Override
    public String optional_get_string(String key) {
        return getStorage(PDK_STR)
                .filter(strings -> strings.contains(key, Tag.TAG_STRING))
                .map(strings -> strings.getString(key))
                .orElse(null);
    }

    @Override
    public void set_int(String key, int value) {
        checkWriteAccess();
        getOrCreateStorage(PDK_INT).putInt(key, value);
        writeback().accept(this.stack);
    }

    @Override
    public void set_number(String key, double value) {
        checkWriteAccess();
        getOrCreateStorage(PDK_DBL).putDouble(key, value);
        writeback().accept(this.stack);
    }

    @Override
    public void set_string(String key, String value) {
        checkWriteAccess();
        getOrCreateStorage(PDK_STR).putString(key, value);
        writeback().accept(this.stack);
    }

    private Optional<CompoundTag> getStorage(String pdk) {
        return Optional.ofNullable(stack.getTag())
                .filter(itemTag -> itemTag.contains(pdk, Tag.TAG_COMPOUND))
                .map(itemTag -> itemTag.getCompound(pdk));
    }

    private void checkWriteAccess() {
        if (readonly) {
            throw new UnsupportedOperationException("Can't write to synced data on client side");
        }
    }

    private @Nonnull CompoundTag getOrCreateStorage(String pdk) {
        var itemTag = stack.getOrCreateTag();
        if (itemTag.contains(pdk, Tag.TAG_COMPOUND)) {
            return itemTag.getCompound(pdk);
        } else {
            var storage = new CompoundTag();
            itemTag.put(pdk, storage);
            return storage;
        }
    }
}
