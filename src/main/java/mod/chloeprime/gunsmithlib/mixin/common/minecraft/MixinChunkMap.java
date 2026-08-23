package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.HomingProjectileBehavior;
import net.minecraft.server.level.ChunkMap;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ChunkMap.class)
public class MixinChunkMap {
    @ModifyExpressionValue(
            method = "addEntity",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/EntityType;trackDeltas()Z"))
    private boolean fixHomingBulletDrifting(boolean original, Entity entity) {
        if (!(entity instanceof EntityKineticBullet bullet)) {
            return original;
        }
        return bullet.getPersistentData().getBoolean(HomingProjectileBehavior.PDKEY_ENABLED);
    }
}
