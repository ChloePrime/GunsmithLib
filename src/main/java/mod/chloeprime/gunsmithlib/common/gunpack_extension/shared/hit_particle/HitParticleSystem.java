package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle;

import com.tacz.guns.resource.pojo.data.gun.ExplosionData;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.SafetyDistanceSystem;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.common.util.LinearAlgebraTypes;
import mod.chloeprime.gunsmithlib.compat.aaap.AaaParticleProxy;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
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
    public static void onAmmoHitAnything(InternalEvent.AmmoHitAnything.Post eventWrapper) {
        var event = eventWrapper.getImpl();
        var ammo = event.getAmmo();
        onAmmoHitAnything(ammo, event.getHitResult(), ammo.getGunId());
    }

    public static void spawnAt(Level level, Vector3d pos, HitParticleData data) {
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
            AaaParticleProxy.addParticle(level, true, id, mojPos, normal, 1, data.getAaaParticleData());
            return;
        }
        if (!(level instanceof ServerLevel sl)) {
            return;
        }
        var particle = data.getParticle(level.registryAccess());
        if (particle == null) {
            return;
        }
        // 释放粒子！
        for (var player : sl.players()) {
            boolean isFar = data.isExplosiveParticleAlternate();
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
        var hitPosAaa = hit.getLocation().add(ammo.getDeltaMovement().normalize().scale(-0.125));
        // 获取当前生效的粒子 data
        var gunInfo = Gunsmith.getGunInfo(Gunsmith.createGunItemFromId(gunId, level.registryAccess())).orElse(null);
        if (gunInfo == null) {
            return;
        }

        for (var data : HitParticleData.of(gunInfo)) {
            if (data == null) {
                continue;
            }
            var isExplodeEvent = ammo instanceof EntityKineticBulletAccessor bullet && bullet.getExplosion();
            if (!data.isActivated(isExplodeEvent)) {
                continue;
            }
            var isFar = data.isExplosiveParticleAlternate();
            // 安全距离内爆炸时不播放爆炸粒子
            if (isFar && SafetyDistanceSystem.isVanillaExplosionInSafeDistance(ammo, hit.getLocation())) {
                return;
            }
            var isAaa = data.isAaaParticle();
            if (isAaa == Boolean.TRUE) {
                var id = data.getParticleId();
                if (id == null) {
                    continue;
                }
                var scale = (isExplodeEvent && isFar) ? getExplodeScale(ammo, gunInfo) : 1;
                AaaParticleProxy.addParticle(level, true, id, hitPosAaa, normal, scale, data.getAaaParticleData());
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

    private static float getExplodeScale(Projectile ammo, GunInfo gunInfo) {
        if (!(ammo instanceof EntityKineticBulletAccessor bullet)) {
            return 1;
        } else if (!bullet.getExplosion()) {
            return 1;
        } else {
            float baseRadius = Optional.ofNullable(gunInfo.index().getBulletData().getExplosionData())
                    .map(ExplosionData::getRadius)
                    .orElse(-1F);
            if (baseRadius <= 0) {
                return 1;
            } else {
                return bullet.getExplosionRadius() / baseRadius;
            }
        }
    }

    public static boolean isHidingExplodeParticle(ItemStack stack) {
        for (var datum : HitParticleData.of(stack)) {
            // 防止 AAA 限定粒子在没有安装 AAA 时顶掉原版爆炸
            if (!datum.isActivated(true)) {
                continue;
            }
            if (datum.isExplosiveParticleAlternate()) {
                return true;
            }
        }
        return false;
    }
}
