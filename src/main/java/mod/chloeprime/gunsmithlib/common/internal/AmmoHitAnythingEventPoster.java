package mod.chloeprime.gunsmithlib.common.internal;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitAnythingEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitEntityEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoSelfExplodeEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.function.Supplier;

@Mod.EventBusSubscriber
public class AmmoHitAnythingEventPoster {
    private static final Supplier<IEventBus> BUS = Suppliers.memoize(() -> MinecraftForge.EVENT_BUS);

    public static AmmoHitEntityEvent entityPre(AmmoHitEntityEvent event) {
        var bus = BUS.get();
        var anyPre = new AmmoHitAnythingEvent.Pre(event.getLevel(), event.getHitResult(), event.getAmmo());
        var anyPreInternal = new InternalEvent.AmmoHitAnything.Pre(anyPre);
        if (bus.post(anyPreInternal) | bus.post(anyPre)) {
            event.setCanceled(true);
        }
        bus.post(event);
        return event;
    }

    public static void entityPost(AmmoHitEntityEvent event) {
        var bus = BUS.get();
        var anyPost = new AmmoHitAnythingEvent.Post(event.getLevel(), event.getHitResult(), event.getAmmo());
        bus.post(new InternalEvent.AmmoHitAnything.Post(anyPost));
        bus.post(anyPost);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void blockPre(AmmoHitBlockEvent event) {
        var bus = BUS.get();
        var anyPre = new AmmoHitAnythingEvent.Pre(event.getLevel(), event.getHitResult(), event.getAmmo());
        var anyPreInternal = new InternalEvent.AmmoHitAnything.Pre(anyPre);
        if (bus.post(anyPreInternal) | bus.post(anyPre)) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void blockPost(AmmoHitBlockEvent event) {
        var bus = BUS.get();
        var anyPost = new AmmoHitAnythingEvent.Post(event.getLevel(), event.getHitResult(), event.getAmmo());
        bus.post(new InternalEvent.AmmoHitAnything.Post(anyPost));
        bus.post(anyPost);
    }

    public static Event selfPre(AmmoSelfExplodeEvent.Pre event) {
        var bus = BUS.get();
        bus.post(new InternalEvent.AmmoHitAnything.Pre(event));
        bus.post(event);
        return event;
    }

    public static void selfPost(AmmoSelfExplodeEvent.Post event) {
        var bus = BUS.get();
        bus.post(new InternalEvent.AmmoHitAnything.Post(event));
        bus.post(event);
    }
}
