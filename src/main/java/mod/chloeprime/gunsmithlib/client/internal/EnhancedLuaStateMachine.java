package mod.chloeprime.gunsmithlib.client.internal;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.client.resource.GunDisplayInstance;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaTable;

import java.util.Optional;

public interface EnhancedLuaStateMachine {
    LuaTable gunsmithlib$getScript();
    void gunsmithlib$setScript(LuaTable value);

    static Optional<LuaTable> getScript(AnimationStateMachine<?> asm) {
        return asm instanceof EnhancedLuaStateMachine enhanced
                ? Optional.ofNullable(enhanced.gunsmithlib$getScript())
                : Optional.empty();
    }

    static Optional<LuaTable> getScript(ItemStack stack) {
        return TimelessAPI.getGunDisplay(stack)
                .map(GunDisplayInstance::getAnimationStateMachine)
                .flatMap(EnhancedLuaStateMachine::getScript);
    }
}
