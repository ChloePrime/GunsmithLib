package mod.chloeprime.gunsmithlib.common.internal;

import com.google.common.base.Suppliers;
import com.google.common.collect.MapMaker;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitAnythingEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitEntityEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoSelfExplodeEvent;
import net.minecraft.world.entity.Entity;
import net.neoforged.bus.api.*;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

import java.util.Collections;
import java.util.Set;
import java.util.function.Supplier;

@EventBusSubscriber
public class AmmoHitAnythingEventPoster {
    private static final Supplier<IEventBus> BUS = Suppliers.memoize(() -> NeoForge.EVENT_BUS);
    private static final Set<Entity> EXEMPTS = Collections.newSetFromMap(new MapMaker().weakKeys().makeMap());

    public static AmmoHitEntityEvent entityPre(AmmoHitEntityEvent event) {
        var bus = BUS.get();
        var anyPre = new AmmoHitAnythingEvent.Pre(event.getLevel(), event.getHitResult(), event.getAmmo());
        var anyPreInternal = new InternalEvent.AmmoHitAnything.Pre(anyPre);
        if (bus.post(anyPreInternal).isCanceled() | bus.post(anyPre).isCanceled()) {
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
        if (bus.post(anyPreInternal).isCanceled() | bus.post(anyPre).isCanceled()) {
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

    public static void exemptFromSelfExplodeEvent(Entity bullet) {
        EXEMPTS.add(bullet);
    }

    public static boolean isExemptedFromSelfExplodeEvent(Entity bullet) {
        return EXEMPTS.contains(bullet);
    }

    public static ICancellableEvent selfPre(AmmoSelfExplodeEvent.Pre event) {
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
