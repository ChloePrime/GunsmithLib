package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

import java.util.LinkedHashSet;
import java.util.Set;

public interface SpecialHurtable {
    Set<EntityType<?>> UNSUPPORTED_TYPES = new LinkedHashSet<>();

    TagKey<EntityType<?>> USE_SPECIAL_HURT = TagKey.create(Registries.ENTITY_TYPE, GunsmithLib.loc("use_special_hurt"));

    default void gunsmith$addUnsupportedTypeForSpecialHurt(EntityType<?> type, Throwable cause) {
        UNSUPPORTED_TYPES.add(type);
        if (GunsmithLib.LOGGER.isWarnEnabled()) {
            GunsmithLib.LOGGER.warn("Tag {} is unsupported on entity type {} because of {}",
                    USE_SPECIAL_HURT.location(),
                    BuiltInRegistries.ENTITY_TYPE.getKey(type),
                    cause.getClass().getSimpleName(),
                    cause);
        }
    }

    boolean gunsmith$usingSpecialHurt();
    void gunsmith$beginSpecialHurt();
    void gunsmith$endSpecialHurt();
    HurtFunction1 getSpecialHurtFunction1();
    HurtFunction2 getSpecialHurtFunction2();
    FloatConsumer getSpecialSetHealthFunction();
}
