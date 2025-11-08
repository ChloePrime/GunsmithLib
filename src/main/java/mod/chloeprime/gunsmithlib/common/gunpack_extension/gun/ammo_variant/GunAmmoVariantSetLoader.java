package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.util.SimpleCodecResourceReloadListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.entity.player.PlayerEvent;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Map;

@EventBusSubscriber
public class GunAmmoVariantSetLoader extends SimpleCodecResourceReloadListener<GunAmmoVariantSet> {
    public static final GunAmmoVariantSetLoader INSTANCE = new GunAmmoVariantSetLoader();

    private GunAmmoVariantSetLoader() {
        super(GunAmmoVariantSet.CODEC, GSON, GunsmithLib.MOD_ID + "/gun_ammo_variant_sets");
    }

    @Override
    @ParametersAreNonnullByDefault
    protected void apply(Map<ResourceLocation, GunAmmoVariantSet> data, ResourceManager resourceManager, ProfilerFiller profiler) {
        receiveData(data);
        sendToAllClientsConnected();
        GunVariantRegistry.injectGunDisplayInstanceRedirectingDataToAllClients();
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void receiveData(CompoundTag data) {
        decodeJsonFromNBT(data)
                .map(INSTANCE::decodeFromJson)
                .ifPresent(GunAmmoVariantSetLoader::receiveData);
    }

    private static void receiveData(Map<ResourceLocation, GunAmmoVariantSet> data) {
        var lock = GunVariantRegistry.SINGLEPLAYER_LOCK.writeLock();
        try {
            lock.lock();
            GunVariantRegistry.REGISTRY.clear();
            GunVariantRegistry.REGISTRY.putAll(data);

            GunVariantRegistry.BY_GUN_ID.clear();
            for (var entry : data.values()) {
                for (ResourceLocation gunId : entry.allGunIds()) {
                    GunVariantRegistry.BY_GUN_ID.put(gunId, entry);
                }
            }
        } finally {
            lock.unlock();
        }
    }

    @SubscribeEvent
    public static void onRegisteringReloadListeners(AddReloadListenerEvent event) {
        event.addListener(GunAmmoVariantSetLoader.INSTANCE);
    }

    @SubscribeEvent
    @SuppressWarnings("CodeBlock2Expr")
    public static void onPlayerLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer ssp) {
            encodeJsonToNBT(INSTANCE.raw).ifPresent(tag -> {
                RPC.call(RPCTarget.to(ssp), GunAmmoVariantSetLoader::receiveData, tag);
            });
        }
    }

    public static void sendToAllClientsConnected() {
        var server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return;
        }
        encodeJsonToNBT(INSTANCE.raw).ifPresent(tag -> {
            for (ServerPlayer player : server.getPlayerList().getPlayers()) {
                RPC.call(RPCTarget.to(player), GunAmmoVariantSetLoader::receiveData, tag);
            }
        });
    }
}
