package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.item.ItemStack;

import java.util.*;

public record AmmoVariantStorage(
        Map<String, OfSinglePart> byPartStorage
) {
    public static final AmmoVariantStorage DEFAULT = new AmmoVariantStorage(Collections.emptyMap());

    public static final Codec<AmmoVariantStorage> CODEC = Codec
            .unboundedMap(Codec.STRING, OfSinglePart.CODEC)
            .xmap(AmmoVariantStorage::new, AmmoVariantStorage::byPartStorage);

    public static AmmoVariantStorage of(ItemStack stack) {
        return stack.getOrDefault(GunsmithLib.DataComponents.AMMO_VARIANT_STORAGE, DEFAULT);
    }

    public static void set(ItemStack stack, AmmoVariantStorage value) {
        var cleaned = cleanup(value);
        if (cleaned.isEmpty()) {
            stack.remove(GunsmithLib.DataComponents.AMMO_VARIANT_STORAGE);
        } else {
            stack.set(GunsmithLib.DataComponents.AMMO_VARIANT_STORAGE, cleaned.get());
        }
    }

    public static Optional<AmmoVariantStorage> cleanup(AmmoVariantStorage original) {
        var table = new HashMap<>(original.byPartStorage());
        table.entrySet().removeIf(entry -> OfSinglePart.DEFAULT.equals(entry.getValue()));
        return table.isEmpty() ? Optional.empty() : Optional.of(new AmmoVariantStorage(Collections.unmodifiableMap(table)));
    }

    public record OfSinglePart(
            int selectedVariant,
            int storedAmmo,
            FireMode fireMode
    ) {
        public static final OfSinglePart DEFAULT = new OfSinglePart(0, 0, FireMode.UNKNOWN);

        public static final Codec<OfSinglePart> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.INT.fieldOf("selected_variant").forGetter(OfSinglePart::selectedVariant),
                Codec.INT.fieldOf("stored_ammo").forGetter(OfSinglePart::storedAmmo),
                GsHelper.enumCodec(FireMode.class).optionalFieldOf("fire_mode", FireMode.UNKNOWN).forGetter(OfSinglePart::fireMode)
        ).apply(instance, OfSinglePart::new));
    }
}
