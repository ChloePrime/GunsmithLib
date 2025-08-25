package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import net.neoforged.bus.api.Event;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class InternalBulletCreateEvent extends Event {
    public InternalBulletCreateEvent(BulletCreateEvent impl) {
        this.impl = impl;
    }

    public BulletCreateEvent getImpl() {
        return impl;
    }

    private final BulletCreateEvent impl;
}
