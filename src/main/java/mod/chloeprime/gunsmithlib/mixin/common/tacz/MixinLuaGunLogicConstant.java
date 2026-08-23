package mod.chloeprime.gunsmithlib.mixin.common.tacz;

import com.tacz.guns.api.vmlib.LuaGunLogicConstant;
import mod.chloeprime.gunsmithlib.api.common.GunsmithLibScriptingConstant;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Map;

@Mixin(value = LuaGunLogicConstant.class, remap = false)
public class MixinLuaGunLogicConstant {
    @Shadow @Final private Map<String, Object> constantMap;

    @Inject(method = "<init>", at = @At("TAIL"))
    private void injectGunsmithLibVersionInfo(CallbackInfo ci) {
        constantMap.putAll(GsHelper.parseStaticFields(GunsmithLibScriptingConstant.class));
    }
}
