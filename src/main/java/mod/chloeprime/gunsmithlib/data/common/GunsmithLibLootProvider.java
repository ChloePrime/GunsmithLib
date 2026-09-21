package mod.chloeprime.gunsmithlib.data.common;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import javax.annotation.Nonnull;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;

public class GunsmithLibLootProvider extends LootTableProvider {
    public GunsmithLibLootProvider(PackOutput output) {
        super(output, Set.of(), entries());
    }

    private static List<LootTableProvider.SubProviderEntry> entries() {
        return List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK)
        );
    }

    private static class BlockLoot extends BlockLootSubProvider {
        private BlockLoot() {
            super(Collections.emptySet(), FeatureFlags.REGISTRY.allFlags());
        }

        @Override
        @SuppressWarnings("deprecation")
        protected @Nonnull Iterable<Block> getKnownBlocks() {
            return StreamSupport.stream(super.getKnownBlocks().spliterator(), false)
                    .filter(block -> GunsmithLib.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(block).getNamespace()))
                    .toList();
        }

        @Override
        protected void generate() {
            dropSelf(GunsmithLib.Blocks.GUN_STEEL_BLOCK.get());
            dropSelf(GunsmithLib.Blocks.GIGA_AMMO_BLOCK.get());
        }
    }
}
