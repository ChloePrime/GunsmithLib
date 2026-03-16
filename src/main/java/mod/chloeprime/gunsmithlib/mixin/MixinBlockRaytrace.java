package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.util.block.BlockRayTrace;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.raytrace_control.RaytraceControlSystem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Predicate;

@Mixin(value = BlockRayTrace.class, remap = false)
public class MixinBlockRaytrace {
    @ModifyExpressionValue(
            method = {"lambda$rayTraceBlocks$1", "lambda$rayTraceBlocks$2"},
            at = @At(value = "FIELD", opcode = Opcodes.GETSTATIC, target = "Lcom/tacz/guns/util/block/BlockRayTrace;IGNORES:Ljava/util/function/Predicate;"))
    private static Predicate<BlockState> modifyCondition(Predicate<BlockState> original, Level level, ClipContext context) {
        return RaytraceControlSystem.makePredicate(context).orElse(original);
    }
}
