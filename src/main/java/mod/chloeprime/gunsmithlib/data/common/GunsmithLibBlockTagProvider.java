package mod.chloeprime.gunsmithlib.data.common;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.BlockTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class GunsmithLibBlockTagProvider extends BlockTagsProvider {
    public GunsmithLibBlockTagProvider(
            PackOutput output,
            CompletableFuture<HolderLookup.Provider> lookupProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(output, lookupProvider, GunsmithLib.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider registry) {
        for (var tag : List.of(
                Tags.Blocks.STORAGE_BLOCKS,
                BlockTags.MINEABLE_WITH_PICKAXE,
                BlockTags.NEEDS_STONE_TOOL
        )) {
            tag(tag).add(GunsmithLib.Blocks.GUN_STEEL_BLOCK.get(), GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get());
        }
    }
}
