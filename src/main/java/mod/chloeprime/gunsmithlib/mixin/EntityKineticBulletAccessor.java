package mod.chloeprime.gunsmithlib.mixin;

import com.tacz.guns.entity.EntityKineticBullet;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = EntityKineticBullet.class, remap = false)
public interface EntityKineticBulletAccessor {
    @Accessor int getLife();
    @Accessor ResourceLocation getGunId();
    @Accessor void setArmorIgnore(float value);
    @Accessor void setExplosionDelayCount(int value);

    @Accessor boolean getExplosion();
    @Accessor float getExplosionRadius();

    @Accessor float getGravity();
    @Accessor void setGravity(float value);
}
