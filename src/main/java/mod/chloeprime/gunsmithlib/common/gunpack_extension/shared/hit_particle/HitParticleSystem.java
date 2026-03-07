package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle;

import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitEntityEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber
public class HitParticleSystem {
    @SubscribeEvent
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        var ammo = event.getAmmo();
        onAmmoHitAnything(ammo, event.getHitResult(), ammo.getGunId());
    }

    @SubscribeEvent
    public static void onAmmoHitEntity(AmmoHitEntityEvent event) {
        var ammo = event.ammo();
        onAmmoHitAnything(ammo, event.hitResult(), ammo.getGunId());
    }

    private static void onAmmoHitAnything(Projectile ammo, HitResult hit, ResourceLocation gunId) {
        var level = Optional.ofNullable(ammo).map(Entity::level).orElse(null);
        if (ammo == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var normal = hit instanceof BlockHitResult blockHit
                ? Vec3.atLowerCornerOf(blockHit.getDirection().getNormal())
                : ammo.getLookAngle().normalize().scale(-1);
        // 获取当前生效的粒子 data
        var gunIdStack = Gunsmith.createGunItemFromId(gunId);
        for (var data : HitParticleData.of(gunIdStack)) {
            if (data == null) {
                continue;
            }
            var isAaa = data.isAaaParticle();
            if (isAaa == Boolean.TRUE) {
                // TODO AAA Particles 适配
                continue;
            }
            // 解码粒子 id 和配置
            ParticleOptions particle;
            var isAdaptiveBlock = data.isAdaptiveBlockParticle();
            if (hit instanceof BlockHitResult blockHit && data.isAdaptiveBlockParticle() != null) {
                // 命中方块
                if (isAdaptiveBlock == Boolean.FALSE) {
                    continue;
                }
                var block = level.getBlockState(blockHit.getBlockPos());
                particle = new BlockParticleOption(ParticleTypes.BLOCK, block);
            } else {
                // 命中实体
                if (isAdaptiveBlock == Boolean.TRUE) {
                    continue;
                }
                // noinspection deprecation
                particle = data.getParticle(BuiltInRegistries.PARTICLE_TYPE.asLookup());
            }
            if (particle == null) {
                continue;
            }
            // 释放粒子！
            var isFar = data.isExplosiveParticleAlternate();
            var hitPos = hit.getLocation();
            for (var player : serverLevel.players()) {
                serverLevel.sendParticles(player, particle, isFar, hitPos.x(), hitPos.y(), hitPos.z(), data.getCount(), data.getDX(), data.getDY(), data.getDZ(), data.getSpeed());
            }
        }
    }

    public static boolean isHidingExplodeParticle(ItemStack stack) {
        for (var datum : HitParticleData.of(stack)) {
            if (datum.isExplosiveParticleAlternate()) {
                return true;
            }
        }
        return false;
    }
}
