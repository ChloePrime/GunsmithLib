package mod.chloeprime.gunsmithlib.common;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaValue;

@ApiStatus.Internal
public interface AbstractGunScriptAPIExtension extends AbstractCommonScriptingExtension, GunScriptAPIExtension {
    @Override
    default void gunsmith_triggerAnimationStateTransition(String input) {
        if (gunsmithlib$getShooter().orElse(null) instanceof ServerPlayer ssp) {
            RPC.call(RPCTarget.to(ssp), AbstractGunScriptAPIExtension::triggerAnimation, input);
        }
    }

    @Override
    default void gunsmith_addEffect(LuaValue effect) {
        gunsmithlib$getShooter().ifPresent(shooter -> gunsmith_addEffectTo(shooter, effect));
    }

    @Override
    default void gunsmith_addEffectTo(LivingEntity target, LuaValue effect) {
        var shooter = gunsmithlib$getShooter().orElse(null);
        GsHelper.lua2obj(effect, PotionEffectData.class).applyTo(target, shooter);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void triggerAnimation(String input) {
        GunsmithLibClient.triggerAnimation(input);
    }
}
