package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared;

import com.google.common.base.MoreObjects;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunsmithLibAttributeModifierEntry;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 枪械和配件共通的扩展功能
 * @since 4.2.0
 */
public class GunsmithLibSharedDataExtension {
    /**
     * 枪械/配件的属性修饰器
     */
    @SuppressWarnings("unused")
    private @Nullable GunsmithLibAttributeModifierEntry[] attribute_modifiers;

    /**
     * 命中后对目标施加的药水效果
     *
     * @since 4.3.0
     */
    @SuppressWarnings("unused")
    private @Nullable PotionEffectData[] potion_effects;

    // 下面是具体实现

    public List<GunsmithLibAttributeModifierEntry> getAttributeModifiers() {
        return List.of(MoreObjects.firstNonNull(attribute_modifiers, EMPTY_MODIFIER_POJO_ARRAY));
    }

    public List<PotionEffectData> getPotionEffects() {
        return List.of(MoreObjects.firstNonNull(potion_effects,  EMPTY_MOB_EFFECT_ARRAY));
    }

    private static final GunsmithLibAttributeModifierEntry[] EMPTY_MODIFIER_POJO_ARRAY = new GunsmithLibAttributeModifierEntry[0];
    private static final PotionEffectData[] EMPTY_MOB_EFFECT_ARRAY = new PotionEffectData[0];
    private transient ItemAttributeModifiers bakedAttributeModifiers;

    @NotNull
    public ItemAttributeModifiers getBakedAttributeModifiers() {
        if (bakedAttributeModifiers == null) {
            var builder = ItemAttributeModifiers.builder();
            for (GunsmithLibAttributeModifierEntry pojo : getAttributeModifiers()) {
                pojo.getModifier().ifPresent(pair -> builder.add(pair.getLeft(), pair.getRight(), EquipmentSlotGroup.MAINHAND));
            }
            bakedAttributeModifiers = builder.build();
        }
        return bakedAttributeModifiers;
    }
}
