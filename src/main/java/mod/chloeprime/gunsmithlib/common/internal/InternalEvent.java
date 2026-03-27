package mod.chloeprime.gunsmithlib.common.internal;

import mod.chloeprime.gunsmithlib.api.common.AmmoHitAnythingEvent;
import mod.chloeprime.gunsmithlib.api.common.RicochetEvent;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class InternalEvent<E extends Event> extends Event implements ICancellableEvent {
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

    public static sealed class AmmoHitAnything<E extends AmmoHitAnythingEvent> extends InternalEvent<E> {
        private AmmoHitAnything(E impl) {
            super(impl);
        }

        public static final class Pre extends AmmoHitAnything<AmmoHitAnythingEvent.Pre> implements ICancellableEvent {
            public Pre(AmmoHitAnythingEvent.Pre impl) {
                super(impl);
            }
        }

        public static final class Post extends AmmoHitAnything<AmmoHitAnythingEvent.Post> {
            public Post(AmmoHitAnythingEvent.Post impl) {
                super(impl);
            }
        }
    }
}
