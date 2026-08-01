package mod.chloeprime.gunsmithlib.mixin.mc1211;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.level.Explosion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Explosion.class)
public interface ExplosionAccessor1211 {
    @Accessor DamageSource getDamageSource();
}
