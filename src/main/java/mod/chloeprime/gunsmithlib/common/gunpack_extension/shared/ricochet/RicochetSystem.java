package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.ricochet;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import it.unimi.dsi.fastutil.booleans.BooleanConsumer;
import mod.chloeprime.gunsmithlib.api.common.RicochetEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.apache.commons.lang3.mutable.MutableInt;

import java.util.Map;
import java.util.WeakHashMap;

@Mod.EventBusSubscriber
public class RicochetSystem {
    /**
     * 速度斩杀线。
     * 如果反弹后速度大小小于这个值，则直接爆炸。
     */
    public static double SPEED_CUTOUT_LINE = 0.1F;

    @SubscribeEvent(priority = EventPriority.LOW)
    public static void onAmmoHitBlock(AmmoHitBlockEvent event) {
        var hit = event.getHitResult();
        var normal = Vec3.atLowerCornerOf(hit.getDirection().getNormal());
        onAmmoHitAnything(event.getAmmo(), event.getAmmo().getGunId(), hit, normal, event::setCanceled);
    }

    private static void onAmmoHitAnything(
            Projectile ammo,
            ResourceLocation gunId,
            HitResult hit,
            Vec3 normal,
            BooleanConsumer canceller
    ) {
        if (ammo.level().isClientSide()) {
            return;
        }
        var gunStack = Gunsmith.createGunItemFromId(gunId);
        var data = RicochetData.of(gunStack).orElse(null);
        if (data == null) {
            return;
        }
        // 速度过小时不计算跳弹
        var oldVelocity = ammo.getDeltaMovement();
        if (oldVelocity.lengthSqr() <= 1e-8) {
            return;
        }
        // cos(入射角)
        var dot = normal.dot(oldVelocity.normalize().scale(-1));
        // 夹角过小时不跳弹
        if (data.getMinAngleOfIncidence() > 0 && dot > Math.cos(data.getMinAngleOfIncidence())) {
            return;
        }
        // 计数
        var count = RICOCHET_COUNT.computeIfAbsent(ammo, _key -> new MutableInt(0));
        if (count.getValue() >= data.getMaxRicochetTimes()) {
            return;
        }
        count.increment();
        // 发布事件，确定命中物体的材质弹性，顺便判定是否被取消
        var event = new RicochetEvent(ammo, ammo.level(), hit, normal);
        MinecraftForge.EVENT_BUS.post(new InternalEvent.RicochetBounciness(event));

        var isCanceled = MinecraftForge.EVENT_BUS.post(event);
        if (isCanceled) {
            return;
        }
        var materialBouncy = event.getMaterialBouncinessOfHitTarget();
        // 重力缩放
        float gravityScale = count.getValue() == 1 ? (float) data.getGravityScale() : 1;
        // R = I - 2 * (I · N) * N
        var reflected = oldVelocity.subtract(normal.scale(2 * oldVelocity.dot(normal)));
        assert (Math.abs(reflected.length() - oldVelocity.length()) <= 1e-4);

        // sin(入射角)
        var sin = Math.sqrt(1 - dot * dot);
        var bulletBouncy = Mth.lerp(sin, data.getMinBounciness(), data.getMaxBounciness());
        var bounciness = materialBouncy * bulletBouncy;
        // 如果反弹速度过小（通常由物体表面较软导致）则视作子弹嵌在目标里面
        var velocityAfterReflect = reflected.scale(bounciness);
        if (velocityAfterReflect.lengthSqr() <= SPEED_CUTOUT_LINE * SPEED_CUTOUT_LINE) {
            velocityAfterReflect = Vec3.ZERO;
            gravityScale = 0;
        }
        // 将子弹弹出一段距离，防止反弹时卡在墙里导致客户端位置偏移脱节
        var reflectPos = hit.getLocation().add(normal.scale(ammo.getBoundingBox().getSize() / 2));
        canceller.accept(true);
        RPC.call(RPCTarget.near(ammo), RicochetSystem::rpcRicochet, ammo, reflectPos, velocityAfterReflect, gravityScale);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT, callLocally = true)
    private static void rpcRicochet(Projectile bullet, Vec3 pos, Vec3 velocity, float gravityScale) {
        if (bullet == null) {
            return;
        }
        bullet.setPos(pos);
        bullet.setDeltaMovement(velocity);
        bullet.lookAt(EntityAnchorArgument.Anchor.FEET, pos.add(velocity));
        if (gravityScale != 1 && bullet instanceof EntityKineticBulletAccessor accessor) {
            accessor.setGravity(gravityScale * accessor.getGravity());
        }
    }

    private static final Map<Entity, MutableInt> RICOCHET_COUNT = new WeakHashMap<>();
}
