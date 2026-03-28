package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check;

import com.google.gson.ExclusionStrategy;
import com.google.gson.FieldAttributes;

public final class ArcanaExtrasExcludeStrategy implements ExclusionStrategy {
    public static final ArcanaExtrasExcludeStrategy INSTANCE = new ArcanaExtrasExcludeStrategy();

    @Override
    public boolean shouldSkipField(FieldAttributes f) {
        return false;
    }

    @Override
    public boolean shouldSkipClass(Class<?> clazz) {
        return ArcanaCheckSystem.ARCANA_INSTALLED && clazz == ArcanaExtras.class;
    }

    private ArcanaExtrasExcludeStrategy() {
    }
}
