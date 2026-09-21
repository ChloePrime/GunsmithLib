package mod.chloeprime.gunsmithlib.data.client;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraftforge.client.model.generators.BlockStateProvider;
import net.minecraftforge.common.data.ExistingFileHelper;

public class GunsmithLibBlockStateProvider extends BlockStateProvider {
    public GunsmithLibBlockStateProvider(
            PackOutput output,
            ExistingFileHelper exFileHelper
    ) {
        super(output, GunsmithLib.MOD_ID, exFileHelper);
    }

    @Override
    @SuppressWarnings("deprecation")
    protected void registerStatesAndModels() {
        simpleBlockWithItem(GunsmithLib.Blocks.GUN_STEEL_BLOCK.get(), cubeAll(GunsmithLib.Blocks.GUN_STEEL_BLOCK.get()));
        directionalBlock(GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get(), models().cubeBottomTop(
                BuiltInRegistries.BLOCK.getKey(GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get()).toString(),
                GunsmithLib.loc("block/giga_ammo_block_side"),
                GunsmithLib.loc("block/giga_ammo_block_bottom"),
                GunsmithLib.loc("block/giga_ammo_block_top")
        ));
        simpleBlockItem(GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get(), models().getExistingFile(BuiltInRegistries.BLOCK.getKey(GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get())));
    }
}
