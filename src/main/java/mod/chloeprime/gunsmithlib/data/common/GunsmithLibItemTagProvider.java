package mod.chloeprime.gunsmithlib.data.common;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

public class GunsmithLibItemTagProvider extends ItemTagsProvider {

    public GunsmithLibItemTagProvider(
            PackOutput packOutput,
            CompletableFuture<HolderLookup.Provider> registry,
            CompletableFuture<TagLookup<Block>> blockTagProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(packOutput, registry, blockTagProvider, GunsmithLib.MOD_ID, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider registry) {
        copy(Tags.Blocks.STORAGE_BLOCKS, Tags.Items.STORAGE_BLOCKS);
    }
}
