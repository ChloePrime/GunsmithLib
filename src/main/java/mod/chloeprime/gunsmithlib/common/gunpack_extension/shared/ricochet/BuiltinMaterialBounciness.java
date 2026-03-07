package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.ricochet;

import it.unimi.dsi.fastutil.objects.*;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.Tags;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class BuiltinMaterialBounciness {
    private static final Object2FloatMap<TagKey<Block>> TAG_TO_BOUNCINESS = new Object2FloatArrayMap<>();
    private static final Object2FloatMap<SoundType> SND_TYPE_TO_BOUNCINESS = new Object2FloatArrayMap<>();

    static {
        TAG_TO_BOUNCINESS.put(Tags.Blocks.STORAGE_BLOCKS_GOLD, 0.6F);
    }

    static {
        SND_TYPE_TO_BOUNCINESS.defaultReturnValue(Float.NaN);
        // 软方块
        SND_TYPE_TO_BOUNCINESS.put(SoundType.POWDER_SNOW, 0);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.SLIME_BLOCK, 0);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.HONEY_BLOCK, 0);

        SND_TYPE_TO_BOUNCINESS.put(SoundType.SAND, 0.05F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.SOUL_SAND, 0.05F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.SOUL_SOIL, 0.05F);

        SND_TYPE_TO_BOUNCINESS.put(SoundType.WOOL, 0.1F);

        // 泥土太常见了，给到 0 导致频繁嵌入泥土中，失去了跳弹系统的本意）
        // 所以给泥土稍微高点的反弹系数
        SND_TYPE_TO_BOUNCINESS.put(SoundType.GRASS, 0.125F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.GRAVEL, 0.125F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.WET_GRASS, 0.125F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.ROOTED_DIRT, 0.125F);

        SND_TYPE_TO_BOUNCINESS.put(SoundType.WART_BLOCK, 0.125F);
        // 木头
        SND_TYPE_TO_BOUNCINESS.put(SoundType.WOOD, 0.25F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.BAMBOO_WOOD, 0.25F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.CHERRY_WOOD, 0.25F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.NETHER_WOOD, 0.25F);
        // 硬方块
        SND_TYPE_TO_BOUNCINESS.put(SoundType.COPPER, 0.75F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.METAL, 0.8F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.ANVIL, 0.8F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.ANCIENT_DEBRIS, 0.9F);
        SND_TYPE_TO_BOUNCINESS.put(SoundType.NETHERITE_BLOCK, 0.95F);
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onRicochet(InternalEvent.RicochetBounciness eventWrapper) {
        var event = eventWrapper.getImpl();
        if (!(event.getHitResult() instanceof BlockHitResult hit)) {
            return;
        }
        var state = event.getLevel().getBlockState(hit.getBlockPos());
        // 根据方块标签从内置表中推测
        for (var entry : TAG_TO_BOUNCINESS.object2FloatEntrySet()) {
            var tag = entry.getKey();
            var val = entry.getFloatValue();
            if (state.is(tag)) {
                event.setMaterialBouncinessOfHitTarget(val);
                return;
            }
        }
        // 根据声音类型从内置表中推测
        var bySound = SND_TYPE_TO_BOUNCINESS.getFloat(state.getSoundType());
        if (!Float.isNaN(bySound)) {
            event.setMaterialBouncinessOfHitTarget(bySound);
        }
    }
}
