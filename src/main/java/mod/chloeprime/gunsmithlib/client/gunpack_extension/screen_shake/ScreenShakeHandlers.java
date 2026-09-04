package mod.chloeprime.gunsmithlib.client.gunpack_extension.screen_shake;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.Util;
import net.minecraft.resources.ResourceLocation;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.RPLScreenShakeHandlerClient;

import java.util.Arrays;
import java.util.List;

/**
 * @since 7.0.0
 */
public final class ScreenShakeHandlers {
    public static ScreenShakeHandler next() {
        var fetched = INSTANCES[next];
        fetched.clearEffects();
        next = (next + 1) % MAX_PARALLELISM;
        return fetched;
    }

    public static List<ScreenShakeHandler> all() {
        return Arrays.asList(INSTANCES);
    }

    private static final int MAX_PARALLELISM = 32;
    private static int next;

    private static final ResourceLocation[] IDS = Util.make(new ResourceLocation[MAX_PARALLELISM], ids -> {
        for (int i = 0; i < ids.length; i++) {
            ids[i] = GunsmithLib.loc("screen_shaker_%d".formatted(i));
        }
    });

    private static final ScreenShakeHandler[] INSTANCES = Util.make(new ScreenShakeHandler[MAX_PARALLELISM], instances -> {
        for (int i = 0; i < instances.length; i++) {
            instances[i] = new ScreenShakeHandler();
        }
    });

    static void init() {
        for (int i = 0; i < MAX_PARALLELISM; i++) {
            RPLScreenShakeHandlerClient.registerModScreenShakeHandler(IDS[i], INSTANCES[i]);
        }
    }

    private ScreenShakeHandlers() {
    }
}
