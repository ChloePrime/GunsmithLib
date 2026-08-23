package mod.chloeprime.gunsmithlib.common.util;

import cpw.mods.modlauncher.api.INameMappingService;
import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.mixin.common.tacz.interactkey.StairBlockAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.fml.util.ObfuscationReflectionHelper;

public class InteractKeyAutoInferencing {
    private static final InheritanceChecker<Block> BLOCK_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Block.class,
            ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_6227_"),
            BlockState.class, Level.class, BlockPos.class, Player.class, InteractionHand.class, BlockHitResult.class
    );

    private static final InheritanceChecker<Entity> ENTITY_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Entity.class,
            ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_6096_"),
            Player.class, InteractionHand.class
    );

    private static final InheritanceChecker<Mob> MOB_INHERITANCE_CHECKER = new InheritanceChecker<>(
            Mob.class,
            ObfuscationReflectionHelper.remapName(INameMappingService.Domain.METHOD, "m_6071_"),
            Player.class, InteractionHand.class
    );

    public static boolean inferenceCanInteractBlock(BlockState state) {
        if (!Config.INTERACT_KEY_INFERENCING.get()) {
            return false;
        }
        var block = state.getBlock();
        return block instanceof StairBlockAccessor stair
                ? BLOCK_INHERITANCE_CHECKER.isInherited(stair.invokeGetModelBlock().getClass())
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
