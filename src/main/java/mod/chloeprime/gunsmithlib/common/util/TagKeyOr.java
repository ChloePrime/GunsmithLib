package mod.chloeprime.gunsmithlib.common.util;

import cn.chloeprime.commons.ContextUtil;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;

import java.util.Optional;
import java.util.function.Predicate;

public sealed interface TagKeyOr<T> {
    static <T> Optional<TagKeyOr<T>> parse(ResourceKey<? extends Registry<T>> registry, String value) {
        if (value.isEmpty()) {
            return Optional.empty();
        }
        if (value.charAt(0) == '#') {
            var id = ResourceLocation.tryParse(value.substring(1));
            if (id == null) {
                return Optional.empty();
            }
            return Optional.of(new Tag<>(TagKey.create(registry, id)));
        } else {
            var id = ResourceLocation.tryParse(value);
            if (id == null) {
                return Optional.empty();
            }
            return ContextUtil.getRegistryAccess()
                    .registry(registry)
                    .flatMap(reg -> reg.getHolder(ResourceKey.create(registry, id)))
                    .map(Object::new);
        }
    }

    boolean match(Predicate<T> objMatcher, Predicate<TagKey<T>> tagMatcher);

    record Tag<T>(TagKey<T> value) implements TagKeyOr<T> {
        @Override
        public boolean match(Predicate<T> objMatcher, Predicate<TagKey<T>> tagMatcher) {
            return tagMatcher.test(this.value);
        }
    }

    record Object<T>(Holder<T> value) implements TagKeyOr<T> {
        @Override
        public boolean match(Predicate<T> objMatcher, Predicate<TagKey<T>> tagMatcher) {
            return objMatcher.test(this.value.value());
        }
    }
}
