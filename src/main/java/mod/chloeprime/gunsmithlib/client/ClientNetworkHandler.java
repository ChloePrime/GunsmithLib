package mod.chloeprime.gunsmithlib.client;

import it.unimi.dsi.fastutil.ints.*;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.HomingProjectileBehavior;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.network.S2CSyncLockedTarget;
import net.minecraft.client.Minecraft;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public class ClientNetworkHandler {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final Int2IntMap BULLET_TARGET_ID_MAP = new Int2IntLinkedOpenHashMap();
    static {
        BULLET_TARGET_ID_MAP.defaultReturnValue(-1);
    }

    public static void handleSyncLockedTarget(S2CSyncLockedTarget packet) {
        var level = MC.level;
        if (level == null) {
            return;
        }
        var bullet = level.getEntity(packet.bulletId());
        var target = level.getEntity(packet.targetId());
        if (target == null) {
            return;
        }
        if (bullet == null) {
            BULLET_TARGET_ID_MAP.put(packet.bulletId(), target.getId());
        } else {
            bullet.getPersistentData().putUUID(HomingProjectileBehavior.PDKEY_TARGET, target.getUUID());
        }
    }

    @SubscribeEvent
    public static void writeSyncedLockTarget(InternalBulletCreateEvent eventWrapper) {
        var bullet = eventWrapper.getImpl().getBullet();
        if (!bullet.level().isClientSide) {
            return;
        }
        int targetId = BULLET_TARGET_ID_MAP.remove(bullet.getId());
        if (targetId < 0) {
            return;
        }
        var target = Optional.ofNullable(MC.level).map(level -> level.getEntity(targetId)).orElse(null);
        if (target == null) {
            return;
        }
        bullet.getPersistentData().putUUID(HomingProjectileBehavior.PDKEY_TARGET, target.getUUID());
    }
}
