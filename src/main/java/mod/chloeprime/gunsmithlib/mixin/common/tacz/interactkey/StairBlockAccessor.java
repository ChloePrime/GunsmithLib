package mod.chloeprime.gunsmithlib.mixin.common.tacz.interactkey;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.StairBlock;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(StairBlock.class)
public interface StairBlockAccessor {
    @Invoker(remap = false) Block invokeGetModelBlock();
}
