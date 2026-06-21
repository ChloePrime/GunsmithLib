package mod.chloeprime.gunsmithlib.common.scripting;

import com.tacz.guns.resource.CommonAssetsManager;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.slf4j.Marker;
import org.slf4j.MarkerFactory;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public final class ScriptBakery {
    private static final Marker MARKER = MarkerFactory.getMarker("GunsmithLib Script Bakery");

    public record BakedScript(@Nullable LuaTable clazz, @Nonnull LuaTable parameters) {
        public BakedScript {
            Objects.requireNonNull(parameters);
        }
    }

    public static BakedScript createDefault() {
        return new BakedScript(null, new LuaTable());
    }

    public static Optional<BakedScript> bake(@Nullable ResourceLocation scriptId, @Nullable Map<String, Object> parameters) {
        if (scriptId == null) {
            return Optional.empty();
        }
        var clazz = Optional.ofNullable(CommonAssetsManager.getInstance())
                .map(manager -> manager.getScript(scriptId))
                .orElse(null);
        if (clazz == null) {
            GunsmithLib.LOGGER.warn(MARKER, "Script {} not found", scriptId);
            return Optional.empty();
        }
        var params = Optional.ofNullable(parameters)
                .map(ScriptBakery::bakeParameters)
                .orElseGet(LuaTable::new);
        return Optional.of(new BakedScript(clazz, params));
    }

    private static @Nullable LuaTable bakeParameters(Map<String, Object> javaParams) {
        if (javaParams.isEmpty()) {
            return null;
        }
        var luaParams = new LuaTable();
        for (var entry : javaParams.entrySet()) {
            luaParams.set(entry.getKey(), CoerceJavaToLua.coerce(entry.getValue()));
        }
        return luaParams;
    }

    private ScriptBakery() {
    }
}
