package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import com.google.common.collect.MapMaker;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Map;

/**
 * AAA 弹道轨迹系统
 *
 * @since 6.4
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class BulletTrailSystem {
    private static final Map<Entity, Boolean> CACHE = new MapMaker().weakKeys().makeMap();

    public static boolean hideVanillaTrailCached(Entity entity, ResourceLocation gunId) {
        return CACHE.computeIfAbsent(entity, _entity -> hideVanillaTrail(gunId));
    }

    public static boolean hideVanillaTrail(ResourceLocation gunId) {
        return GunsmithLibGunDataExtension
                .forGunOrAmmo(Gunsmith.createGunItemFromId(gunId), data -> data.getBulletTrails() != null
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
