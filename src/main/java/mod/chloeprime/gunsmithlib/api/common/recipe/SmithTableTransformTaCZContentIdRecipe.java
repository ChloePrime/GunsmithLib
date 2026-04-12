package mod.chloeprime.gunsmithlib.api.common.recipe;

import cn.chloeprime.commons.ContextUtil;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import mod.chloeprime.gunsmithlib.api.common.TaCZContentType;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.GsHelper1211;
import mod.chloeprime.gunsmithlib.mixin.SmithingTransformRecipeAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmithTableTransformTaCZContentIdRecipe extends SmithingTransformRecipe {
    private final TaCZContentType taczContentType;
    private final ResourceLocation resultId;

    public SmithTableTransformTaCZContentIdRecipe(
            Ingredient template,
            Ingredient base,
            Ingredient addition,
            TaCZContentType taczContentType,
            ResourceLocation resultId
    ) {
        super(template, base, addition, taczContentType.createWithId(resultId, ContextUtil.getRegistryAccess()));
        this.taczContentType = taczContentType;
        this.resultId = resultId;
    }

    public RecipeSerializer<?> getSerializer() {
        return GunRecipeSerializers.SMITHING_TRANSFORM_TACZ_CONTENT_ID.get();
    }

    /**
     * 对于转换枪械的配方来说，必须把子弹卸干净才能锻造。
     */
    @Override
    public boolean matches(SmithingRecipeInput container, Level level) {
        if (!super.matches(container, level)) {
            return false;
        }
        var base = Gunsmith.getGunInfo(container.getItem(1)).orElse(null);
        return base == null || isAmmoFitForTransform(base, level.registryAccess());
    }

    private boolean isAmmoFitForTransform(GunInfo src, RegistryAccess registryAccess) {
        // 待转换武器内没有子弹时，永远可以转换
        if (src.getTotalAmmo() == 0) {
            return true;
        }
        var dst = Gunsmith.getGunInfo(getResultItem(registryAccess)).orElse(null);
        if (dst == null) {
            return false;
        }
        var srcAmmoId = src.index().getGunData().getAmmoId();
        var dstAmmoId = dst.index().getGunData().getAmmoId();
        // 转换前后武器的子弹种类一致，
        // 且待转换武器内的子弹数量小于等于转换后的武器的总弹容量时，
        // 才可以转换
        return Objects.equals(srcAmmoId, dstAmmoId) && src.getTotalAmmo() <= dst.getTotalMagazineSize();
    }

    @Override
    public ItemStack assemble(SmithingRecipeInput container, HolderLookup.Provider registryAccess) {
        var src = container.getItem(1);
        var type = TaCZContentType.of(src).filter(this.taczContentType::equals).orElse(null);
        if (type == null) {
            return src;
        }
        var dst = src.copy();
        type.setId(dst, this.resultId);
        return dst;
    }

    public static class Serializer implements RecipeSerializer<SmithTableTransformTaCZContentIdRecipe> {
        public static final MapCodec<SmithTableTransformTaCZContentIdRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                Ingredient.CODEC.fieldOf("template").forGetter(recipe -> ((SmithingTransformRecipeAccessor) recipe).getTemplate()),
                Ingredient.CODEC_NONEMPTY.fieldOf("base").forGetter(recipe -> ((SmithingTransformRecipeAccessor) recipe).getBase()),
                Ingredient.CODEC.fieldOf("addition").forGetter(recipe -> ((SmithingTransformRecipeAccessor) recipe).getAddition()),
                GsHelper.enumCodec(TaCZContentType.class).fieldOf("tacz_content_type").forGetter(recipe -> recipe.taczContentType),
                ResourceLocation.CODEC.fieldOf("result_id").forGetter(recipe -> recipe.resultId)
        ).apply(instance, SmithTableTransformTaCZContentIdRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, SmithTableTransformTaCZContentIdRecipe> STREAM_CODEC = StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> ((SmithingTransformRecipeAccessor) recipe).getTemplate(),
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> ((SmithingTransformRecipeAccessor) recipe).getBase(),
                Ingredient.CONTENTS_STREAM_CODEC, recipe -> ((SmithingTransformRecipeAccessor) recipe).getAddition(),
                GsHelper1211.enumStreamCodec(TaCZContentType.class), recipe -> recipe.taczContentType,
                ResourceLocation.STREAM_CODEC, recipe -> recipe.resultId,
                SmithTableTransformTaCZContentIdRecipe::new);

        @Override
        public MapCodec<SmithTableTransformTaCZContentIdRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, SmithTableTransformTaCZContentIdRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}
