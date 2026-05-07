package mod.chloeprime.gunsmithlib.mixin;

import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Entity.class)
public interface EntityAccessor {
    @Invoker boolean invokeIsInRain();
    @Invoker boolean invokeIsInBubbleColumn();
    @Accessor RandomSource getRandom();
}
