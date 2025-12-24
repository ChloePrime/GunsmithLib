package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import com.google.common.collect.*;
import com.mojang.datafixers.util.Either;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.*;
import java.util.function.Function;

public record GunAmmoVariantSet(
        List<Part> parts,
        Set<ResourceLocation> allGunIds,
        Map<String, Part> partByName,
        Object2IntMap<Part> partToIndex
) {
    public static final Codec<GunAmmoVariantSet> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.list(Part.CODEC).fieldOf("parts").forGetter(GunAmmoVariantSet::parts)
    ).apply(instance, GunAmmoVariantSet::new));

    public GunAmmoVariantSet(List<Part> parts) {
        this(parts, mergeGunIds(parts), gatherPartByName(parts), index(parts));
    }

    public static Optional<GunAmmoVariantSet> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun).flatMap(GunAmmoVariantSet::of);
    }

    public static Optional<GunAmmoVariantSet> of(GunInfo gun) {
        return Optional.ofNullable(GunVariantRegistry.BY_GUN_ID.get(gun.gunId()));
    }

    public record Part(
            String name,
            List<Variant> variants,
            Object2IntMap<ResourceLocation> gunIdToIndex
    ) {
        public static final Codec<Part> CODEC = RecordCodecBuilder.create(instance -> instance
                .group(
                        Codec.STRING.fieldOf("name").forGetter(Part::name),
                        GsHelper.selfOrList(Variant.CODEC).fieldOf("variants").forGetter(Part::variants))
                .apply(instance, Part::new));

        public Part(
                String name,
                List<Variant> variants
        ) {
            this(name, variants, expandVariantIndexToGunIdIndex(index(variants)));
        }

        public Variant getVariantFromGunId(ResourceLocation gunId) {
            var index = Mth.clamp(gunIdToIndex().getInt(gunId), 0, variants().size() - 1);
            return variants().get(index);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    public sealed interface Variant {
        Optional<ResourceLocation> getGunId(FireMode fireMode);
        Collection<ResourceLocation> getGunIds();

        default Optional<ResourceLocation> getGunIdOrFallback(FireMode fireMode) {
            return getGunId(fireMode).or(() -> getGunIds().stream().findFirst());
        }

        Codec<Variant> CODEC = Codec
                .either(FireModeInvariantVariant.CODEC, FireModeVariantVariant.CODEC)
                .xmap(either -> either.map(Function.identity(), Function.identity()),
                        variant -> {
                            if (variant instanceof FireModeInvariantVariant fiv) {
                                return Either.left(fiv);
                            }
                            if (variant instanceof FireModeVariantVariant fvv) {
                                return Either.right(fvv);
                            }
                            throw new IllegalStateException("Sealed class %s's seal is broken".formatted(Variant.class.getSimpleName()));
                        });
    }

    public record FireModeInvariantVariant(
            ResourceLocation gunId
    ) implements Variant {
        public static final Codec<FireModeInvariantVariant> CODEC = ResourceLocation.CODEC.xmap(
                FireModeInvariantVariant::new,
                FireModeInvariantVariant::gunId);

        @Override
        public Optional<ResourceLocation> getGunId(FireMode fireMode) {
            return Optional.of(gunId());
        }

        @Override
        public Collection<ResourceLocation> getGunIds() {
            return List.of(gunId);
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    public record FireModeVariantVariant(
            BiMap<FireMode, ResourceLocation> gunIds
    ) implements Variant {
        public static final Codec<FireModeVariantVariant> CODEC = Codec
                .unboundedMap(GsHelper.enumCodec(FireMode.class), ResourceLocation.CODEC)
                .xmap(map -> new FireModeVariantVariant(ImmutableBiMap.copyOf(map)), FireModeVariantVariant::gunIds);

        @Override
        public Optional<ResourceLocation> getGunId(FireMode fireMode) {
            return Optional.ofNullable(gunIds.get(fireMode));
        }

        @Override
        public Collection<ResourceLocation> getGunIds() {
            return Collections.unmodifiableSet(gunIds.values());
        }

        @Override
        public boolean equals(Object obj) {
            return this == obj;
        }

        @Override
        public int hashCode() {
            return System.identityHashCode(this);
        }
    }

    @Override
    public boolean equals(Object obj) {
        return this == obj;
    }

    @Override
    public int hashCode() {
        return System.identityHashCode(this);
    }

    private static Set<ResourceLocation> mergeGunIds(List<Part> parts) {
        var builder = ImmutableSet.<ResourceLocation>builder();
        for (var part : parts) {
            for (var variant : part.variants()) {
                builder.addAll(variant.getGunIds());
            }
        }
        return builder.build();
    }

    private static Map<String, Part> gatherPartByName(List<Part> parts) {
        var builder = ImmutableMap.<String, Part>builder();
        for (var part : parts) {
            builder.put(part.name(), part);
        }
        return builder.buildKeepingLast();
    }

    private static <T> Object2IntMap<T> index(List<T> objects) {
        var builder = new Object2IntOpenHashMap<T>();
        for (int i = 0; i < objects.size(); i++) {
            builder.put(objects.get(i), i);
        }
        builder.defaultReturnValue(-1);
        return Object2IntMaps.unmodifiable(builder);
    }

    private static Object2IntMap<ResourceLocation> expandVariantIndexToGunIdIndex(Object2IntMap<Variant> variantIndex) {
        var result = new Object2IntOpenHashMap<ResourceLocation>();
        variantIndex.forEach((variant, index) ->  {
            for (var gunId : variant.getGunIds()) {
                result.put(gunId, index.intValue());
            }
        });
        return Object2IntMaps.unmodifiable(result);
    }
}
