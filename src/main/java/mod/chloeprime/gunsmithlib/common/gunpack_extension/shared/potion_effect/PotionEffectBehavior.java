package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect;

import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import mod.chloeprime.gunsmithlib.common.entity.AreaEffectCloud3D;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber
public class PotionEffectBehavior {
    private static final ArrayList<PotionEffectData> BUFFER = new ArrayList<>(50);

    @SubscribeEvent
    @SuppressWarnings("unchecked")
    public static void onBulletCreate(InternalBulletCreateEvent eventWrapper) {
        var event = eventWrapper.getImpl();
        var gun = event.getGunInfo();

        if (event.getBullet().level().isClientSide()) {
            return;
        }

        try {
            BUFFER.clear();
            int aecDuration = 0;
            float aecMinSize = 0;
            for (var extension : GunsmithLibSharedDataExtension.allOf(gun)) {
                BUFFER.addAll(extension.getPotionEffects());
                if (extension.getAreaEffectCloudDuration() > aecDuration) {
                    aecDuration = extension.getAreaEffectCloudDuration();
                }
                if (extension.getAreaEffectCloudMinSizeRate() > aecMinSize) {
                    aecMinSize = extension.getAreaEffectCloudMinSizeRate();
                }
            }
            if (!BUFFER.isEmpty() && event.getBullet() instanceof EnhancedKineticBullet bullet) {
                var finalEffectList = (List<PotionEffectData>) BUFFER.clone();
                bullet.gunsmithlib$setPotionEffects(Collections.unmodifiableList(finalEffectList));
                bullet.gunsmithlib$setPotionCloudDuration(aecDuration);
                bullet.gunsmithlib$setPotionCloudMinSizeRate(aecMinSize);
            }
        } finally {
            BUFFER.clear();
        }
    }

    @SubscribeEvent
    public static void onGunshotPost(EntityHurtByGunEvent.Post event) {
        if (event.getBullet().level().isClientSide()) {
            return;
        }
        if (!(event.getBullet() instanceof EnhancedKineticBullet bullet)) {
            return;
        }
        if (!(event.getHurtEntity() instanceof LivingEntity victim)) {
            return;
        }
        for (var effectData : bullet.gunsmithlib$getPotionEffects()) {
            effectData.applyTo(victim);
        }
    }

    @SubscribeEvent
    public static void onBulletLeaveLevel(EntityLeaveLevelEvent event) {
        Entity projectile = event.getEntity();
        if (projectile.level().isClientSide()) {
            return;
        }
        if (projectile.getRemovalReason() != Entity.RemovalReason.DISCARDED) {
            return;
        }
        if (!(projectile instanceof EnhancedKineticBullet bullet) || !bullet.isExplosion()) {
            return;
        }
        // 最终持续时间小于0时不生成药水云
        int duration = bullet.gunsmithlib$getPotionCloudDuration();
        if (duration <= 0) {
            return;
        }
        // 没有药水效果时不生成药水云
        var effects = bullet.gunsmithlib$getPotionEffects();
        if (effects == null || effects.isEmpty()) {
            return;
        }
        var hitPos = bullet.gunsmithlib$getHitPos();
        float radius = bullet.getExplosionRadius();
        var cloud = AreaEffectCloud3D.createAtCenter(
                projectile.level(),
                hitPos.x(), hitPos.y(), hitPos.z(),
                radius
        );
        float minRadiusRate = Mth.clamp(bullet.gunsmithlib$getPotionCloudMinSizeRate(), 0, 1);
        // 设置药水云的其他属性
        cloud.setDuration(duration);
        cloud.setWaitTime(0);
        cloud.setRadiusPerTick(-radius * (1 - minRadiusRate) / duration);
        effects.forEach(effect -> effect.applyTo(cloud));
        // 把药水云上市（加入世界）
        projectile.level().addFreshEntity(cloud);
    }
}
