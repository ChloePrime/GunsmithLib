package mod.chloeprime.gunsmithlib.compat.jei;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.GunAmmoVariantSet;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.GunVariantRegistry;
import mod.chloeprime.gunsmithlib.compat.jei.client.GunAmmoVariantSystemQueryCategory;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nonnull;
import java.util.ArrayList;

@JeiPlugin
public class GunAmmoVariantSystemJeiPlugin implements IModPlugin {
    public static final ResourceLocation ID = GunsmithLib.loc("gun_ammo_variant_system");
    public static final RecipeType<GunAmmoVariantSet> QUERY_RECIPE_TYPE = RecipeType.create(GunsmithLib.MOD_ID, "gun_ammo_variant_query", GunAmmoVariantSet.class);

    @Override
    public @Nonnull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerCategories(@Nonnull IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new GunAmmoVariantSystemQueryCategory(QUERY_RECIPE_TYPE, registration.getJeiHelpers()));
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        registration.addRecipes(QUERY_RECIPE_TYPE, new ArrayList<>(GunVariantRegistry.all().values()));
    }
}
