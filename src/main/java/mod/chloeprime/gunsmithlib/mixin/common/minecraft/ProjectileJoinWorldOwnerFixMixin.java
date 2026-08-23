package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReceiver;
import mod.chloeprime.gunsmithlib.common.BulletCreateEventDistributor;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.network.PlayMessages;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;
import java.util.function.Supplier;

/**
 * MC1.20.1 Only
 */
@Mixin(value = PlayMessages.SpawnEntity.class, remap = false)
public class ProjectileJoinWorldOwnerFixMixin {
    private static @Unique Entity apotheosis_modern_ragnarok_zero$capturedEntity;

    @ModifyReceiver(
            method = "lambda$handle$2",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/world/entity/Entity;setUUID(Ljava/util/UUID;)V")
    )
    private static Entity captureEntity(Entity entity, UUID pUniqueId) {
        if (entity instanceof Projectile) {
            apotheosis_modern_ragnarok_zero$capturedEntity = entity;
        }
        return entity;
    }

    @Inject(method = "lambda$handle$2", at = @At("RETURN"))
    private static void doFixAndCleanup(PlayMessages.SpawnEntity msg, Supplier<?> ctx, CallbackInfo ci) {
        var entity = apotheosis_modern_ragnarok_zero$capturedEntity;
        if (entity == null) {
            return;
        }
        BulletCreateEventDistributor.onBulletCreate0(new EntityJoinLevelEvent(entity, entity.level()), true);
        apotheosis_modern_ragnarok_zero$capturedEntity = null;
    }
}
