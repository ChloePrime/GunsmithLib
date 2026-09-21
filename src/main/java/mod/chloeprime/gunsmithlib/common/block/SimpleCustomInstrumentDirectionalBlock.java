package mod.chloeprime.gunsmithlib.common.block;

import cn.chloeprime.commons.world.CustomInstrumentBlock;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;

import javax.annotation.Nonnull;
import java.util.Optional;

public class SimpleCustomInstrumentDirectionalBlock extends DirectionalBlock implements CustomInstrumentBlock {
    private static final MapCodec<SimpleCustomInstrumentDirectionalBlock> CODEC = RecordCodecBuilder.mapCodec(builder -> builder.group(
            BuiltInRegistries.SOUND_EVENT.holderByNameCodec().fieldOf("instrument").forGetter(blk -> blk.instrument),
            propertiesCodec()
    ).apply(builder, SimpleCustomInstrumentDirectionalBlock::new));

    private final Holder<SoundEvent> instrument;

    public SimpleCustomInstrumentDirectionalBlock(Holder<SoundEvent> instrument, Properties properties) {
        super(properties);
        this.instrument = instrument;
        this.registerDefaultState(defaultBlockState().setValue(FACING, Direction.UP));
    }

    @Override
    protected void createBlockStateDefinition(@Nonnull StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }

    @Override
    public @Nonnull BlockState rotate(BlockState state, Rotation rot) {
        return state.setValue(FACING, rot.rotate(state.getValue(FACING)));
    }

    @Override
    @SuppressWarnings("deprecation")
    public @Nonnull BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getNearestLookingDirection().getOpposite());
    }

    @Override
    public Optional<Holder<SoundEvent>> getNoteBlockSound(Level level, BlockPos noteBlockPos, BlockState noteBlock) {
        return Optional.ofNullable(instrument);
    }

    @Override
    protected @Nonnull MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }
}
