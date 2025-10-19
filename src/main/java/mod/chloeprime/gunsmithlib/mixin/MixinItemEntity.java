package mod.chloeprime.gunsmithlib.mixin;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.GunExplosiveData;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class MixinItemEntity {
    /**
     * 防止枪弹爆炸炸坏物品。
     * 这个功能需要枪械 data 中开启才有效。
     */
    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    private void preventGunExplosiveFromDestroyingItems(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        if (!source.is(DamageTypeTags.IS_EXPLOSION)) {
            return;
        }
        if (!(source.getEntity() instanceof LivingEntity shooter)) {
            return;
        }
        var explosiveImmune = GunExplosiveData
                .fromGun(shooter.getMainHandItem())
                .filter(GunExplosiveData::willPreventDestroyingLootItems)
                .isPresent();
        if (explosiveImmune) {
            cir.setReturnValue(false);
        }
    }
}
