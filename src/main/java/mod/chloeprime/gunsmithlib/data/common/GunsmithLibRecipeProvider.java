package mod.chloeprime.gunsmithlib.data.common;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.*;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.Tags;

import javax.annotation.Nonnull;
import java.util.function.Consumer;

import static mod.chloeprime.gunsmithlib.GunsmithLib.Blocks.*;

public class GunsmithLibRecipeProvider extends RecipeProvider {
    public GunsmithLibRecipeProvider(PackOutput output) {
        super(output);
    }

    @Override
    protected void buildRecipes(@Nonnull Consumer<FinishedRecipe> output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GUN_STEEL_BLOCK.get(), 24)
                .define('#', Tags.Items.STORAGE_BLOCKS_IRON)
                .define('X', Items.COAL)
                .pattern("###")
                .pattern("#X#")
                .pattern("###")
                .unlockedBy("has_iron_block", has(Items.IRON_BLOCK))
                .save(output, GunsmithLib.loc("gun_steel_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.IRON_INGOT, 3)
                .requires(GUN_STEEL_BLOCK.get())
                .unlockedBy("has_gun_steel_block", has(GUN_STEEL_BLOCK.get()))
                .save(output, GunsmithLib.loc("minecraft_iron_ingot_from_gun_steel_block"));

        ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, GIGA_AMMO_BLOCK.get(), 24)
                .define('#', Tags.Items.STORAGE_BLOCKS_COPPER)
                .define('X', Tags.Items.GUNPOWDER)
                .pattern("###")
                .pattern("#X#")
                .pattern("###")
                .unlockedBy("has_gunpowder", has(Items.GUNPOWDER))
                .save(output, GunsmithLib.loc("giga_ammo_block"));
        ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, Items.COPPER_INGOT, 3)
                .requires(GIGA_AMMO_BLOCK.get())
                .unlockedBy("has_giga_ammo_block", has(GIGA_AMMO_BLOCK.get()))
                .save(output, GunsmithLib.loc("minecraft_copper_ingot_from_giga_ammo_block"));
    }
}
