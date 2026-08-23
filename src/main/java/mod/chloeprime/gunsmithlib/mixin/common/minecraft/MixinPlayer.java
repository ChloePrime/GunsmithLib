package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldBehavior;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Player.class)
public abstract class MixinPlayer extends LivingEntity {
    @ModifyExpressionValue(
            method = "disableShield",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/player/Player;getUseItem()Lnet/minecraft/world/item/ItemStack;"))
    private ItemStack startCdForGunShieldIfIsBlockingUsingGunShield(ItemStack original) {
        var weapon = getMainHandItem();
        if (ShieldBehavior.getUsedShieldForBlockingVanillaDamage(this, weapon).filter(ShieldData::canBeDisabledByAxes).isPresent()) {
            return weapon;
        } else {
            return original;
        }
    }

    public MixinPlayer(EntityType<? extends LivingEntity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }
}
