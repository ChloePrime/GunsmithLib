package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.tacz.guns.api.event.common.EntityHurtByGunEvent;
import com.tacz.guns.api.event.common.GunFireEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.screen_shake.ScreenShakeSystemClient;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.mixin.common.tacz.EntityKineticBulletAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Objects;
import java.util.Optional;

/**
 * @since 7.0.0
 */
@Mod.EventBusSubscriber
public final class ScreenShakeSystemCommon {
    @SubscribeEvent
    public static void onAmmoHitAnything(InternalEvent.AmmoHitAnything.Post eventWrapper) {
        var event = eventWrapper.getImpl();
        if (event.getHitResult().getType() == HitResult.Type.MISS) {
            return;
        }
        if (event.getLevel().isClientSide()) {
            return;
        }
        var bullet = event.getAmmo();
        var optShakeData = GsHelper.gunInfoFromBullet(bullet).flatMap(ScreenShakeData::of);
        var type = ((EntityKineticBulletAccessor) bullet).isExplosion()
                ? ScreenShakeType.EXPLOSION
                : ScreenShakeType.IMPACT;
        if (optShakeData.flatMap(type.getField()).isPresent()) {
            var owner = bullet.getOwner() instanceof LivingEntity shooter ? shooter : null;
            applyScreenShakeAt(bullet, event.getHitResult().getLocation(), owner, bullet.getGunId(), type);
        }
    }

    @SubscribeEvent
    public static void onLivingFire(GunFireEvent event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(event.getGunItemStack()).orElse(null);
        var hasShake = gun != null && Optional.of(gun)
                .flatMap(ScreenShakeData::of)
                .flatMap(ScreenShakeType.SHOOT.getField())
                .isPresent();
        if (hasShake) {
            var shooter = event.getShooter();
            applyScreenShakeAt(shooter, shooter.getEyePosition(), shooter, gun.gunId(), ScreenShakeType.SHOOT);
        }
    }

    @SubscribeEvent
    public static void onLivingGotHit(EntityHurtByGunEvent.Post event) {
        if (event.getLogicalSide().isClient()) {
            return;
        }
        if (!(event.getHurtEntity() instanceof ServerPlayer victim)) {
            return;
        }
        var gun = GsHelper.gunInfoFromShooter(event.getAttacker(), event.getGunId()).orElse(null);
        var config = Optional.ofNullable(gun)
                .flatMap(ScreenShakeData::of)
                .flatMap(ScreenShakeType.HIT.getField())
                .orElse(null);
        if (config != null) {
            var bullet = event.getBullet();
            var eyePos = victim.getEyePosition();
            Vec3 pos;
            if (bullet != null) {
                if (!Objects.equals(bullet.position(), eyePos)) {
                    pos = eyePos.add(bullet.position().subtract(eyePos)
                            .normalize()
                            .scale(config.getRange() / 100));
                } else {
                    pos = bullet.position();
                }
            } else {
                pos = victim.getEyePosition().add(victim.getLookAngle().scale(config.getRange() / 100));
            }
            var shooter = event.getAttacker();
            applyScreenShakeTo(victim, pos, shooter, gun.gunId(), ScreenShakeType.HIT);
        }
    }

    public static void applyScreenShakeAt(Entity center, Vec3 pos, LivingEntity shooter, ResourceLocation gunId, ScreenShakeType type) {
        RPC.call(RPCTarget.near(center), ScreenShakeSystemCommon::rpcApplyScreenShake, pos, shooter, gunId, type);
    }

    public static void applyScreenShakeTo(ServerPlayer victim, Vec3 pos, LivingEntity shooter, ResourceLocation gunId, ScreenShakeType type) {
        RPC.call(RPCTarget.to(victim), ScreenShakeSystemCommon::rpcApplyScreenShake, pos, shooter, gunId, type);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void rpcApplyScreenShake(Vec3 pos, LivingEntity shooter, ResourceLocation gunId, ScreenShakeType type) {
        ScreenShakeSystemClient.applyScreenShakeAt(pos, shooter, gunId, type.getField());
    }

    private ScreenShakeSystemCommon() {
    }
}
