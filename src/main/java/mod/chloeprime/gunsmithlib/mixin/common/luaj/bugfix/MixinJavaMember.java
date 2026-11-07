package mod.chloeprime.gunsmithlib.mixin.common.luaj.bugfix;

import com.google.common.base.Suppliers;
import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Slice;

import java.util.Optional;
import java.util.function.Supplier;

@Mixin(targets = {"org.luaj.vm2.lib.jse.JavaMember"}, remap = false)
public class MixinJavaMember {
    private final @Unique Supplier<Integer> gunsmith$fixedArgsLength = Suppliers.memoize(() -> gunsmith$getField("fixedargs", Object[].class).length);
    private final @Unique Supplier<Object> gunsmith$varargs = Suppliers.memoize(() -> gunsmith$getField("varargs", Object.class));

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

    /**
     * 让以参数个数等于固定参数个数调用 vararg 函数时，
     * 转换后的 varargs 多出一个参数，用于存放 new T[0]。
     */
    @ModifyExpressionValue(
            method = "convertArgs",
            at = @At(value = "INVOKE", target = "Ljava/lang/Math;max(II)I"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lorg/luaj/vm2/Varargs;narg()I"),
                    to = @At(value = "FIELD", opcode = Opcodes.GETFIELD, ordinal = 1, target = "Lorg/luaj/vm2/lib/jse/JavaMember;varargs:Lorg/luaj/vm2/lib/jse/CoerceLuaToJava$Coercion;")
            ))
    private int addEmptyArrayForVararg(int original) {
        var needsExtraArg = original == gunsmith$fixedArgsLength.get() && gunsmith$varargs.get() != null;
        return original + (needsExtraArg ? 1 : 0);
    }

    @Unique
    private <T> T gunsmith$getField(String field, Class<T> type) {
        try {
            return Optional.ofNullable(getClass().getSuperclass().getDeclaredField(field).get(this))
                    .map(type::cast)
                    .orElse(null);
        } catch (IllegalAccessException | NoSuchFieldException ex) {
            throw new SecurityException("?", ex);
        }
    }
}
