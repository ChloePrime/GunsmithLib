package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.google.common.collect.*;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

@EventBusSubscriber
public class GunVariantRegistry {
    static final BiMap<ResourceLocation, GunAmmoVariantSet> REGISTRY = HashBiMap.create();
    static final Map<ResourceLocation, GunAmmoVariantSet> BY_GUN_ID = new HashMap<>();
    static final ReadWriteLock SINGLEPLAYER_LOCK = new ReentrantReadWriteLock();

    public static @Nullable GunAmmoVariantSet getValue(@Nullable ResourceLocation key) {
        if (key == null) {
            return null;
        }
        var lock = SINGLEPLAYER_LOCK.readLock();
        try {
            lock.lock();
            return REGISTRY.get(key);
        } finally {
            lock.unlock();
        }
    }

    public static @Nullable ResourceLocation getKey(@Nullable GunAmmoVariantSet value) {
        if (value == null) {
            return null;
        }
        var lock = SINGLEPLAYER_LOCK.readLock();
        try {
            lock.lock();
            return REGISTRY.inverse().get(value);
        } finally {
            lock.unlock();
        }
    }

    static void injectGunDisplayInstanceRedirectingDataToAllClients() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            injectGunDisplayInstanceRedirectingDataToClient(player);
        }
    }

    private static void injectGunDisplayInstanceRedirectingDataToClient(ServerPlayer player) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        var packet = new ArrayList<ResourceLocation[]>();

        var lock = SINGLEPLAYER_LOCK.readLock();
        try {
            lock.lock();
            REGISTRY.forEach((setId, set) -> {
                if (set == null || set.parts().isEmpty()) {
                    return;
                }
                var firstPart = set.parts().getFirst();
                if (firstPart.variants().isEmpty()) {
                    return;
                }
                var entry = new ArrayList<ResourceLocation>(set.allGunIds().size() + 1);
                // add master display id
                var master = set.masterId().or(firstPart.variants().getFirst().getGunIds().stream()::findFirst);
                if (master.isEmpty()) {
                    GunsmithLib.LOGGER.error("Master variant is empty for variant set {}", setId);
                    return;
                }
                entry.add(master.get());
                // add all ids
                entry.addAll(set.allGunIds());
                // build "line"
                packet.add(entry.toArray(ResourceLocation[]::new));
            });
        } finally {
            lock.unlock();
        }

        RPC.call(RPCTarget.to(player), GunVariantRegistry::rpcApplyInjectionData, packet.toArray(ResourceLocation[][]::new));
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void rpcApplyInjectionData(ResourceLocation[][] encodedFullPacket) {
        var map = ImmutableMap.<ResourceLocation, List<ResourceLocation>>builder();
        for (var encodedEntry : encodedFullPacket) {
            var master = encodedEntry[0];
            var ids = Arrays.asList(encodedEntry).subList(1, encodedEntry.length);
            map.put(master, ids);
        }
        GunsmithLibClient.applyDisplayRedirectionData(map.buildKeepingLast());
    }

    /**
     * 玩家登录时发送 redirect 数据
     */
    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer ssp) {
            injectGunDisplayInstanceRedirectingDataToClient(ssp);
        }
    }

    public static boolean isEnabledFor(ItemStack gun) {
        var lock = SINGLEPLAYER_LOCK.readLock();
        try {
            lock.lock();
            return Gunsmith.getGunInfo(gun).map(GunInfo::gunId).filter(BY_GUN_ID::containsKey).isPresent();
        } finally {
            lock.unlock();
        }
    }
}
