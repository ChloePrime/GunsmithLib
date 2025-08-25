package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@EventBusSubscriber
public class PotionEffectBehavior {
    private static final AttachmentType[] ATTACH_TYPE_REGISTRY = AttachmentType.values();
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
            ((EnhancedGunData) gun.index().getGunData()).gunsmith$getGunsmithLibExtension()
                    .map(GunsmithLibSharedDataExtension::getPotionEffects)
                    .map(BUFFER::addAll);
            for (var attachmentType : ATTACH_TYPE_REGISTRY) {
                TimelessAPI.getCommonAttachmentIndex(gun.gunItem().getAttachmentId(gun.gunStack(), attachmentType))
                        .map(CommonAttachmentIndex::getData)
                        .flatMap(data -> ((EnhancedAttachmentData)data).gunsmith$getGunsmithLibExtension())
                        .map(GunsmithLibSharedDataExtension::getPotionEffects)
                        .ifPresent(BUFFER::addAll);
            }
            if (!BUFFER.isEmpty() && event.getBullet() instanceof EnhancedKineticBullet bullet) {
                var finalEffectList = (List<PotionEffectData>) BUFFER.clone();
                bullet.gunsmithlib$setPotionEffects(Collections.unmodifiableList(finalEffectList));
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
}
