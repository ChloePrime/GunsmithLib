package mod.chloeprime.gunsmithlib.common.scripting.lang;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.resources.ResourceLocation;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.Varargs;
import org.luaj.vm2.lib.VarArgFunction;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.awt.*;

public class GunsmithLuaLib extends VarArgFunction {
    static final int INIT               = 0;
    static final int NEW_IDENTIFIER_1   = 1;
    static final int NEW_IDENTIFIER_2   = 2;
    static final int NEW_JWT_COLOR      = 3;
    static final int INT_COLOR_FROM_RGB = 4;

    static final String[] NAMES = {
            "new_identifier",
            "new_resource_location",
            "new_color",
            "int_color_from_rgb",
    };

    @Override
    public Varargs invoke(Varargs args) {
        if (opcode == INIT) {
            LuaValue env = args.arg(2);
            LuaTable t = new LuaTable();
            bind(t, this.getClass(), NAMES, NEW_IDENTIFIER_1);
            env.set("gunsmithlib", t);
            if (!env.get("package").isnil()) {
                env.get("package").get("loaded").set("gunsmithlib", t);
            }
            return t;
        } else {
            return CoerceJavaToLua.coerce(invoke0(args));
        }
    }

    private Object invoke0(Varargs args) {
        switch (opcode) {
            case NEW_IDENTIFIER_1, NEW_IDENTIFIER_2 -> {
                return newResourceLocation(args);
            }
            case NEW_JWT_COLOR -> {
                return ColorParser.newColor(args);
            }
            case INT_COLOR_FROM_RGB -> {
                return ColorParser.newColor(args).getRGB();
            }
            default -> throw new LuaError("not yet supported: %s[%d]".formatted(this, opcode));
        }
    }

    private static ResourceLocation newResourceLocation(Varargs args) {
        var hasArg2 = args.narg() >= 2;
        if (hasArg2) {
            return GunsmithLib.loc(args.checkjstring(1), args.checkjstring(2));
        }
        return ResourceLocation.tryParse(args.checkjstring(1));
    }
}
