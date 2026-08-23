package mod.chloeprime.gunsmithlib.common;

import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.common.tacz.EntityKineticBulletAccessor;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;

@Mod.EventBusSubscriber
public class BulletCreateEventDistributor {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBulletCreate(EntityJoinLevelEvent event) {
        onBulletCreate0(event, false);
    }

    @ApiStatus.Internal
    public static void onBulletCreate0(EntityJoinLevelEvent event, boolean isClientFixCall) {
        if (!isClientFixCall && event.getEntity().level().isClientSide) {
            return;
        }
        if (!(event.getEntity() instanceof Projectile bullet)) {
            return;
        }
        if (!(bullet.getOwner() instanceof LivingEntity shooter)) {
            return;
        }

        var gunId = bullet instanceof EntityKineticBulletAccessor accessor
                ? accessor.getGunId()
                : null;
        var gun = Gunsmith.getGunInfo(shooter.getMainHandItem())
                .filter(gi -> gunId == null || gunId.equals(gi.gunId()))
                .or(() -> Gunsmith.getGunInfo(Gunsmith.createGunItemFromId(gunId)))
                .orElse(null);
        if (gun == null) {
            return;
        }

        var bcEvent = new BulletCreateEvent(bullet, shooter, gun);
        MinecraftForge.EVENT_BUS.post(bcEvent);
        MinecraftForge.EVENT_BUS.post(new InternalBulletCreateEvent(bcEvent));
    }
}
