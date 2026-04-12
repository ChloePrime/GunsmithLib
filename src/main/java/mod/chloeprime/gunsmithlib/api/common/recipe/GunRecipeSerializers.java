package mod.chloeprime.gunsmithlib.api.common.recipe;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.jetbrains.annotations.ApiStatus;

import java.util.function.Supplier;

public class GunRecipeSerializers {
    @ApiStatus.Internal
    private static final DeferredRegister<RecipeSerializer<?>> DFR = DeferredRegister.create(Registries.RECIPE_SERIALIZER, GunsmithLib.MOD_ID);

    /**
     * 锻造台改变内容 id 的配方。
     * 示例配方：{@code run/tacz/debug/data/gunsmithlib/recipes/smith_example.json}
     */
    public static final Supplier<RecipeSerializer<SmithTableTransformTaCZContentIdRecipe>> SMITHING_TRANSFORM_TACZ_CONTENT_ID = DFR
            .register("smithing_transform_tacz_content_id", SmithTableTransformTaCZContentIdRecipe.Serializer::new);

    @ApiStatus.Internal
    public static void init(IEventBus bus) {
        DFR.register(bus);
    }
}
