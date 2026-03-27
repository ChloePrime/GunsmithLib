package mod.chloeprime.gunsmithlib.common.util;

import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

public class GsHelper1211 {
    public static boolean isCanceled(Event event) {
        return event instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }
}
