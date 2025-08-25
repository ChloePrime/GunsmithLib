package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun;

import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.EntityTickEvent;

import javax.annotation.Nonnull;

/**
 * 过热的视听反馈，包含烟雾粒子和过热音效
 */
@EventBusSubscriber
public class OverheatFeedback {
    @SubscribeEvent
    public static void onLivingTick(EntityTickEvent.Post event) {
        // 只在客户端计算
        if (!(event.getEntity() instanceof LivingEntity entity) || !entity.level().isClientSide) {
            return;
        }

        var gunInfo = Gunsmith.getGunInfo(entity.getMainHandItem()).orElse(null);
        if (gunInfo == null) {
            return;
        }
        // 没有配置启用则不启用
        var disabled = GunsmithLibGunDataExtension.of(gunInfo)
                .filter(GunsmithLibGunDataExtension::enableOverheatFeedback)
                .isEmpty();
        if (disabled) {
            return;
        }
        // 没有过热时不触发
        var isOverheated = gunInfo.gunItem().isOverheatLocked(gunInfo.gunStack());
        if (!isOverheated) {
            return;
        }
        var updateInterval = 2;
        var now = entity.level().getGameTime();
        var salt = entity.hashCode();
        if ((now + salt) % updateInterval == 0) {
            spawnCooldownSmoke(entity);
        }
    }

    public static void tryPlayCooldownSound(@Nonnull Entity shooter, ItemStack gun) {
        // 音效是在服务端放的
        if (shooter.level().isClientSide) {
            return;
        }
        var gunInfo = Gunsmith.getGunInfo(gun).orElse(null);
        if (gunInfo == null) {
            return;
        }
        // 没有配置启用则不启用
        var disabled = GunsmithLibGunDataExtension.of(gunInfo)
                .filter(GunsmithLibGunDataExtension::enableOverheatFeedback)
                .isEmpty();
        if (disabled) {
            return;
        }
        playCooldownSound(shooter);
    }

    public static void playCooldownSound(Entity shooter) {
        if (shooter.level().isClientSide) {
            return;
        }
        shooter.level().playSound(null, shooter.getX(), shooter.getEyeY(), shooter.getZ(), SoundEvents.FIRE_EXTINGUISH, shooter.getSoundSource(), 1, 0.8F);
    }

    private static void spawnCooldownSmoke(LivingEntity shooter) {
        var muzzle = Gunsmith.getProximityMuzzlePos(shooter);
        shooter.level().addParticle(
                ParticleTypes.POOF,
                muzzle.x(), muzzle.y(), muzzle.z(),
                0, 0.25, 0
        );
    }
}
