package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.apache.commons.lang3.tuple.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = EntityKineticBullet.class, remap = false)
public class AlternativeArmorPierceFormulaMixin {
    @Shadow
    private float armorIgnore;
    private static final @Unique ResourceLocation gunsmithlib$AP_MODIFIER_ID = GunsmithLib.loc("bullet_armor_piercing");

    @WrapOperation(
            method = "tacAttackEntity",
            at = @At(value = "INVOKE", ordinal = 0, remap = true, target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean altFormula(
            Entity target, DamageSource source, float originalAmount, Operation<Boolean> original,
            EntityKineticBullet.MaybeMultipartEntity parts, float fullDamageOfThisHit, Pair<DamageSource, DamageSource> sources
    ) {
        if (!Config.ALTERNATIVE_ARMOR_PIERCING_FORMULA.get()) {
            return original.call(target, source, originalAmount);
        }
        // 打中非生物，不计算穿甲
        if (!(target instanceof LivingEntity victim)) {
            return original.call(target, source, fullDamageOfThisHit);
        }
        // 打中生物，开始穿甲
        var armor = victim.getAttribute(Attributes.ARMOR);
        var tough = victim.getAttribute(Attributes.ARMOR_TOUGHNESS);
        var modifier = new AttributeModifier(gunsmithlib$AP_MODIFIER_ID, -this.armorIgnore, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL);
        try {
            if (armor != null) {
                armor.addTransientModifier(modifier);
            }
            if (tough != null) {
                tough.addTransientModifier(modifier);
            }
            return original.call(target, source, fullDamageOfThisHit);
        } finally {
            if (armor != null) {
                armor.removeModifier(modifier);
            }
            if (tough != null) {
                tough.removeModifier(modifier);
            }
        }
    }

    /**
     * 取消穿甲段伤害
     */
    @WrapOperation(
            method = "tacAttackEntity",
            at = @At(value = "INVOKE", ordinal = 1, remap = true, target = "Lnet/minecraft/world/entity/Entity;hurt(Lnet/minecraft/world/damagesource/DamageSource;F)Z"))
    private boolean cancelApPartDamage(
            Entity target, DamageSource source, float originalAmount, Operation<Boolean> original,
            EntityKineticBullet.MaybeMultipartEntity parts, float fullDamageOfThisHit, Pair<DamageSource, DamageSource> sources
    ) {
        if (Config.ALTERNATIVE_ARMOR_PIERCING_FORMULA.get()) {
            return false;
        } else {
            return original.call(target, source, originalAmount);
        }
    }
}
