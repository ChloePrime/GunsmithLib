package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle;

import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import mod.chloeprime.gunsmithlib.api.common.AmmoHitEntityEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.LinearAlgebraTypes;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.joml.Vector3d;

import java.util.Optional;

@EventBusSubscriber
public class HitParticleSystem {
    @SubscribeEvent
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        var ammo = event.getAmmo();
        onAmmoHitAnything(ammo, event.getHitResult(), ammo.getGunId());
    }

    @SubscribeEvent
    public static void onAmmoHitEntity(AmmoHitEntityEvent event) {
        var ammo = event.getAmmo();
        onAmmoHitAnything(ammo, event.getHitResult(), ammo.getGunId());
    }

    @SuppressWarnings("deprecation")
    public static void spawnAt(Level level, Vector3d pos, HitParticleData data) {
        var isFar = data.isExplosiveParticleAlternate();
        var isAaa = data.isAaaParticle();
        if (isAaa == Boolean.FALSE && AaaParticleProxy.INSTALLED) {
            return;
        }
        if (isAaa == Boolean.TRUE) {
            var id = data.getParticleId();
            if (id == null) {
                return;
            }
            var normal = new Vec3(data.getDX(), data.getDY(), data.getDZ()).normalize();
            var mojPos = LinearAlgebraTypes.joml2moj(pos);
            AaaParticleProxy.addParticle(level, isFar, id, mojPos, normal, data.getAaaParticleData());
            return;
        }
        if (!(level instanceof ServerLevel sl)) {
            return;
        }
        var particle = data.getParticle(BuiltInRegistries.PARTICLE_TYPE.asLookup());
        if (particle == null) {
            return;
        }
        // 释放粒子！
        for (var player : sl.players()) {
            sl.sendParticles(player, particle, isFar, pos.x(), pos.y(), pos.z(), data.getCount(), data.getDX(), data.getDY(), data.getDZ(), data.getSpeed());
        }
    }

    private static void onAmmoHitAnything(Projectile ammo, HitResult hit, ResourceLocation gunId) {
        var level = Optional.ofNullable(ammo).map(Entity::level).orElse(null);
        if (ammo == null || level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }

        var normal = hit instanceof BlockHitResult blockHit
                ? Vec3.atLowerCornerOf(blockHit.getDirection().getNormal())
                : ammo.getLookAngle().normalize().scale(-1);
        var hitPos = hit.getLocation().add(normal.scale(0.25));
        // 获取当前生效的粒子 data
        var gunIdStack = Gunsmith.createGunItemFromId(gunId, level.registryAccess());

        for (var data : HitParticleData.of(gunIdStack)) {
            if (data == null) {
                continue;
            }
            var isFar = data.isExplosiveParticleAlternate();
            var isAaa = data.isAaaParticle();
            if (isAaa == Boolean.FALSE && AaaParticleProxy.INSTALLED) {
                continue;
            }
            if (isAaa == Boolean.TRUE) {
                var id = data.getParticleId();
                if (id == null) {
                    continue;
                }
                AaaParticleProxy.addParticle(level, isFar, id, hitPos, normal, data.getAaaParticleData());
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
                particle = data.getParticle(level.registryAccess());
            }
            if (particle == null) {
                continue;
            }
            // 释放粒子！
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
