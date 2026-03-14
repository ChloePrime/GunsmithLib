package mod.chloeprime.gunsmithlib.mixin.bugfix.luaj;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;

import java.util.function.Supplier;

@Mixin(targets = {"org.luaj.vm2.lib.jse.JavaMember"}, remap = false)
public class MixinJavaMember {
    private final @Unique Supplier<Integer> gunsmith$fixedArgsLength = Suppliers.memoize(this::gunsmith$getFixedArgsLength);

    @WrapOperation(
            method = "convertArgs",
            at = @At(value = "INVOKE", ordinal = 2, target = "Lorg/luaj/vm2/lib/jse/CoerceLuaToJava$Coercion;coerce(Lorg/luaj/vm2/LuaValue;)Ljava/lang/Object;"))
    private Object fixVarargCoerce(@Coerce Object coercion, LuaValue ignored, Operation<Object> original, Varargs args) {
        // LuaValue[] -> Object[]
        var base = gunsmith$fixedArgsLength.get();
        var argc = args.narg() - base;
        var argv = new LuaTable();
        for (int i = 0; i < argc; i++) {
            argv.set(i + 1, args.arg(base + i + 1));
        }
        return original.call(coercion, argv);
    }

    @Unique
    private int gunsmith$getFixedArgsLength() {
        try {
            return ((Object[]) getClass().getSuperclass().getDeclaredField("fixedargs").get(this)).length;
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new SecurityException("?", ex);
        }
    }
}
