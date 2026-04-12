package mod.chloeprime.gunsmithlib.common.util;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.bus.api.Event;
import net.neoforged.bus.api.ICancellableEvent;

import java.util.Locale;

public class GsHelper1211 {
    public static boolean isCanceled(Event event) {
        return event instanceof ICancellableEvent cancellable && cancellable.isCanceled();
    }

    public static <E extends Enum<E>> StreamCodec<ByteBuf, E> enumStreamCodec(Class<E> type) {
        return ByteBufCodecs.STRING_UTF8.map(
                str -> Enum.valueOf(type, str.toUpperCase(Locale.ROOT)),
                value -> value.name().toLowerCase(Locale.ROOT));
    }
}
