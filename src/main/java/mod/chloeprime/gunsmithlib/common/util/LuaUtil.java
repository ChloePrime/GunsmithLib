package mod.chloeprime.gunsmithlib.common.util;

import com.google.gson.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.luaj.vm2.LuaTable;
import org.luaj.vm2.LuaValue;

import java.util.Arrays;

public class LuaUtil {
    public static JsonElement lua2json(LuaValue value) {
        if (value.isnil()) {
            return JsonNull.INSTANCE;
        }
        if (value.isboolean()) {
            return new JsonPrimitive(value.toboolean());
        }
        if (value.isnumber()) {
            return new JsonPrimitive(value.todouble());
        }
        if (value.isstring()) {
            return new JsonPrimitive(value.tojstring());
        }
        if (value.istable() && value instanceof LuaTable table) {
            if (isArray(table)) {
                var arr = new JsonArray();
                var top = new MutableInt(0);
                Arrays.stream(table.keys())
                        .mapToInt(LuaValue::toint)
                        .map(index -> index - 1)
                        .sorted().forEach(index -> {
                            // 处理不连续的 index
                            if (top.getValue() != index) {
                                reserve(arr, index);
                                top.setValue(index);
                            }
                            arr.add(lua2json(table.get(index)));
                            top.increment();
                        });
                return arr;
            } else {
                var obj = new JsonObject();
                for (LuaValue key : table.keys()) {
                    obj.add(key.tojstring(), lua2json(table.get(key)));
                }
                return obj;
            }
        }
        return JsonNull.INSTANCE;
    }

    public static boolean isArray(LuaTable table) {
        return Arrays.stream(table.keys()).allMatch(LuaValue::isint);
    }

    private static void reserve(JsonArray array, int count) {
        var delta = array.size() - count;
        for (int i = 0; i < delta; i++) {
            array.add(JsonNull.INSTANCE);
        }
    }
}
