package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.common.entity.RangefinderMarker;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.LevelEvent;
import org.apache.commons.lang3.mutable.MutableObject;

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.WeakHashMap;

@EventBusSubscriber
public class RangefinderSupport {
    public static Projectile getMarkerForEntityClipping(Entity shooter) {
        var level = shooter.level();
        var table = level.isClientSide ? TEST_PROJECTILES_CLIENT : TEST_PROJECTILES_SERVER;
        var markerRef = new MutableObject<Projectile>();
        table.compute(level, (useless, existing) -> {
            var valid = Optional.ofNullable(existing)
                    .map(Reference::get)
                    .orElse(null);
            if (valid != null && valid.isAlive()) {
                markerRef.setValue(valid);
                return existing;
            } else {
                var newMarker = new RangefinderMarker(level);
                markerRef.setValue(newMarker);
                return new WeakReference<>(newMarker);
            }
        });

        var marker = Objects.requireNonNull(markerRef.getValue());
        marker.setOwner(shooter);
        marker.setPos(shooter.getEyePosition());
        return marker;
    }

    private static final Map<LevelAccessor, WeakReference<Projectile>> TEST_PROJECTILES_CLIENT = new WeakHashMap<>();
    private static final Map<LevelAccessor, WeakReference<Projectile>> TEST_PROJECTILES_SERVER = new WeakHashMap<>();

    @SubscribeEvent
    public static void onLevelUnload(LevelEvent.Unload event) {
        if (!(event.getLevel() instanceof Level level)) {
            return;
        }
        var table = level.isClientSide ? TEST_PROJECTILES_CLIENT : TEST_PROJECTILES_SERVER;
        table.remove(level);
    }
}
