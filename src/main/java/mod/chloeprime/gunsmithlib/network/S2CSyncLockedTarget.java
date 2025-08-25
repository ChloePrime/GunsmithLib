package mod.chloeprime.gunsmithlib.network;

import io.netty.buffer.ByteBuf;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.client.ClientNetworkHandler;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import javax.annotation.Nonnull;

public record S2CSyncLockedTarget(
        int bulletId,
        int targetId
) implements CustomPacketPayload {
    public static final Type<S2CSyncLockedTarget> TYPE = new Type<>(GunsmithLib.loc("sync_locked_target"));
    public static final StreamCodec<ByteBuf, S2CSyncLockedTarget> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.INT, S2CSyncLockedTarget::bulletId, ByteBufCodecs.INT, S2CSyncLockedTarget::targetId, S2CSyncLockedTarget::new
    );

    public void handle(IPayloadContext ignored) {
        ClientNetworkHandler.handleSyncLockedTarget(this);
    }

    @Override
    public @Nonnull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
