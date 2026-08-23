package mod.chloeprime.gunsmithlib.mixin.common.tacz.interactkey;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.config.util.InteractKeyConfigRead;
import mod.chloeprime.gunsmithlib.common.util.InteractKeyAutoInferencing;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = InteractKeyConfigRead.class, remap = false)
public class TacInteractKeyAutoInferenceMixin {
    @ModifyReturnValue(method = "canInteractBlock", at = @At("TAIL"))
    private static boolean inferenceBlock(boolean original, BlockState block) {
        return original || InteractKeyAutoInferencing.inferenceCanInteractBlock(block);
    }

    @ModifyReturnValue(method = "canInteractEntity", at = @At("TAIL"))
    private static boolean inferenceBlock(boolean original, Entity entity) {
        return original || InteractKeyAutoInferencing.inferenceCanInteractEntity(entity);
    }
}
