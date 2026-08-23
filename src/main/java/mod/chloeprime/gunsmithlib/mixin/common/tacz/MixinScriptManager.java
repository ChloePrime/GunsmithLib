package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.tacz.guns.resource.manager.ScriptManager;
import mod.chloeprime.gunsmithlib.common.scripting.lang.GunsmithLuaLib;
import org.luaj.vm2.Globals;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = ScriptManager.class, remap = false)
public class MixinScriptManager {
    @Inject(method = "secureStandardGlobals", at = @At("RETURN"))
    private static void installLuaLibraries(CallbackInfoReturnable<Globals> cir) {
        cir.getReturnValue().load(new GunsmithLuaLib());
    }
}
