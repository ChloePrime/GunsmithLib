package mod.chloeprime.gunsmithlib.mixin.bugfix.tacz;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.Config;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(ServerEntity.class)
public class TracerStartPosFixDisableBulletTrackingMixin {
    @Shadow @Final private Entity entity;
    @Shadow @Final private boolean trackDelta;

    @ModifyExpressionValue(
            method = "sendChanges",
            at = @At(value = "NEW", target = "Lnet/minecraft/network/protocol/game/ClientboundTeleportEntityPacket;"))
    private ClientboundTeleportEntityPacket doNotTeleportBullets(ClientboundTeleportEntityPacket original) {
        if (!Config.IMPROVE_TRACER_ROTATION_STABILITY.get()) {
            return original;
        }
        if (entity instanceof EntityKineticBullet) {
            return trackDelta ? original : null;
        } else {
            return original;
        }
    }
}
