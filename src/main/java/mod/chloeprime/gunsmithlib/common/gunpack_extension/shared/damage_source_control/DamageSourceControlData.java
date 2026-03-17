package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.damage_source_control;

import cn.chloeprime.commons.ContextUtil;
import com.google.common.base.Suppliers;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import mod.chloeprime.gunsmithlib.common.util.TagKeyOr;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * @since 5.8.0
 */
public class DamageSourceControlData {
    /**
     * 修改基础伤害类型
     */
    @GunpackProperty
    private @Nullable String master_type;

    /**
     * 修改穿甲段的基础伤害类型。
     * <p>
     * 警告：穿甲段伤害在 GunsmithLib 默认配置下被禁用，
     * 在没有重新启用的情况下设置这个字段没有任何效果。
     */
    @GunpackProperty
    @ApiStatus.Obsolete
    private @Nullable String master_type_ap;

    /**
     * 让伤害来源判定 is 某个 tag 为 true。
     * <p>
     * 必须是以 # 开头的伤害类型标签，不能是某个具体的伤害类型。
     * <p>
     * 非传递性，设置某个 tag 判定为 true 不会让引用这个 tag 的其他 tag 被判定为 true。
     */
    @GunpackProperty
    private String[] inject_is = EMPTY_STRING_ARRAY;

    /**
     * 让伤害来源判定 is 某个 tag 为 false，覆盖 {@link #inject_is}。
     * <p>
     * 必须是以 # 开头的伤害类型标签，不能是某个具体的伤害类型。
     * <p>
     * 非传递性，设置某个 tag 判定为 false 不会让引用这个 tag 的其他 tag 被判定为 false。
     */
    @GunpackProperty
    private String[] inject_is_not = EMPTY_STRING_ARRAY;

    // 下面是实现 :P

    public Optional<Holder<DamageType>> getMasterType() {
        return Optional.ofNullable(masterType.get());
    }

    public Optional<Holder<DamageType>> getMasterApType() {
        return Optional.ofNullable(masterTypeAp.get());
    }

    public final List<TagKeyOr<DamageType>> getIsList() {
        return is.get();
    }

    public final List<TagKeyOr<DamageType>> getIsNotList() {
        return isNot.get();
    }

    private static final String[] EMPTY_STRING_ARRAY = new String[0];

    private transient final Supplier<Holder<DamageType>> masterType = Suppliers.memoize(() -> getDamageType(master_type));
    private transient final Supplier<Holder<DamageType>> masterTypeAp = Suppliers.memoize(() -> getDamageType(master_type_ap));
    private transient final Supplier<List<TagKeyOr<DamageType>>> is = TagKeyOr.compile(Registries.DAMAGE_TYPE, () -> inject_is);
    private transient final Supplier<List<TagKeyOr<DamageType>>> isNot = TagKeyOr.compile(Registries.DAMAGE_TYPE, () -> inject_is_not);

    public static List<DamageSourceControlData> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun)
                .map(DamageSourceControlData::of)
                .orElse(Collections.emptyList());
    }

    public static List<DamageSourceControlData> of(GunInfo gun) {
        return GunsmithLibSharedDataExtension.forGunOrAmmoWithAttachment(gun, GunsmithLibSharedDataExtension::getDamageSourceControlData);
    }

    private static @Nullable Holder<DamageType> getDamageType(@Nullable String id) {
        var loc = Optional.ofNullable(id)
                .map(ResourceLocation::tryParse)
                .orElse(null);
        if (loc == null) {
            return null;
        }
        var regKey = Registries.DAMAGE_TYPE;
        return ContextUtil.getRegistryAccess()
                .registry(regKey)
                .flatMap(reg -> reg.getHolder(ResourceKey.create(regKey, loc)))
                .orElse(null);
    }
}
