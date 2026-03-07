package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public final class InternalBulletCreateEvent extends InternalEvent<BulletCreateEvent> {
    public InternalBulletCreateEvent(BulletCreateEvent impl) {
        super(impl);
    }
}
