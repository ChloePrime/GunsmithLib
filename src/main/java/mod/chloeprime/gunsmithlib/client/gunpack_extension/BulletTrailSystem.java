package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import com.google.common.collect.MapMaker;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Map;

/**
 * AAA 弹道轨迹系统
 *
 * @since 6.4
 */
@EventBusSubscriber(Dist.CLIENT)
public final class BulletTrailSystem {
    private static final Map<Entity, Boolean> CACHE = new MapMaker().weakKeys().makeMap();

    public static boolean hideVanillaTrailCached(Entity entity, ResourceLocation gunId) {
        if (!ModInstallationStatus.AAA_PARTICLES_INSTALLED) {
            return false;
        }
        return CACHE.computeIfAbsent(entity, _entity -> hideVanillaTrail(gunId, entity.registryAccess()));
    }

    public static boolean hideVanillaTrail(ResourceLocation gunId, RegistryAccess registryAccess1211) {
        if (!ModInstallationStatus.AAA_PARTICLES_INSTALLED) {
            return false;
        }
        return GunsmithLibGunDataExtension
                .forGunOrAmmo(Gunsmith.createGunItemFromId(gunId, registryAccess1211), data -> data.getBulletTrails() != null
                        ? data.hideVanillaBulletTrails()
                        : null)
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
        for (HitParticleData trail : trails) {
            AaaParticleProxy.bindParticleZP(bullet, trail.getParticleId(), Vec3.ZERO, 1, trail.getAaaParticleData());
        }
    }

    private BulletTrailSystem() {
    }
}
