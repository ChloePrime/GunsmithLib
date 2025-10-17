package mod.chloeprime.gunsmithlib.common.util;

import cn.chloeprime.commons.lang4.FloatSupplier;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.mixin.ItemCooldownsAccessor;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import org.apache.maven.artifact.versioning.ArtifactVersion;
import org.apache.maven.artifact.versioning.DefaultArtifactVersion;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.DoubleSupplier;

public class GsHelper {
    public static float infDist(FloatSupplier nextGaussianFunc, float mean, float dev) {
        return infLerp(nextGaussianFunc.getAsFloat(), mean - dev, mean + dev);
    }

    public static double infDist(DoubleSupplier nextGaussianFunc, double mean, double dev) {
        return infLerp(nextGaussianFunc.getAsDouble(), mean - dev, mean + dev);
    }

    public static float infLerp(float delta, float start, float end) {
        var normalizedDelta = (float) Math.atan(delta) / Mth.PI + 0.5F;
        return Mth.lerp(normalizedDelta, start, end);
    }

    public static double infLerp(double delta, double start, double end) {
        var normalizedDelta = Math.atan(delta) / Math.PI + 0.5;
        return Mth.lerp(normalizedDelta, start, end);
    }

    /**
     * @return 增益倍率，永远不会为 0
     * @since 3.2.0
     */
    public static double getBuffCoefficient(ResourceLocation gunId, boolean isMelee) {
        if (!isMelee) {
            return 1;
        }
        return TimelessAPI.getCommonGunIndex(gunId)
                .map(index -> index.getGunData().getBulletData().getBulletAmount())
                .map(shrapnel -> shrapnel == 0 ? 1 : 1.0 / Math.abs(shrapnel))
                .orElse(1.0);
    }

    public static Optional<GunInfo> unpack(IGun gunItem, ItemStack gunStack) {
        var gunId = gunItem.getGunId(gunStack);
        return TimelessAPI.getCommonGunIndex(gunId).map(index -> new GunInfo(gunStack, gunItem, gunId, index));
    }

    public static double getAttributeValueWithBase(LivingEntity holder, Holder<Attribute> attribute, double base) {
        var instance = holder.getAttribute(attribute);
        if (instance == null) {
            return base;
        }

        var oldBase = instance.getBaseValue();
        try {
            instance.setBaseValue(base);
            return instance.getValue();
        } finally {
            instance.setBaseValue(oldBase);
        }
    }

    public static int[] getModVersion() {
        if (version != null) {
            return version;
        }
        version = ModList.get().getModContainerById(GunsmithLib.MOD_ID)
                .map(container -> extractModVersion(container.getModInfo().getVersion()))
                .map(version -> new int[]{version.getMajorVersion(), version.getMinorVersion(), version.getIncrementalVersion()})
                .orElse(new int[]{0, 0, 0});
        return version;
    }

    private static ArtifactVersion extractModVersion(ArtifactVersion fullVersion) {
        if (fullVersion.getMajorVersion() > 0) {
            return fullVersion;
        }
        return Arrays.stream(fullVersion.getQualifier().split("\\+")).findFirst()
                .<ArtifactVersion>map(DefaultArtifactVersion::new)
                .orElse(fullVersion);
    }

    public static Map<String, Object> parseStaticFields(Class<?> clazz) {
        Map<String, Object> constantMap = new LinkedHashMap<>();
        // 获取 GunAnimationConstant 的所有 public 字段
        Field[] fields = clazz.getFields();
        // 将 static final 的常量字段提取到 constantMap
        for (Field field : fields) {
            if (Modifier.isStatic(field.getModifiers()) && Modifier.isFinal(field.getModifiers())) {
                try {
                    // 获取变量名和值
                    String name = field.getName();
                    Object value = field.get(null);
                    constantMap.put(name, value);
                } catch (IllegalAccessException ex) {
                    GunsmithLib.LOGGER.warn("Failed to parse static fields for class {}", clazz.getCanonicalName(), ex);
                }
            }
        }
        return constantMap;
    }

    private static int[] version = null;

    private GsHelper() {
    }

    public static double getEstimatedMaxRange(@Nullable Entity entity, @Nonnull ItemStack gunStack) {
        IGun gunItem = IGun.getIGunOrNull(gunStack);
        if (gunItem == null) {
            return 0;
        }
        return getEstimatedMaxRange(entity, gunStack, gunItem);
    }

    @SuppressWarnings("UnstableApiUsage")
    public static double getEstimatedMaxRange(@Nullable Entity entity, @Nonnull ItemStack gunStack, @Nonnull IGun gunItem) {
        if (!(entity instanceof LivingEntity shooter)) {
            return 0;
        }
        var gi = unpack(gunItem, gunStack).orElse(null);
        if (gi == null) {
            return 0;
        }
        return Optional.ofNullable(IGunOperator.fromLivingEntity(shooter).getCacheProperty())
                .map(cache -> {
                    float speed = cache.getCache(GunProperties.AMMO_SPEED);
                    float life = gi.index().getBulletData().getLifeSecond();
                    return speed * life;
                })
                .orElse(0F);
    }

    public static float getCooldownDuration(ItemCooldowns cooldowns, Item item) {
        var instance = ((ItemCooldownsAccessor) cooldowns).getCooldowns().get(item);
        if (instance == null) {
            return 0;
        }
        return instance.endTime - instance.startTime;
    }

    public record AttributeEvaluatorBuffer(
            List<AttributeModifier> addition,
            List<AttributeModifier> mulBase,
            List<AttributeModifier> mulTotal
    ) {
        public AttributeEvaluatorBuffer() {
            this(new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
        }
    }

    private static final ThreadLocal<AttributeEvaluatorBuffer> BUFFER_BY_THREAD = ThreadLocal.withInitial(AttributeEvaluatorBuffer::new);

    public static double evaluateItemAttribute(
            ItemStack item, Holder<Attribute> attributeHolder, double baseValue
    ) {
        var buffer = BUFFER_BY_THREAD.get();

        try {
            item.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
                if (!attributeHolder.equals(attribute)) {
                    return;
                }
                switch (modifier.operation()) {
                    case ADD_VALUE -> buffer.addition().add(modifier);
                    case ADD_MULTIPLIED_BASE -> buffer.mulBase().add(modifier);
                    case ADD_MULTIPLIED_TOTAL -> buffer.mulTotal().add(modifier);
                }
            });

            double afterAddition = baseValue;
            for(var modifier : buffer.addition()) {
                afterAddition += modifier.amount();
            }

            double finalValue = afterAddition;
            for(var modifier : buffer.mulBase()) {
                finalValue += afterAddition * modifier.amount();
            }

            for(var modifier : buffer.mulTotal()) {
                finalValue *= 1.0D + modifier.amount();
            }

            return attributeHolder.value().sanitizeValue(finalValue);
        } finally {
            buffer.addition().clear();
            buffer.mulBase().clear();
            buffer.mulTotal().clear();
        }
    }
}
