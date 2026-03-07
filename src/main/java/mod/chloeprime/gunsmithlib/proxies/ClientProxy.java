package mod.chloeprime.gunsmithlib.proxies;

import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.BlockPos;
import net.minecraft.core.RegistryAccess;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.LogicalSide;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientProxy {
    private static final boolean DEDICATED_SERVER = FMLLoader.getDist().isDedicatedServer();
    private static final AtomicBoolean GET_ENTITY_LOGGED = new AtomicBoolean(false);

    public static float getPartialTick() {
        return DEDICATED_SERVER ? 1 : ClientProxyImpl.getPartialTick();
    }

    public static Optional<RegistryAccess> getRegistryAccess() {
        if (DEDICATED_SERVER || EffectiveSide.get().isServer()) {
            return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()).map(MinecraftServer::registryAccess);
        } else {
            return ClientProxyImpl.getRegistryAccess();
        }
    }

    public static LogicalSide sideOf(Level level) {
        return level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public static Optional<Entity> getEntityByUuid(Level level, UUID uuid) {
        if (level instanceof ServerLevel serverLevel) {
            return Optional.ofNullable(serverLevel.getEntity(uuid));
        } else if (!DEDICATED_SERVER) {
            return Optional.ofNullable(ClientProxyImpl.getEntityByUuid(level, uuid));
        }
        if (!GET_ENTITY_LOGGED.getAndSet(true)) {
            GunsmithLib.LOGGER.warn("Unknown dist in {}::{}", ClientProxy.class.getCanonicalName(), "getEntityByUuid", new IllegalStateException());
        }
        return Optional.empty();
    }

    public static Vec3 bobCompensation(LogicalSide side, Vec3 vec) {
        if (DEDICATED_SERVER || side.isServer()) {
            return vec;
        } else {
            return ClientProxyImpl.bobCompensation(vec);
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    public static void receiveNoParticleExplodePacket(Vec3 pos, float power, BlockPos[] toBlow, Vec3 knockback) {
        if (DEDICATED_SERVER) {
            return;
        }
        ClientProxyImpl.receiveNoParticleExplodePacket(pos, power, toBlow, knockback);
    }
}
