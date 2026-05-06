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
import mod.chloeprime.gunsmithlib.common.util.EqualityComparator;
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
import java.util.stream.Collectors;

@EventBusSubscriber
public class GunVariantRegistry {
    /**
     * @deprecated Since 6.0.0, variant sets are merged by gun id and unmerged variant sets became meaningless.
     */
    @Deprecated(since = "6.0.0")
    static final BiMap<ResourceLocation, GunAmmoVariantSet> REGISTRY = HashBiMap.create();

    /**
     * Stores merged variant sets since 6.0.
     */
    static final Map<ResourceLocation, GunAmmoVariantSet> BY_GUN_ID = new HashMap<>();

    /**
     * Lock to prevent threading issues in singleplayer.
     */
    static final ReadWriteLock SINGLEPLAYER_LOCK = new ReentrantReadWriteLock();

    /**
     * Get an **unmerged** variant set by its id.
     *
     * @param key the key of the **unmerged** variant set
     * @return the **unmerged** variant set
     * @deprecated Since 6.0.0, variant sets are merged by gun id and unmerged variant sets became meaningless.
     */
    @Deprecated(since = "6.0.0")
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

    /**
     * Get an **unmerged** variant set's id.
     *
     * @param value the **unmerged** variant set
     * @return the key of the **unmerged** variant set
     * @deprecated Since 6.0.0, variant sets are merged by gun id and unmerged variant sets became meaningless.
     */
    @Deprecated(since = "6.0.0")
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

    static void mergeByGunId(Collection<GunAmmoVariantSet> data) {
        var finalMap = BY_GUN_ID;
        var unmergedByGunIdPass0 = new HashMap<ResourceLocation, List<GunAmmoVariantSet>>();
        for (var entry : data) {
            for (var gunId : entry.allGunIds()) {
                var unmerged = unmergedByGunIdPass0.computeIfAbsent(gunId, _id -> new ArrayList<>(4));
                unmerged.add(entry);
            }
        }
        var unmergedByGunIdPass1 = new HashMap<ResourceLocation, Set<GunAmmoVariantSet>>();
        for (List<GunAmmoVariantSet> pass0 : unmergedByGunIdPass0.values()) {
            var allGunIds = pass0.stream()
                    .map(GunAmmoVariantSet::allGunIds)
                    .flatMap(Collection::stream)
                    .collect(Collectors.toUnmodifiableSet());
            for (var gunId : allGunIds) {
                var merged = unmergedByGunIdPass1.computeIfAbsent(gunId, _id -> new HashSet<>());
                merged.addAll(pass0);
            }
        }
        var unmergedByGunIdPass2 = new HashMap<ResourceLocation, List<GunAmmoVariantSet>>();
        unmergedByGunIdPass1.forEach((gunId, set) -> unmergedByGunIdPass2.put(gunId, new ArrayList<>(set)));

        var mergedPriority = "MERGED";
        var cache = new HashMap<ResourceLocation, GunAmmoVariantSet>();
        var unmergedPartMap = new HashMap<String, List<GunAmmoVariantSet.Part>>();
        var sortedPartNames = new LinkedHashSet<String>();
        var mergedParts = new HashMap<String, GunAmmoVariantSet.Part>();

        finalMap.clear();
        unmergedByGunIdPass2.forEach((gunId, sets) -> {
            if (sets.size() == 1) {
                finalMap.put(gunId, sets.get(0));
                return;
            }
            var cached = cache.get(gunId);
            if (cached != null) {
                finalMap.put(gunId, cached);
                return;
            }
            // Sort variant sets by variant set priority and get master.
            GunVariantSorting.sortChecked(sets, gunId::toString);
            var masterId = sets.get(0).masterId();

            // Sort part names by variant set priority.
            sets.stream()
                    .map(GunAmmoVariantSet::parts)
                    .flatMap(Collection::stream)
                    .map(GunAmmoVariantSet.Part::name)
                    .forEach(sortedPartNames::add);
            var partNames = sortedPartNames.toArray(String[]::new);

            // Collect parts by part name.
            for (var gavs : sets) {
                for (var part : gavs.parts()) {
                    unmergedPartMap.computeIfAbsent(part.name(), _name -> new ArrayList<>()).add(part);
                }
            }

            // Sort and merge parts
            unmergedPartMap.forEach((name, parts) -> {
                GunVariantSorting.sortChecked(parts, () -> "%s:%s".formatted(gunId, name));
                var variants = (List<GunAmmoVariantSet.Variant>) parts.stream()
                        .map(GunAmmoVariantSet.Part::variants)
                        .flatMap(Collection::stream)
                        .map(EqualityComparator.by(GunVariantSorting::variantEq))
                        .distinct()
                        .map(EqualityComparator::unwrap)
                        .toList();
                mergedParts.put(name, new GunAmmoVariantSet.Part(name, mergedPriority, variants));
            });

            // Construct
            var parts = Arrays.stream(partNames)
                    .map(mergedParts::get)
                    .filter(Objects::nonNull)
                    .toList();
            var mergedVariantSet = new GunAmmoVariantSet(parts, mergedPriority, masterId);
            finalMap.put(gunId, mergedVariantSet);

            // Update Cache
            mergedVariantSet.allGunIds().forEach(id -> cache.put(id, mergedVariantSet));

            // Cleanup
            sortedPartNames.clear();
            unmergedPartMap.clear();
            mergedParts.clear();
        });
        // Cleanup
        cache.clear();
    }

    static void injectGunDisplayInstanceRedirectingDataToClient(ServerPlayer player) {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        var packet = new ArrayList<ResourceLocation[]>();

        var lock = SINGLEPLAYER_LOCK.readLock();
        try {
            lock.lock();
            BY_GUN_ID.forEach((gunId, set) -> {
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
                    GunsmithLib.LOGGER.error("Master variant is empty for merged variant set of {}", gunId);
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
