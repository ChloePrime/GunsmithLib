package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Iterables;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.attachment.AttachmentType;
import com.tacz.guns.resource.index.CommonAttachmentIndex;
import mod.chloeprime.gunsmithlib.api.util.AmmoInfo;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo.GunsmithLibAmmoDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.EnhancedAttachmentData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.GunsmithLibAttachmentDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.attribute.GunsmithLibAttributeModifierEntry;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.damage_source_control.DamageSourceControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.raytrace_control.RaytraceControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.ricochet.RicochetData;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.Function;

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

    /**
     * 命中时的粒子效果，
     * 可以添加在枪械和子弹的 data 里，添加在配件上无效。
     *
     * @since 5.2.0
     */
    @GunpackProperty
    private @Nullable HitParticleData[] hit_particles;

    /**
     * 跳弹设置。
     * 玩补包榴弹玩的
     *
     * @since 5.2.0
     */
    @GunpackProperty
    private @Nullable RicochetData ricochet;

    @GunpackProperty
    private @Nullable RaytraceControlData raytrace_control;

    @GunpackProperty
    private @Nullable DamageSourceControlData damage_source_control;

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

    public @Nullable HitParticleData[] getHitParticles() {
        return hit_particles;
    }

    public @Nullable RicochetData getRicochetData() {
        return ricochet;
    }

    public @Nullable RaytraceControlData getRaytraceControlData() {
        return raytrace_control;
    }
    public @Nullable DamageSourceControlData getDamageSourceControlData() {
        return damage_source_control;
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

    public static Optional<GunsmithLibSharedDataExtension> forGun(GunInfo gun) {
        return GunsmithLibGunDataExtension.of(gun).map(Function.identity());
    }

    public static Optional<GunsmithLibSharedDataExtension> forAmmo(AmmoInfo ammo) {
        return GunsmithLibAmmoDataExtension.of(ammo).map(Function.identity());
    }

    public static Optional<GunsmithLibSharedDataExtension> forAttachment(AttachmentInfo attachment) {
        return GunsmithLibAttachmentDataExtension.of(attachment).map(Function.identity());
    }

    public static <T> Optional<T> forGunOrAmmo(
            ItemStack gun,
            Function<GunsmithLibSharedDataExtension, T> field
    ) {
        var gunInfo = Gunsmith.getGunInfo(gun).orElse(null);
        if (gunInfo == null) {
            return Optional.empty();
        }
        return forGunOrAmmo(gunInfo, field);
    }

    @SuppressWarnings("OptionalIsPresent")
    public static <T> Optional<T> forGunOrAmmo(
            GunInfo gunInfo,
            Function<GunsmithLibSharedDataExtension, T> field
    ) {
        var onGun = GunsmithLibSharedDataExtension
                .forGun(gunInfo)
                .map(field);
        if (onGun.isPresent()) {
            return onGun;
        }
        var onAmmo = Gunsmith
                .getAmmoInfo(Gunsmith.createAmmoItemFromId(gunInfo.index().getGunData().getAmmoId()))
                .flatMap(GunsmithLibSharedDataExtension::forAmmo)
                .map(field);
        if (onAmmo.isPresent()) {
            return onAmmo;
        }
        return Optional.empty();
    }

    public static <T> List<T> forGunOrAmmoWithAttachment(
            ItemStack gun,
            Function<GunsmithLibSharedDataExtension, T> field
    ) {
        var gunInfo = Gunsmith.getGunInfo(gun).orElse(null);
        if (gunInfo == null) {
            return Collections.emptyList();
        }
        return forGunOrAmmoWithAttachment(gunInfo, field);
    }

    public static <T> List<T> forGunOrAmmoWithAttachment(
            GunInfo gunInfo,
            Function<GunsmithLibSharedDataExtension, T> field
    ) {
        var fromBase = forGunOrAmmo(gunInfo, field).orElse(null);
        var fromAttach = (List<T>) null;
        var iGun = gunInfo.gunItem();
        var stack = gunInfo.gunStack();
        for (var type : AttachmentType.values()) {
            T onAttach = Gunsmith.getAttachmentInfo(iGun.getAttachment(stack, type))
                    .flatMap(GunsmithLibSharedDataExtension::forAttachment)
                    .map(field)
                    .orElse(null);
            if (onAttach != null) {
                if (fromAttach == null) {
                    fromAttach = new ArrayList<>(6);
                }
                fromAttach.add(onAttach);
            }
        }
        if (fromBase == null && fromAttach == null) {
            return Collections.emptyList();
        }
        if (fromBase == null) {
            return fromAttach;
        }
        if (fromAttach == null) {
            return List.of(fromBase);
        }
        var total = new ArrayList<T>(fromAttach.size() + 1);
        total.add(fromBase);
        total.addAll(fromAttach);
        return Collections.unmodifiableList(total);
    }

    /**
     * 返回枪械和配件上所有的扩展 data，不包括子弹扩展 data。
     *
     * @param gun 枪械对象。
     * @return 枪械和配件上所有的扩展 data，不包括子弹！
     */
    public static Iterable<GunsmithLibSharedDataExtension> allOf(GunInfo gun) {
        // From Gun
        Iterable<GunsmithLibSharedDataExtension> fromGun = forGun(gun)
                .map(List::of)
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
