package mod.chloeprime.gunsmithlib.api.common.recipe;

import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import mod.chloeprime.gunsmithlib.api.common.TaCZContentType;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.mixin.common.minecraft.SmithingTransformRecipeAccessor;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SmithTableTransformTaCZContentIdRecipe extends SmithingTransformRecipe {
    private final TaCZContentType taczContentType;
    private final ResourceLocation resultId;

    public SmithTableTransformTaCZContentIdRecipe(
            ResourceLocation id,
            Ingredient template,
            Ingredient base,
            Ingredient addition,
            TaCZContentType taczContentType,
            ResourceLocation resultId
    ) {
        super(id, template, base, addition, taczContentType.createWithId(resultId));
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
    public boolean matches(Container container, Level level) {
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
    public ItemStack assemble(Container container, RegistryAccess registryAccess) {
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
        public SmithTableTransformTaCZContentIdRecipe fromJson(ResourceLocation id, JsonObject data) {
            var template = Ingredient.fromJson(GsonHelper.getNonNull(data, "template"));
            var base = Ingredient.fromJson(GsonHelper.getNonNull(data, "base"));
            var addition = Ingredient.fromJson(GsonHelper.getNonNull(data, "addition"));
            var type = TaCZContentType.valueOf(GsonHelper.getAsString(data, "tacz_content_type").toUpperCase(Locale.ROOT));
            var resultIdStr = GsonHelper.getAsString(data, "result_id");
            var resultId = Optional
                    .ofNullable(ResourceLocation.tryParse(resultIdStr))
                    .orElseThrow(() -> new JsonParseException("Invalid resource location: %s".formatted(resultIdStr)));
            return new SmithTableTransformTaCZContentIdRecipe(id, template, base, addition, type, resultId);
        }

        public SmithTableTransformTaCZContentIdRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            var template = Ingredient.fromNetwork(buf);
            var base = Ingredient.fromNetwork(buf);
            var addition = Ingredient.fromNetwork(buf);
            var type = TaCZContentType.valueOf(buf.readUtf());
            var resultId = buf.readResourceLocation();
            return new SmithTableTransformTaCZContentIdRecipe(id, template, base, addition, type, resultId);
        }

        public void toNetwork(FriendlyByteBuf buf, SmithTableTransformTaCZContentIdRecipe instance) {
            var accessor = (SmithingTransformRecipeAccessor) instance;
            accessor.getTemplate().toNetwork(buf);
            accessor.getBase().toNetwork(buf);
            accessor.getAddition().toNetwork(buf);
            buf.writeUtf(instance.taczContentType.name());
            buf.writeResourceLocation(instance.resultId);
        }
    }
}
