package mod.chloeprime.gunsmithlib.common.internal;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;

import java.util.List;

public interface EnhancedKineticBullet {
    boolean isExplosion();
    float getExplosionRadius();
    List<PotionEffectData> gunsmithlib$getPotionEffects();
    void gunsmithlib$setPotionEffects(List<PotionEffectData> value);
    int gunsmithlib$getPotionCloudDuration();
    void gunsmithlib$setPotionCloudDuration(int value);
    float gunsmithlib$getPotionCloudMinSizeRate();
    void gunsmithlib$setPotionCloudMinSizeRate(float value);
}
