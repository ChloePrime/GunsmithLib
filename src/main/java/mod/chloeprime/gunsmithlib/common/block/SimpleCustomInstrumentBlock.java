package mod.chloeprime.gunsmithlib.common.block;

import cn.chloeprime.commons.world.CustomInstrumentBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;
import java.util.function.Supplier;

public class SimpleCustomInstrumentBlock extends Block implements CustomInstrumentBlock {
    private final Supplier<Holder<SoundEvent>> instrument;

    @SuppressWarnings("deprecation")
    public SimpleCustomInstrumentBlock(Supplier<SoundEvent> instrument, Properties properties) {
        super(properties);
        this.instrument = () -> BuiltInRegistries.SOUND_EVENT.wrapAsHolder(instrument.get());
    }

    @Override
    public Optional<Holder<SoundEvent>> getNoteBlockSound(Level level, BlockPos noteBlockPos, BlockState noteBlock) {
        return Optional.ofNullable(instrument.get());
    }
}
