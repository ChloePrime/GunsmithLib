package mod.chloeprime.gunsmithlib.common.util;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.ExtraCodecs;
import net.minecraft.util.profiling.ProfilerFiller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;
import java.util.Optional;

public abstract class SimpleCodecResourceReloadListener<T> extends SimplePreparableReloadListener<Map<ResourceLocation, T>> {
    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    public static final Codec<Map<ResourceLocation, JsonElement>> RAW_JSON_CODEC = Codec.unboundedMap(
            ResourceLocation.CODEC,
            ExtraCodecs.JSON
    );

    private final Codec<T> codec;
    private final Delegate delegate;
    private final Logger logger;
    protected volatile Map<ResourceLocation, JsonElement> raw;

    public SimpleCodecResourceReloadListener(Codec<T> codec, Gson pGson, String directory) {
        this.codec = codec;
        this.delegate = new Delegate(pGson, directory);
        this.logger = LoggerFactory.getLogger("%s: %s".formatted(getClass().getSimpleName(), directory));
    }

    @Override
    @ParametersAreNonnullByDefault
    protected @Nonnull Map<ResourceLocation, T> prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
        var jsons = this.raw = delegate.prepare(resourceManager, profiler);
        return decodeFromJson(jsons);
    }

    protected Map<ResourceLocation, T> decodeFromJson(Map<ResourceLocation, JsonElement> rawData) {
        var builder = ImmutableMap.<ResourceLocation, T>builder();
        rawData.forEach((id, json) -> {
            var decoded = codec.decode(JsonOps.INSTANCE, json);
            if (decoded.error().isPresent()) {
                logger.error("failed to parse resource {}: {}", id, decoded.error().get().message());
                return;
            }
            if (decoded.result().isPresent()) {
                builder.put(id, decoded.result().get().getFirst());
            }
        });
        return builder.build();
    }

    protected static Optional<CompoundTag> encodeJsonToNBT(Map<ResourceLocation, JsonElement> jsons) {
        var encoded = RAW_JSON_CODEC.encodeStart(NbtOps.INSTANCE, jsons);
        if (encoded.error().isPresent()) {
            GunsmithLib.LOGGER.error("Failed to encode gun variant registry: {}", encoded.error().get().message());
            return Optional.empty();
        }
        if (encoded.result().isPresent()) {
            Tag tag = encoded.result().get();
            if (tag instanceof CompoundTag compound) {
                return Optional.of(compound);
            } else {
                GunsmithLib.LOGGER.error("Not a compound tag, this should not happen, actual tag type: {}", tag.getClass().getSimpleName());
                return Optional.empty();
            }
        }
        throw new IllegalStateException();
    }

    protected static Optional<Map<ResourceLocation, JsonElement>> decodeJsonFromNBT(CompoundTag nbt) {
        var json = RAW_JSON_CODEC.decode(NbtOps.INSTANCE, nbt);
        if (json.error().isPresent()) {
            GunsmithLib.LOGGER.error("Failed to decodeFromJson gun variant registry: {}", json.error().get().message());
            return Optional.empty();
        }
        if (json.result().isPresent()) {
            return json.result().map(Pair::getFirst);
        }
        throw new IllegalStateException();
    }

    /**
     * Users should implement this :)
     */
    @Override
    @ParametersAreNonnullByDefault
    protected abstract void apply(Map<ResourceLocation, T> data, ResourceManager resourceManager, ProfilerFiller profiler);

    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private static class Delegate extends SimpleJsonResourceReloadListener {
        public Delegate(Gson gson, String directory) {
            super(gson, directory);
        }

        @Override
        protected Map<ResourceLocation, JsonElement> prepare(ResourceManager pResourceManager, ProfilerFiller pProfiler) {
            return super.prepare(pResourceManager, pProfiler);
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> pObject, ResourceManager pResourceManager, ProfilerFiller pProfiler) {
            throw new AbstractMethodError();
        }
    }
}
