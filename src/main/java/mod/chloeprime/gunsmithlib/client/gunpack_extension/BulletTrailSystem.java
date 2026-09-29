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
        for (HitParticleData trail : trails) {
            AaaParticleProxy.bindParticleZP(bullet, trail.getParticleId(), Vec3.ZERO, 1, trail.getAaaParticleData());
        }
    }

    private BulletTrailSystem() {
    }
}
