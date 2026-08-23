package mod.chloeprime.gunsmithlib.mixin.common.tacz.bugfix;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;
import net.minecraft.network.protocol.game.ClientboundMoveEntityPacket;
import net.minecraft.network.protocol.game.ClientboundTeleportEntityPacket;
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
                // 修复榴弹弹道粒子比实际轨迹下坠得更厉害
                // 装了 Arcana 以后这个 bug 就没有了，为避免更大的兼容性问题所以在装了 Arcana 以后不开启
                if (!ModInstallationStatus.ARCANA_INSTALLED && packet instanceof ClientboundMoveEntityPacket) {
                    return;
                }
                // 修复曳光弹倾斜
                if (packet instanceof ClientboundTeleportEntityPacket) {
                    return;
                }
            }
        }
        original.call(broadcast, packet);
    }
}
