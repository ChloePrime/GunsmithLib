package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import cn.chloeprime.commons.async.TaskScheduler;
import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.util.SimpleCodecResourceReloadListener;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import net.neoforged.neoforge.event.OnDatapackSyncEvent;

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
            GunVariantRegistry.mergeByGunId(data.values());
        } finally {
            lock.unlock();
        }
    }

    @SubscribeEvent
    public static void onRegisteringReloadListeners(AddReloadListenerEvent event) {
        event.addListener(GunAmmoVariantSetLoader.INSTANCE);
    }

    private static final TaskScheduler DELAYER = TaskScheduler.createTickBased(LogicalSide.SERVER);

    @SubscribeEvent
    public static void syncRegistryDataOnDatapackSync(OnDatapackSyncEvent event) {
        encodeJsonToNBT(INSTANCE.raw).ifPresent(tag -> {
            event.getRelevantPlayers().forEach(player -> {
                DELAYER.withCondition(player::isAlive).delay(1, task -> {
                    RPC.call(RPCTarget.to(player), GunAmmoVariantSetLoader::receiveData, tag);
                    GunVariantRegistry.injectGunDisplayInstanceRedirectingDataToClient(player);
                });
            });
        });
    }
}
