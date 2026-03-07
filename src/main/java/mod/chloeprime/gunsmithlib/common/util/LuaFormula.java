package mod.chloeprime.gunsmithlib.common.util;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import org.luaj.vm2.Globals;
import org.luaj.vm2.LoadState;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Prototype;
import org.luaj.vm2.compiler.LuaC;
import org.luaj.vm2.lib.Bit32Lib;
import org.luaj.vm2.lib.PackageLib;
import org.luaj.vm2.lib.TableLib;
import org.luaj.vm2.lib.jse.JseBaseLib;
import org.luaj.vm2.lib.jse.JseMathLib;
import org.luaj.vm2.lib.jse.JseStringLib;
import org.luaj.vm2.luajc.LuaJC;

import javax.annotation.Nonnull;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.function.Supplier;

public class LuaFormula {

    private static final ThreadLocal<Globals> FORMULA_GLOBALS = ThreadLocal.withInitial(LuaFormula::secureStandardGlobals);

    private final Supplier<String> field;
    private Object lastRecordedField;
    private Compiled compiled;

    public LuaFormula(
            Supplier<String> field
    ) {
        this.field = field;
    }

    public Supplier<String> getSourceCodeField() {
        return field;
    }

    public @Nonnull Compiled getCompiledValue() {
        var code = Objects.requireNonNullElse(getSourceCodeField().get(), """
                # WARN: formula string is null
                return 0;""");
        if (code == lastRecordedField && compiled != null) {
            return compiled;
        }
        lastRecordedField = code;
        return compiled = compile(code);
    }

    private Compiled compile(String code) {
        try {
            var value = Double.parseDouble(code);
            return new SimpleValue(value);
        } catch (NumberFormatException ignored) {
        }
        var codeFile = code.getBytes(StandardCharsets.UTF_8);
        try {
            var prototype = LuaC.instance.compile(new ByteArrayInputStream(codeFile), "formula");
            return new LuaPrototype(code, prototype);
        } catch (IOException ex) {
            GunsmithLib.LOGGER.warn("Failed to compile lua formula {}", code, ex);
            return new SimpleValue(0);
        }
    }

    public double eval() {
        var compiled = getCompiledValue();
        if (compiled instanceof SimpleValue(double value1)) {
            return value1;
        }
        if (compiled instanceof LuaPrototype(String source, Prototype prototype1)) {
            var globals = FORMULA_GLOBALS.get();
            LuaValue ret;
            try {
                ret = LuaJC.instance.load(prototype1, "formula", globals);
            } catch (Exception ex) {
                GunsmithLib.LOGGER.warn("Failed to load lua formula {}", source, ex);
                return 0;
            }
            return ret.todouble();
        }
        return 0;
    }

    private static Globals secureStandardGlobals() {
        Globals globals = new Globals();
        globals.load(new JseBaseLib());
        globals.load(new PackageLib());
        globals.load(new Bit32Lib());
        globals.load(new TableLib());
        globals.load(new JseStringLib());
        // No CoroutineLib
        globals.load(new JseMathLib());
        // No JseIoLib
        // No JseOsLib
        // No LuajavaLib
        LoadState.install(globals);
        LuaC.install(globals);
        LuaJC.install(globals);
        return globals;
    }

    public sealed interface Compiled permits SimpleValue, LuaPrototype {
    }

    public record SimpleValue(double value) implements Compiled {
    }

    public record LuaPrototype(
            String source,
            Prototype prototype
    ) implements Compiled {
    }
}
