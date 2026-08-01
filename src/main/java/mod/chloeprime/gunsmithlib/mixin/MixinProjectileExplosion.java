package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.util.block.ProjectileExplosion;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.damage_source_control.DamageSourceControlSystem;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ProjectileExplosion.class)
public class MixinProjectileExplosion extends Explosion {

    // 修改伤害类型，防止炸坏物品

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectDamageSourceTags(
            Level level, Entity owner, Entity exploder,
            DamageSource source, ExplosionDamageCalculator damageCalculator,
            double x, double y, double z, float power, float radius, boolean knockback,
            Explosion.BlockInteraction mode, CallbackInfo ci
    ) {
        DamageSourceControlSystem.injectExplosionDamageSource(exploder, getDamageSource());
        gunsmithlib$mercyItems = GsHelper.gunInfoFromBullet(exploder)
                .flatMap(GunExplosiveData::fromGun)
                .filter(GunExplosiveData::willPreventDestroyingLootItems)
                .isPresent();
    }

    private @Unique boolean gunsmithlib$mercyItems;

    @WrapOperation(
            method = "explode",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean doNotHurtMyLoots(Entity victim, DamageSource source, float amount, Operation<Boolean> original) {
        if (gunsmithlib$mercyItems && victim instanceof ItemEntity) {
            return false;
        }
        return original.call(victim, source, amount);
    }

    // Boilerplate

    @ApiStatus.Internal
    public MixinProjectileExplosion(Level level, @Nullable Entity source, @Nullable DamageSource damageSource, @Nullable ExplosionDamageCalculator damageCalculator, double x, double y, double z, float radius, boolean fire, BlockInteraction blockInteraction) {
        super(level, source, damageSource, damageCalculator, x, y, z, radius, fire, blockInteraction);
    }
}
