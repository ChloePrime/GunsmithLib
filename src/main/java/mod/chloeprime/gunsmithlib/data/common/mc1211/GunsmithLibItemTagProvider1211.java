package mod.chloeprime.gunsmithlib.data.common.mc1211;

import com.tacz.guns.init.ModItems;
import mod.chloeprime.gunsmithlib.data.common.GunsmithLibItemTagProvider;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.concurrent.CompletableFuture;

/**
 * 附 魔 标 签
 *
 * @since 7.2.0
 */
public class GunsmithLibItemTagProvider1211 extends GunsmithLibItemTagProvider {

    public GunsmithLibItemTagProvider1211(
            PackOutput packOutput,
            CompletableFuture<HolderLookup.Provider> registry,
            CompletableFuture<TagLookup<Block>> blockTagProvider,
            @Nullable ExistingFileHelper existingFileHelper
    ) {
        super(packOutput, registry, blockTagProvider, existingFileHelper);
    }

    @Override
    protected void addTags(@Nonnull HolderLookup.Provider provider) {
        super.addTags(provider);
        tag(ItemTags.DURABILITY_ENCHANTABLE).add(ModItems.MODERN_KINETIC_GUN.get(), ModItems.ATTACHMENT.get());
    }
}
