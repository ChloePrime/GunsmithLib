package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.api.client.animation.statemachine.AnimationStateContext;
import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import com.tacz.guns.api.client.animation.statemachine.LuaStateMachineFactory;
import mod.chloeprime.gunsmithlib.client.internal.EnhancedLuaStateMachine;
import org.luaj.vm2.LuaTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = LuaStateMachineFactory.class, remap = false)
public class MixinLuaStateMachineFactory<T extends AnimationStateContext> {
    private @Unique LuaTable gunsmithlib$script;

    @Inject(method = "setLuaScripts", at = @At("HEAD"))
    private void captureScriptObject(LuaTable table, CallbackInfoReturnable<LuaStateMachineFactory<T>> cir) {
        gunsmithlib$script = table;
    }

    @Inject(method = "build", at = @At("RETURN"))
    private void injectScriptInfoToArtifact(CallbackInfoReturnable<LuaAnimationStateMachine<T>> cir) {
        if (cir.getReturnValue() instanceof EnhancedLuaStateMachine accessor) {
            accessor.gunsmithlib$setScript(gunsmithlib$script);
        } else {
            throw new IllegalStateException();
        }
    }
}
