package mod.chloeprime.gunsmithlib.common;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import net.minecraft.server.level.ServerPlayer;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public interface AbstractGunScriptAPIExtension extends AbstractCommonScriptingExtension, GunScriptAPIExtension {
    @Override
    default void gunsmith_triggerAnimationStateTransition(String input) {
        if (gunsmithlib$getShooter().orElse(null) instanceof ServerPlayer ssp) {
            RPC.call(RPCTarget.to(ssp), AbstractGunScriptAPIExtension::triggerAnimation, input);
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void triggerAnimation(String input) {
        GunsmithLibClient.triggerAnimation(input);
    }
}
