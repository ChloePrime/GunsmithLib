package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.mixin.interactkey.StairBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

public class InteractKeyAutoInferencing {
    private static final InheritanceChecker<Block> BLOCK_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Block.class, "useWithoutItem",
            BlockState.class, Level.class, BlockPos.class, Player.class, BlockHitResult.class
    );

    private static final InheritanceChecker<Entity> ENTITY_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Entity.class, "interact",
            Player.class, InteractionHand.class
    );

    private static final InheritanceChecker<Mob> MOB_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Mob.class, "mobInteract",
            Player.class, InteractionHand.class
    );

    public static boolean inferenceCanInteractBlock(BlockState state) {
        if (!Config.INTERACT_KEY_INFERENCING.get()) {
            return false;
        }
        var block = state.getBlock();
        return block instanceof StairBlockAccessor stair
                ? BLOCK_INHERITANCE_CHECKER.isInherited(stair.getBaseState().getBlock().getClass())
                : BLOCK_INHERITANCE_CHECKER.isInherited(block.getClass());
    }

    public static boolean inferenceCanInteractEntity(Entity entity) {
        if (!Config.INTERACT_KEY_INFERENCING.get()) {
            return false;
        }
        return entity instanceof Mob hitMob
                ? MOB_INHERITANCE_CHECKER.isInherited(hitMob.getClass())
                : ENTITY_INHERITANCE_CHECKER.isInherited(entity.getClass());
    }
}
