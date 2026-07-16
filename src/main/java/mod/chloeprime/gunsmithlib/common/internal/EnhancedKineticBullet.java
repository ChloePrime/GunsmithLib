package mod.chloeprime.gunsmithlib.common.internal;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public interface EnhancedKineticBullet extends TraceableEntity {
    boolean isExplosion();
    float getExplosionRadius();
    List<PotionEffectData> gunsmithlib$getPotionEffects();
    void gunsmithlib$setPotionEffects(List<PotionEffectData> value);
    int gunsmithlib$getPotionCloudDuration();
    void gunsmithlib$setPotionCloudDuration(int value);
    float gunsmithlib$getPotionCloudMinSizeRate();
    void gunsmithlib$setPotionCloudMinSizeRate(float value);

    /**
     * @return 弹药直至上一刻已经移动的距离
     * @since 6.2
     */
    double gunsmithlib$getMovedDistance();

    /**
     * 瞬移子弹时刷新子弹已飞过的距离
     *
     * @since 6.2
     */
    void gunsmith$onMovedToPos(Vec3 newPos);

    Vec3 gunsmithlib$getHitPos();
}
