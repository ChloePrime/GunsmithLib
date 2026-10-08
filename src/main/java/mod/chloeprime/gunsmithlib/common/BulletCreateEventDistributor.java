package mod.chloeprime.gunsmithlib.common;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import mod.chloeprime.gunsmithlib.api.common.BulletCreateEvent;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;

import java.util.Objects;

@EventBusSubscriber
public class BulletCreateEventDistributor {
    @SubscribeEvent(priority = EventPriority.LOWEST)
    private static void onBulletCreate(EntityJoinLevelEvent event) {
        if (!(event.getEntity() instanceof Projectile bullet)) {
            return;
        }
        if (!(bullet.getOwner() instanceof LivingEntity shooter)) {
            return;
        }

        var gunId = bullet instanceof EntityKineticBulletAccessor accessor
                ? accessor.getGunId()
                : null;
        var gun = Gunsmith.getGunInfo(shooter.getMainHandItem())
                .filter(gi -> gunId == null || gunId.equals(gi.gunId()))
                .or(() -> Gunsmith.getGunInfo(Gunsmith.createGunItemFromId(gunId, shooter.level().registryAccess())))
                .orElse(null);
        if (gun == null) {
            return;
        }

        if (!bullet.level().isClientSide()) {
            RPC.call(RPCTarget.near(shooter), BulletGunIdSync_1211::rpcSetClientGunId, bullet.getId(), gun.gunId());
        }

        var bcEvent = new BulletCreateEvent(bullet, shooter, gun);
        NeoForge.EVENT_BUS.post(bcEvent);
        NeoForge.EVENT_BUS.post(new InternalBulletCreateEvent(bcEvent));
    }

    @EventBusSubscriber
    public static class BulletGunIdSync_1211 {
        @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
        private static void rpcSetClientGunId(int entityId, ResourceLocation gunId) {
            Objects.requireNonNull(gunId);

            BULLET_ID_SYNC_TABLE.put(entityId, gunId);
        }

        @SubscribeEvent(priority = EventPriority.HIGHEST)
        private static void onEntityJoinLevel(EntityJoinLevelEvent event) {
            var gunId = BULLET_ID_SYNC_TABLE.remove(event.getEntity().getId());
            if (gunId != null && event.getEntity() instanceof EntityKineticBulletAccessor accessor) {
                accessor.setGunId(gunId);
            }
        }

        private static final Int2ObjectMap<ResourceLocation> BULLET_ID_SYNC_TABLE = new Int2ObjectOpenHashMap<>();
    }
}
