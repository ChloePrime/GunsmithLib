package mod.chloeprime.gunsmithlib.client.papi.framework;

import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.client.GunsmithClientConfig;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.jetbrains.annotations.Nullable;

import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

@EventBusSubscriber(Dist.CLIENT)
public final class PapiTargetCache {
    public static Optional<Rangefinder.Result> current() {
        return Optional.ofNullable(CurrentTarget.get());
    }

    private static final WeakReference<Rangefinder.@Nullable Result> NULL_REF = new WeakReference<>(null);
    private static WeakReference<Rangefinder.@Nullable Result> CurrentTarget = NULL_REF;
    private static final Map<Level, Projectile> PROBES = new WeakHashMap<>();

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        var player = Minecraft.getInstance().player;
        if (player == null || !IGun.mainHandHoldGun(player)) {
            return;
        }
        var partial = ClientProxy.getPartialTicks();
        var muzzle = player.getEyePosition(partial);
        var front = player.getViewVector(partial);
        var probe = PROBES.computeIfAbsent(player.level(), PapiTargetCache::makeProbe);
        probe.setPos(muzzle);

        var maxDistance = (double) GunsmithClientConfig.PAPI_RANGEFINDING_DISTANCE.get();
        var result = Rangefinder.clip(player, muzzle, front, 0, maxDistance);
        CurrentTarget = result.asHitResult().getType() != HitResult.Type.MISS
                ? new WeakReference<>(result)
                : NULL_REF;
    }

    private static Projectile makeProbe(Level level) {
        return new Snowball(EntityType.SNOWBALL, level);
    }
}
