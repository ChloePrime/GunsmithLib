package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import org.luaj.vm2.luajc.JavaLoader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = JavaLoader.class, remap = false)
public class LuaJcClassloadingFixMixin {
    @WrapOperation(
            method = "load(Ljava/lang/String;Lorg/luaj/vm2/LuaValue;)Lorg/luaj/vm2/LuaFunction;",
            at = @At(value = "INVOKE", target = "Lorg/luaj/vm2/luajc/JavaLoader;loadClass(Ljava/lang/String;)Ljava/lang/Class;"))
    private static Class<?> useGoodClassLoader(JavaLoader instance, String name, Operation<Class<?>> original) throws ClassNotFoundException {
        return GunsmithLib.class.getClassLoader().loadClass(name);
    }
}
