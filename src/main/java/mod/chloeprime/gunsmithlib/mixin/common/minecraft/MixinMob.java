package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.common.util.FloatConsumer;
import mod.chloeprime.gunsmithlib.common.util.HurtFunction2;
import mod.chloeprime.gunsmithlib.common.util.HurtFunction1;
import mod.chloeprime.gunsmithlib.common.util.SpecialHurtable;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Mob.class)
public abstract class MixinMob extends LivingEntity implements SpecialHurtable {
    @Override
    public boolean gunsmith$usingSpecialHurt() {
        EntityType<?> type = getType();
        return Config.ENABLE_SPECIAL_HURT.get() && !UNSUPPORTED_TYPES.contains(type) && type.is(USE_SPECIAL_HURT);
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public HurtFunction1 getSpecialHurtFunction1() {
        return this::gunsmith$specialHurt1Wrapper;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public HurtFunction2 getSpecialHurtFunction2() {
        return this::gunsmith$specialHurt2Wrapper;
    }

    @Override
    @SuppressWarnings("AddedMixinMembersNamePattern")
    public FloatConsumer getSpecialSetHealthFunction() {
        return this::gunsmith$specialSetHealthWrapper;
    }

    private @Unique boolean gunsmith$specialHurt1Wrapper(DamageSource source, float amount) {
        try {
            gunsmith$beginSpecialHurt();
            try {
                return super.hurt(source, amount);
            } catch (Exception ex) {
                gunsmith$addUnsupportedTypeForSpecialHurt(getType(), ex);
                return hurt(source, amount);
            }
        } finally {
            gunsmith$endSpecialHurt();
        }
    }

    private @Unique void gunsmith$specialHurt2Wrapper(DamageSource source, float amount) {
        try {
            super.actuallyHurt(source, amount);
        } catch (Exception ex) {
            gunsmith$addUnsupportedTypeForSpecialHurt(getType(), ex);
            actuallyHurt(source, amount);
        }
    }

    private @Unique void gunsmith$specialSetHealthWrapper(float value) {
        try {
            super.setHealth(value);
        } catch (Exception ex) {
            gunsmith$addUnsupportedTypeForSpecialHurt(getType(), ex);
            setHealth(value);
        }
    }

    protected MixinMob(EntityType<? extends LivingEntity> type, Level world) {
        super(type, world);
    }
}
