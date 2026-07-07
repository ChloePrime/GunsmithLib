package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.ClientGunScripting;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.Optional;

public final class ModifyDisplayPropertyHook {
    public static <T> T modifyProperty(ItemStack gunItem, String id, Class<T> type, T original) {
        return modifyProperty(gunItem, "gunsmithlib_modify_display_property", id, type, original);
    }

    public static <T> T modifyProperty(ItemStack gunItem, String luaMethodName, String id, Class<T> type, T original) {
        var asr = AnimScriptReady.of(gunItem).orElse(null);
        if (asr == null) {
            return original;
        }
        try {
            return asr.supplyWithContext(() -> Optional.of(asr.script())
                    .map(script -> GsHelper.checkFunction(script.get(luaMethodName)))
                    .map(func -> func.call(CoerceJavaToLua.coerce(asr.context()), LuaValue.valueOf(id), CoerceJavaToLua.coerce(original)))
                    .map(luaValue -> type.cast(CoerceLuaToJava.coerce(luaValue, type)))
                    .orElse(original));
        } catch (Exception exception) {
            GunsmithLib.LOGGER.warn(ClientGunScripting.MARKER, "Failed to modify gun display property {}", id, exception);
            return original;
        }
    }

    private ModifyDisplayPropertyHook() {
    }
}
