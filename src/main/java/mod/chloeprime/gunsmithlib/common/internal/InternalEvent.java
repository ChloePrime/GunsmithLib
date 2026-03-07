package mod.chloeprime.gunsmithlib.common.internal;

import mod.chloeprime.gunsmithlib.api.common.RicochetEvent;
import net.minecraftforge.eventbus.api.Event;

public class InternalEvent<E extends Event> extends Event {
    private final E impl;

    public InternalEvent(E impl) {
        this.impl = impl;
    }

    public E getImpl() {
        return impl;
    }

    public static final class RicochetBounciness extends InternalEvent<RicochetEvent> {
        public RicochetBounciness(RicochetEvent impl) {
            super(impl);
        }
    }
}
