package mod.chloeprime.gunsmithlib.mixin.client.tacz;

import com.tacz.guns.api.client.animation.statemachine.LuaAnimationStateMachine;
import mod.chloeprime.gunsmithlib.client.internal.EnhancedLuaStateMachine;
import org.luaj.vm2.LuaTable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(LuaAnimationStateMachine.class)
public class MixinLuaAnimationStateMachine implements EnhancedLuaStateMachine {
    private @Unique LuaTable gunsmithlib$script;

    @Override
    public LuaTable gunsmithlib$getScript() {
        return gunsmithlib$script;
    }

    @Override
    public void gunsmithlib$setScript(LuaTable value) {
        gunsmithlib$script = value;
    }
}
