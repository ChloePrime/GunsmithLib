package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import com.google.common.collect.MapMaker;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.index.CommonGunIndex;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo.GunsmithLibAmmoDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;

/**
 * AAA 弹道轨迹系统
 *
 * @since 6.4
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class BulletTrailSystem {
    private static final Map<Entity, Boolean> CACHE = new MapMaker().weakKeys().makeMap();
    private static final Queue<Runnable> DELAY_QUEUE = new ArrayDeque<>();

    public static boolean hideVanillaTrailCached(Entity entity, ResourceLocation gunId) {
        if (!ModInstallationStatus.AAA_PARTICLES_INSTALLED) {
            return false;
        }
        return CACHE.computeIfAbsent(entity, _entity -> hideVanillaTrail(gunId));
    }

    public static boolean hideVanillaTrail(ResourceLocation gunId) {
        if (!ModInstallationStatus.AAA_PARTICLES_INSTALLED) {
            return false;
        }
        return TimelessAPI.getCommonGunIndex(gunId)
                .map(CommonGunIndex::getGunData)
                .flatMap(gunData -> GunsmithLibAmmoDataExtension.forGunOrAmmo(gunData, data -> data.getBulletTrails() != null
                        ? data.hideVanillaBulletTrails()
                        : null))
                .orElse(false);
    }

    @SubscribeEvent
    public static void onBulletCreate(BulletCreateEvent event) {
        if (!event.getBullet().level().isClientSide()) {
            return;
        }
        // Read trail particle data
        var trails = ClientEffekHelper.get(event.getGun(), GunsmithLibSharedDataExtension::getBulletTrails);
        if (trails.isEmpty()) {
            return;
        }
        var bullet = event.getBullet();
        DELAY_QUEUE.offer(() -> {
            if (!bullet.isAlive()) {
                return;
            }
            for (HitParticleData trail : trails) {
                AaaParticleProxy.bindParticleZP(bullet, trail.getParticleId(), Vec3.ZERO, 1, trail.getAaaParticleData());
            }
        });
    }

    @SubscribeEvent
    public static void runQueuedOnRenderTick(TickEvent.RenderTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
            return;
        }
        Runnable code;
        while ((code = DELAY_QUEUE.poll()) != null) {
            code.run();
        }
    }

    private BulletTrailSystem() {
    }
}
