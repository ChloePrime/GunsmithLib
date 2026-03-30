package mod.chloeprime.gunsmithlib.client.bugfix.tacz;

import com.google.common.collect.MapMaker;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public class TracerStartPosFixStatics {
    public static final Map<Entity, Double> CLIENT_RANGES = new MapMaker().weakKeys().makeMap();

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        var bullet = event.getBullet();
        if (!bullet.level().isClientSide()) {
            return;
        }
        var shooter = event.getShooter();
        @SuppressWarnings("UnstableApiUsage")
        int pierce = Optional.ofNullable(IGunOperator.fromLivingEntity(shooter).getCacheProperty())
                .map(cache -> cache.getCache(GunProperties.PIERCE))
                .orElse(0);
        // DeltaMovement is not synced at this time in MC 1.21.1
        double maxRange = 50;
        double range = Rangefinder
                .clip(shooter, bullet.position(), bullet.getDeltaMovement().normalize(), pierce, maxRange)
                .getLengthOrDefault(maxRange);
        CLIENT_RANGES.put(bullet, range);
    }
}
