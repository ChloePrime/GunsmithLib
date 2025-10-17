package mod.chloeprime.gunsmithlib.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityKineticBullet.class, remap = false)
public interface EntityKineticBulletAccessor {
    @Accessor void setArmorIgnore(float value);
    @Accessor void setExplosionDelayCount(int value);
}
