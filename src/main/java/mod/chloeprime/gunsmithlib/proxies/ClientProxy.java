package mod.chloeprime.gunsmithlib.proxies;

import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.fml.util.thread.EffectiveSide;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

public class ClientProxy {
    private static final boolean DEDICATED_SERVER = FMLLoader.getDist().isDedicatedServer();
    private static final AtomicBoolean GET_ENTITY_LOGGED = new AtomicBoolean(false);

    public static LogicalSide sideOf(Level level) {
        return level.isClientSide ? LogicalSide.CLIENT : LogicalSide.SERVER;
    }

    public static Optional<Level> clientLevel() {
        if (DEDICATED_SERVER) {
            GunsmithLib.LOGGER.warn("Trying to access client level on dedicated server.");
            return Optional.empty();
        }
        if (!FMLLoader.isProduction() && EffectiveSide.get().isServer()) {
            GunsmithLib.LOGGER.warn("Trying to access client level on logical server.");
        }
        return ClientProxyImpl.clientLevel();
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

    public static long getGameTime() {
        return getGameTime(EffectiveSide.get().isClient());
    }

    public static long getGameTime(boolean isClientSide) {
        if (DEDICATED_SERVER || !isClientSide) {
            return Optional.ofNullable(ServerLifecycleHooks.getCurrentServer())
                    .map(MinecraftServer::overworld)
                    .map(Level::getGameTime)
                    .orElse(0L);
        }
        return ClientProxyImpl.getGameTime();
    }

    public static float getPartialTicks() {
        return getPartialTicks(EffectiveSide.get().isClient());
    }

    public static float getPartialTicks(boolean isClientSide) {
        if (DEDICATED_SERVER || !isClientSide) {
            return 0;
        }
        return ClientProxyImpl.getPartialTicks();
    }
}
