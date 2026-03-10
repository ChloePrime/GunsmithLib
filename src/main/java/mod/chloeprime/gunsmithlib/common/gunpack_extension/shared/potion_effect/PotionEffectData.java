package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect;

import com.google.common.base.Suppliers;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import mod.chloeprime.gunsmithlib.common.entity.AreaEffectCloud3D;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 子弹命中施加药水效果
 * @since 4.3.0
 */
public class PotionEffectData {
    /**
     * 药水效果的 id。<p>
     * 原版的效果 id 列表请见 <a href="https://zh.minecraft.wiki/w/%E7%8A%B6%E6%80%81%E6%95%88%E6%9E%9C">Minecraft Wiki 上的状态效果页面</a>
     */
    @GunpackProperty
    private ResourceLocation effect_id;

    /**
     * 药水效果的持续时间，
     * 单位为刻（tick）
     */
    @GunpackProperty
    private int duration = 20;

    /**
     * 药水效果的等级。<p>
     * 1 = 1级，2 = 2级。<p>
     * 同样等级需要填的参数比 /effect 命令里的 amplifier 参数大 1 点。
     */
    @GunpackProperty
    private int level = 1;

    /**
     * 如果为 true，那么药水的粒子效果会变淡
     */
    @GunpackProperty
    private boolean is_ambient = false;

    /**
     * 如果为 false，那么药水的粒子效果会隐藏
     */
    @GunpackProperty
    private boolean visible = true;

    /**
     * 如果为 true，那么该药水效果会在客户端里显示出来
     */
    @GunpackProperty
    private boolean show_icon = true;

    /**
     * 每次命中施加效果的概率
     */
    @GunpackProperty
    private float chance = 1;

    /**
     * 最大叠加的药水等级。
     * 如果 <= 0 则不叠加
     */
    @GunpackProperty
    private int max_stack_level;

    /**
     * 药水云的药水等级
     *
     * @since 5.3.0
     */
    @GunpackProperty
    private Integer area_cloud_level;

    /**
     * 药水云的生成概率
     *
     * @since 5.3.0
     */
    @GunpackProperty
    private Float area_cloud_chance;

    // 下面是代码
    private transient final Supplier<Optional<Holder<MobEffect>>> effect = Suppliers.memoize(() -> BuiltInRegistries.MOB_EFFECT
            .getHolder(effect_id)
            .map(Function.identity()));

    public final Optional<Holder<MobEffect>> getEffect() {
        return effect.get();
    }

    public final int getDuration() {
        return duration;
    }

    public final int getLevel() {
        return level;
    }

    public final boolean isAmbient() {
        return is_ambient;
    }

    public final boolean isVisible() {
        return visible;
    }

    public final boolean willShowIcon() {
        return show_icon;
    }

    public final float getChance() {
        return chance;
    }

    public final int getMaxStackLevel() {
        return max_stack_level;
    }

    public final int getAreaCloudLevel() {
        return Objects.requireNonNullElse(area_cloud_level, level);
    }

    public final float getAreaCloudChance() {
        return Objects.requireNonNullElse(area_cloud_chance, chance);
    }

    public void applyTo(LivingEntity target) {
        applyTo(target.getRandom(), target::getEffect, target::addEffect, false);
    }

    public boolean applyTo(AreaEffectCloud3D cloud) {
        return applyTo(cloud.getRandom(), _effect -> null, cloud::addEffect, true);
    }

    public boolean applyTo(
            RandomSource random,
            Function<Holder<MobEffect>, @Nullable MobEffectInstance> current,
            Consumer<MobEffectInstance> target,
            boolean isCloud
    ) {
        var applyChance = isCloud ? getAreaCloudChance() : getChance();
        if (applyChance <= 0) {
            return false;
        }
        var effect = getEffect().orElse(null);
        if (effect == null) {
            return false;
        }
        if (applyChance < 1 && random.nextFloat() > applyChance) {
            return false;
        }
        int appLevel = isCloud ? getAreaCloudLevel() : getLevel();
        int newLevel;
        if (getMaxStackLevel() > 0) {
            var existLevel = Optional.ofNullable(current.apply(effect)).map(MobEffectInstance::getAmplifier).orElse(-1) + 1;
            newLevel = Mth.clamp(existLevel + appLevel, 1, getMaxStackLevel());
        } else {
            newLevel = appLevel;
        }
        if (newLevel > 0) {
            target.accept(createInstance(effect, newLevel));
            return true;
        } else {
            return false;
        }
    }

    private MobEffectInstance createInstance(Holder<MobEffect> effect, int level) {
        return new MobEffectInstance(effect, getDuration(), Math.max(0, level - 1), isAmbient(), isVisible(), willShowIcon());
    }
}
