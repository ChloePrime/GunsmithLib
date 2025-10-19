package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunsmithLibAttributeModifierEntry;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * 枪械和配件共通的扩展功能
 * @since 4.2.0
 */
public class GunsmithLibSharedDataExtension {
    /**
     * 枪械/配件的属性修饰器
     */
    @GunpackProperty
    private @Nullable GunsmithLibAttributeModifierEntry[] attribute_modifiers;

    /**
     * 命中后对目标施加的药水效果
     *
     * @since 4.3.0
     */
    @GunpackProperty
    private @Nullable PotionEffectData[] potion_effects;

    /**
     * 带有药水效果的爆炸弹药爆炸后药水云的持续时间
     */
    @GunpackProperty
    private int area_effect_cloud_duration = 600;

    /**
     * 药水云的最小大小比例（结束时的大小/初始大小）
     */
    @GunpackProperty
    private float area_effect_cloud_min_size_rate = 0;

    // 下面是具体实现

    public List<GunsmithLibAttributeModifierEntry> getAttributeModifiers() {
        return List.of(MoreObjects.firstNonNull(attribute_modifiers, EMPTY_MODIFIER_POJO_ARRAY));
    }

    public List<PotionEffectData> getPotionEffects() {
        return List.of(MoreObjects.firstNonNull(potion_effects,  EMPTY_MOB_EFFECT_ARRAY));
    }

    public int getAreaEffectCloudDuration() {
        return area_effect_cloud_duration;
    }

    public float getAreaEffectCloudMinSizeRate() {
        return area_effect_cloud_min_size_rate;
    }

    private static final GunsmithLibAttributeModifierEntry[] EMPTY_MODIFIER_POJO_ARRAY = new GunsmithLibAttributeModifierEntry[0];
    private static final AttachmentType[] ATTACH_TYPE_REGISTRY = AttachmentType.values();
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

    public static Iterable<GunsmithLibSharedDataExtension> allOf(GunInfo gun) {
        // From Gun
        Iterable<GunsmithLibSharedDataExtension> fromGun = ((EnhancedGunData) gun.index().getGunData()).gunsmith$getGunsmithLibExtension()
                .map(List::<GunsmithLibSharedDataExtension>of)
                .orElse(List.of());
        // From Attachment
        Iterable<GunsmithLibSharedDataExtension> fromAttachment = Iterables.transform(Arrays.asList(ATTACH_TYPE_REGISTRY), attachmentType -> TimelessAPI
                .getCommonAttachmentIndex(gun.gunItem().getAttachmentId(gun.gunStack(), attachmentType))
                .map(CommonAttachmentIndex::getData)
                .flatMap(data -> ((EnhancedAttachmentData) data).gunsmith$getGunsmithLibExtension())
                .orElse(null));
        return Iterables.filter(Iterables.concat(fromGun, fromAttachment), Objects::nonNull);
    }
}
