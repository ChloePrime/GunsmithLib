package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability;

import cn.chloeprime.commons.lang4.FloatSupplier;
import cn.chloeprime.commons.lang4.StringName;
import com.google.common.base.Suppliers;
import com.google.gson.JsonElement;
import com.mojang.authlib.GameProfile;
import com.tacz.guns.resource.pojo.data.attachment.AttachmentData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.api.common.GunsmithLibGunProperties;
import mod.chloeprime.gunsmithlib.api.util.AttachmentInfo;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment.GunsmithLibAttachmentDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.scripting.attachment.AttachmentScripting;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector4f;

import java.awt.*;
import java.util.*;
import java.util.function.Supplier;

/**
 * 耐久数据。
 * 和其他数据不同的是，这个数据被原版和各大模组系统视为耐久值。
 * 可被神能炉修复，可被莱特兰恶意瞬间蒸发，等等。
 *
 * @since 7.2
 */
public class DurabilityData {
    // 可配置属性
    /**
     * 耐久上限
     */
    @GunpackProperty
    private int max_durability = 1500;

    /**
     * 如果为 {@code true}，则最大耐久可以被 modify property。
     * @apiNote 因为计算配件最大耐久时无法获取枪械的上下文，而配件脚本需要枪械上下文，所以配件的最大耐久无法被 modify property。
     */
    @GunpackProperty
    private boolean max_durability_modifiable = false;

    /**
     * 耐久耗尽时是否销毁物品。如果为 {@code true} 则会销毁。默认为 {@code false}。
     * <p>
     * 优先级低于 {@link #transform_to_on_durability_exhausted}
     */
    @GunpackProperty
    private boolean breaks_on_durability_exhausted;

    /**
     * 耐久耗尽时转换为另一个 TaCZ 内容物品。
     * 枪只能转换成枪，配件只能转换成配件。
     * <p>
     * 优先级高于 {@link #breaks_on_durability_exhausted}
     */
    @GunpackProperty
    private @Nullable ResourceLocation transform_to_on_durability_exhausted;

    /**
     * 耐久最低时的耐久条颜色。
     * 两个颜色任意一个留空，则耐久条颜色将使用原版的默认颜色。
     */
    @GunpackProperty
    private int[] bar_color_at_min;

    /**
     * 耐久最高时的耐久条颜色。
     * 两个颜色任意一个留空，则耐久条颜色将使用原版的默认颜色。
     */
    @GunpackProperty
    private int[] bar_color_at_max;

    /**
     * 何时消耗武器的耐久。
     *
     * @see BuiltinDurabilityDamageCases
     */
    @GunpackProperty
    private Map<StringName, Integer> damage_cases = Map.of(BuiltinDurabilityDamageCases.FIRE, 1);

    /**
     * 修理材料。实际类型为 {@link Ingredient}。
     */
    @GunpackProperty
    private @Nullable JsonElement repair_material;

    // 脚本可修改（吗）属性

    public static final String PROP_NAME_GUN_MAX_DURABILITY = GunsmithLibGunProperties.GUN_MAX_DURABILITY;

    /**
     * @deprecated 因为计算配件最大耐久时无法获取枪械的上下文，而配件脚本需要枪械上下文，所以配件的最大耐久无法被 modify property。
     */
    @Deprecated
    public static final String PROP_NAME_ATTACH_MAX_DURABILITY = GunsmithLibGunProperties.ATTACHMENT_MAX_DURABILITY;

    // 下面是 Java 实现喵

    private static final GameProfile ACCOUNTANT = new GameProfile(UUID.fromString("48a1e171-ee65-44df-bdae-a91eb63c3ac0"), "[Durability Accountant]");
    private static final ThreadLocal<Vector4f> DUR_SYSTEM_BAR_COLOR_CACHE = ThreadLocal.withInitial(Vector4f::new);
    private final Supplier<@Nullable Vector4f> compiledMinBarColor = Suppliers.memoize(() -> GsHelper.parseDataColor(bar_color_at_min));
    private final Supplier<@Nullable Vector4f> compiledMaxBarColor = Suppliers.memoize(() -> GsHelper.parseDataColor(bar_color_at_max));
    private final Supplier<@Nullable Ingredient> compiledRepairMaterial = Suppliers.memoize(() -> Optional.ofNullable(repair_material)
            .map(itemJson -> CraftingHelper.getIngredient(itemJson, false))
            .orElse(null));

    public int getMaxDurability() {
        return max_durability;
    }

    public int getGunMaxDurability(GunInfo gun) {
        var base = getMaxDurability();
        if (isMaxDurabilityModifiable()) {
            return GsHelper.checkThreadIsServerThreadAndGetOverworld()
                    .map(overworld -> FakePlayerFactory.get(overworld, ACCOUNTANT))
                    .map(accountant -> GsHelper.modifyProperty(
                            gun, accountant,
                            PROP_NAME_GUN_MAX_DURABILITY, Integer.class, base))
                    .orElse(base);
        }
        return base;
    }

    /**
     * @deprecated 因为计算配件最大耐久时无法获取枪械的上下文，而配件脚本需要枪械上下文，所以配件的最大耐久无法被 modify property。
     * @since 7.2
     */
    @Deprecated
    public int getAttachmentMaxDurability(GunInfo gun, AttachmentInfo attachment) {
        var base = getMaxDurability();
        if (isMaxDurabilityModifiable()) {
            return GsHelper.checkThreadIsServerThreadAndGetOverworld()
                    .map(overworld -> FakePlayerFactory.get(overworld, ACCOUNTANT))
                    .map(accountant -> AttachmentScripting.modifyAttachmentProperty(
                            gun, attachment, accountant,
                            PROP_NAME_ATTACH_MAX_DURABILITY, Integer.class, base))
                    .orElse(base);
        }
        return base;
    }

    public boolean isMaxDurabilityModifiable() {
        return max_durability_modifiable;
    }

    public boolean breaksOnDurabilityExhausted() {
        return breaks_on_durability_exhausted;
    }

    public Optional<ResourceLocation> getTransformToOnDurabilityExhausted() {
        return Optional.ofNullable(transform_to_on_durability_exhausted);
    }

    public Optional<Vector4f> getMinBarColor() {
        return Optional.ofNullable(compiledMinBarColor.get());
    }

    public Optional<Vector4f> getMaxBarColor() {
        return Optional.ofNullable(compiledMaxBarColor.get());
    }

    // durabilityPercent 的计算过程可能涉及脚本调用，
    // 所以这里传入高阶函数以实现仅在需要时计算耐久。
    public Optional<Color> getBarColor(FloatSupplier durabilityPercent) {
        // 如果没配置则不启用。
        var min = getMinBarColor().orElse(null);
        var max = getMaxBarColor().orElse(null);
        if (max == null || min == null) {
            return Optional.empty();
        }
        // 计算插值。
        var color = min.lerp(max, durabilityPercent.getAsFloat(), DUR_SYSTEM_BAR_COLOR_CACHE.get());
        var r = (float) color.x;
        var g = (float) color.y;
        var b = (float) color.z;
        var a = (float) color.w;
        return Optional.of(new Color(r, g, b, a));
    }

    public Map<StringName, Integer> getDamageCases() {
        return Collections.unmodifiableMap(damage_cases);
    }

    public Optional<Ingredient> getRepairMaterial() {
        return Optional.ofNullable(compiledRepairMaterial.get());
    }

    public static Optional<DurabilityData> of(ItemStack stack) {
        return GunInfo.of(stack).flatMap(DurabilityData::of).or(() -> AttachmentInfo.of(stack).flatMap(DurabilityData::of));
    }

    public static Optional<DurabilityData> of(GunInfo gun) {
        return of(gun.index().getGunData());
    }

    public static Optional<DurabilityData> of(GunData gun) {
        return GunsmithLibGunDataExtension
                .of(gun)
                .map(GunsmithLibSharedDataExtension::getDurabilityData);
    }

    public static Optional<DurabilityData> of(AttachmentInfo attachment) {
        return of(attachment.index().getData());
    }

    public static Optional<DurabilityData> of(AttachmentData attachment) {
        return GunsmithLibAttachmentDataExtension
                .of(attachment)
                .map(GunsmithLibSharedDataExtension::getDurabilityData);
    }
}
