package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.GunDisplayProperties;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.ClientGunScripting;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.awt.*;
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

    public static boolean modifyIsBarVisible(boolean original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return original || ModifyDisplayPropertyHook.computeDurabilityBarCached(stack)
                .filter(cache -> cache.getBarWidth().isPresent())
                .isPresent();
    }

    public static int modifyBarWidth(int original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return ModifyDisplayPropertyHook.computeDurabilityBarCached(stack)
                .map(cache -> cache.getBarWidth().orElse(Double.NaN))
                .filter(Double::isFinite)
                .map(width -> (int) Math.round(width * 13))
                .orElse(original);
    }

    public static int modifyBarColor(int original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return ModifyDisplayPropertyHook.computeDurabilityBarCached(stack)
                .flatMap(GunBarCache::getBarColor)
                .map(Color::getRGB)
                .orElse(original);
    }

    private static Optional<GunBarCache> computeDurabilityBarCached(ItemStack stack) {
        var recursiveDepth = GunBarCache.CALL_STACK.get();
        if (recursiveDepth.getValue() > 0) {
            return Optional.empty();
        }
        if (Gunsmith.getGunInfo(stack).isEmpty()) {
            return Optional.empty();
        }
        var cache = GunBarCache.CACHE.computeIfAbsent(stack, _stack -> new GunBarCache());
        var now = GunBarCache.Frame.current();

        if (cache.timestamp.equals(now)) {
            return Optional.of(cache).filter(c -> c.barWidth > -1e-8);
        }
        cache.timestamp = now;

        try {
            recursiveDepth.increment();
            float newWidth = modifyProperty(stack, GunDisplayProperties.DUR_BAR_LENGTH, Float.class, -1.0f);
            if (newWidth > -1e-8) {
                Color oldColor = new Color(stack.getBarColor());
                cache.barWidth = Mth.clamp(newWidth, 0, 1);
                cache.barColor = modifyProperty(stack, GunDisplayProperties.DUR_BAR_COLOR, Color.class, oldColor);
                return Optional.of(cache);
            } else {
                cache.barWidth = Float.NaN;
                cache.barColor = null;
                return Optional.empty();
            }
        } finally {
            recursiveDepth.decrement();
        }
    }

    private ModifyDisplayPropertyHook() {
    }
}
