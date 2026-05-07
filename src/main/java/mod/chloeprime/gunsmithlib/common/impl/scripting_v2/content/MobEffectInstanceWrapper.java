package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.PotionEffectInstanceView;
import net.minecraft.world.effect.MobEffectInstance;

/**
 * @since 6.0.0
 */
public record MobEffectInstanceWrapper(MobEffectInstance instance) implements PotionEffectInstanceView {
    @Override
    public int duration_ticks() {
        return instance.getDuration();
    }

    @Override
    public int amplifier() {
        return instance.getAmplifier();
    }

    @Override
    public boolean is_ambient() {
        return instance.isAmbient();
    }

    @Override
    public boolean is_visible() {
        return instance.isVisible();
    }

    @Override
    public boolean shows_icon() {
        return instance.showIcon();
    }
}
