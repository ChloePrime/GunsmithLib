package mod.chloeprime.gunsmithlib.mixin.bugfix.tacz;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.Config;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.server.level.ServerEntity;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;

import java.util.function.Consumer;

@Mixin(ServerEntity.class)
public class TracerStartPosFixDisableBulletTrackingMixin {
    @Shadow @Final private Entity entity;
    @Shadow @Final private boolean trackDelta;

    @WrapOperation(
            method = "sendChanges",
            at = @At(value = "INVOKE", target = "Ljava/util/function/Consumer;accept(Ljava/lang/Object;)V"))
    private <T> void doNotTeleportBullets(Consumer<T> broadcast, T packet, Operation<Void> original) {
        if (Config.IMPROVE_TRACER_ROTATION_STABILITY.get()) {
            if (!trackDelta && entity instanceof EntityKineticBullet) {
                if (packet instanceof ClientboundMoveEntityPacket) {
                    return;
                }
            }
        }
        original.call(broadcast, packet);
    }
}
