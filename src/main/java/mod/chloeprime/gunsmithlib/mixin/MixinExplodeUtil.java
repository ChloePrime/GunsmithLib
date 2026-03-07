package mod.chloeprime.gunsmithlib.mixin;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.ExplodeUtil;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleSystem;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundExplodePacket;
import net.minecraft.server.network.ServerGamePacketListenerImpl;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Slice;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = ExplodeUtil.class, remap = false)
public class MixinExplodeUtil {
    private static @Unique boolean gunsmith$useNoParticlePacket;

    @Inject(
            method = "createExplosion",
            at = @At(
                    value = "INVOKE", remap = true, target = "Lnet/minecraft/server/level/ServerLevel;players()Ljava/util/List;"),
                    slice = @Slice(
                            from = @At(value = "INVOKE", remap = true, target = "Lcom/tacz/guns/util/block/ProjectileExplosion;finalizeExplosion(Z)V"),
                            to = @At("TAIL")
                    ))
    private static void hideParticleWhenConfiguredHitParticle(Entity owner, Entity exploder, float damage, float radius, boolean knockback, boolean destroy, Vec3 hitPos, CallbackInfo ci) {
        gunsmith$useNoParticlePacket = false;
        if (!(exploder instanceof EntityKineticBullet bullet)) {
            return;
        }
        gunsmith$useNoParticlePacket = HitParticleSystem.isHidingExplodeParticle(Gunsmith.createGunItemFromId(bullet.getGunId()));
    }

    @WrapOperation(
            method = "lambda$createExplosion$1",
            at = @At(value = "INVOKE", remap = true, target = "Lnet/minecraft/server/network/ServerGamePacketListenerImpl;send(Lnet/minecraft/network/protocol/Packet;)V"))
    private static void hideParticleWhenConfiguredHitParticle(ServerGamePacketListenerImpl connection, Packet<?> packet, Operation<Void> original) {
        if (gunsmith$useNoParticlePacket && packet instanceof ClientboundExplodePacket explosion) {
            var pos = new Vec3(explosion.getX(), explosion.getY(), explosion.getZ());
            var power = explosion.getPower();
            var toBlow = explosion.getToBlow().toArray(BlockPos[]::new);
            var knockback = new Vec3(explosion.getKnockbackX(), explosion.getKnockbackY(), explosion.getKnockbackZ());
            RPC.call(RPCTarget.to(connection.player), ClientProxy::receiveNoParticleExplodePacket, pos, power, toBlow, knockback);
        } else {
            original.call(connection, packet);
        }
    }
}
