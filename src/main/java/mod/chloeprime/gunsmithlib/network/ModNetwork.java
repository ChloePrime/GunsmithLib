package mod.chloeprime.gunsmithlib.network;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

@EventBusSubscriber
public final class ModNetwork {
    @SubscribeEvent
    public static void register(RegisterPayloadHandlersEvent event) {
        // Sets the current network version
        final String version = String.valueOf(1);
        final PayloadRegistrar registrar = event.registrar(version);

        registrar.playToClient(S2CSyncLockedTarget.TYPE, S2CSyncLockedTarget.STREAM_CODEC, S2CSyncLockedTarget::handle);
    }

    private ModNetwork() {}
}
