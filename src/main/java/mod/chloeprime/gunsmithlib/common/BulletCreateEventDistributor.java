package mod.chloeprime.gunsmithlib.common;

import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

@EventBusSubscriber
public class BulletCreateEventDistributor {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onBulletCreate(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Projectile bullet)) {
            return;
        }
        if (!(bullet.getOwner() instanceof LivingEntity shooter)) {
            return;
        }

        var gun = Gunsmith.getGunInfo(shooter.getMainHandItem()).orElse(null);
        if (gun == null) {
            return;
        }

        var bcEvent = new BulletCreateEvent(bullet, shooter, gun);
        NeoForge.EVENT_BUS.post(bcEvent);
        NeoForge.EVENT_BUS.post(new InternalBulletCreateEvent(bcEvent));
    }
}
