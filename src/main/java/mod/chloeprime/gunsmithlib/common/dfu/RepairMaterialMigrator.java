package mod.chloeprime.gunsmithlib.common.dfu;

import com.google.gson.JsonElement;
import com.tacz.guns.resource.convert.PackConversionPlan;
import com.tacz.guns.resource.convert.UniversalPackConverter;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.mixin.common.tacz.dfu.UniversalPackConverterAccessor;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 把 1.20.1 写法的耐久修理材料（{@code gunsmithlib_extension.durability.repair_material}）
 * 迁移成 1.21.1 写法。
 * <p>
 * 耐久数据只在 GunsmithLib 内部使用，而 TaCZ 的通用升级器不会处理枪械/配件 data 文件，
 * 所以在编译 {@code DurabilityData} 的修理材料时做一次迁移，让“升级过的包”“手工转换过的包”
 * 和“没走升级器的包”都能正常工作。
 * <p>
 * 迁移规则直接复用 TaCZ 升级器的材料转换实现（{@link UniversalPackConverter} 的 ingredient 转换），
 * 避免两处规则不一致。
 *
 * @since 7.2.0
 */
public final class RepairMaterialMigrator {
    private static final String REPAIR_MATERIAL_PATH = "$.gunsmithlib_extension.durability.repair_material";
    private static final int MAX_REPORTED_MESSAGES = 64;

    private static final Object LOCK = new Object();
    private static final Set<String> REPORTED_MESSAGES = ConcurrentHashMap.newKeySet();
    private static volatile @Nullable UniversalPackConverter converter;
    private static volatile boolean unavailable;

    private RepairMaterialMigrator() {
    }

    /**
     * 把 1.20.1 写法的修理材料迁移成 1.21.1 写法。
     * <p>
     * 无法迁移时原样返回传入的 JSON（调用方会像以前一样解码失败并打日志），
     * 迁移本身不会抛出任何异常，也不会影响枪包其余数据的加载。
     *
     * @return 迁移后的 JSON；已经是新写法、或迁移不可用时返回原值
     */
    public static JsonElement migrate(JsonElement raw) {
        if (unavailable || !needsMigration(raw)) {
            return raw;
        }
        var converter = converter();
        if (converter == null) {
            return raw;
        }
        var plan = new PackConversionPlan("repair_material");
        JsonElement converted;
        try {
            // noinspection DataFlowIssue
            converted = ((UniversalPackConverterAccessor) (Object) converter).gunsmithlib$convertIngredient(raw, REPAIR_MATERIAL_PATH, plan);
        } catch (RuntimeException ex) {
            report("warning", "Failed to migrate durability repair material, keeping the legacy value: " + message(ex));
            return raw;
        }
        plan.diagnostics.forEach(diagnostic -> report(diagnostic.severity(), diagnostic.message()));
        if (!converted.equals(raw)) {
            report("info", "Migrated legacy durability repair material " + raw + " -> " + converted);
        }
        return converted;
    }

    /**
     * 只有旧版 Forge 材料类型和 {@code forge:} 标签才需要迁移。
     * 已经迁好的数据（{@code c:} / {@code minecraft:} 标签、{@code tacz:nbt} 等）直接跳过，
     * 既不重复转换，也不会让 TaCZ 的转换器把不认识的新写法当成错误报出来。
     */
    private static boolean needsMigration(JsonElement raw) {
        if (raw == null || !raw.isJsonObject()) {
            return false;
        }
        var object = raw.getAsJsonObject();
        var type = object.get("type");
        if (type != null && type.isJsonPrimitive() && type.getAsString().startsWith("forge:")) {
            return true;
        }
        var tag = object.get("tag");
        return tag != null && tag.isJsonPrimitive() && tag.getAsString().startsWith("forge:");
    }

    private static @Nullable UniversalPackConverter converter() {
        var current = converter;
        if (current != null) {
            return current;
        }
        synchronized (LOCK) {
            if (unavailable) {
                return null;
            }
            if (converter != null) {
                return converter;
            }
            try {
                converter = new UniversalPackConverter(
                        GunpackConversionSupport.registries(),
                        Map.of(),
                        GunpackConversionSupport.conversionRules());
                return converter;
            } catch (Exception ex) {
                unavailable = true;
                GunsmithLib.LOGGER.error("Failed to initialize the durability repair material migrator, "
                        + "legacy repair materials will be kept as-is", ex);
                return null;
            }
        }
    }

    private static void report(String severity, String message) {
        if (REPORTED_MESSAGES.size() >= MAX_REPORTED_MESSAGES || !REPORTED_MESSAGES.add(severity + message)) {
            return;
        }
        switch (severity) {
            case "error" -> GunsmithLib.LOGGER.error("[Durability] {}", message);
            case "info" -> GunsmithLib.LOGGER.info("[Durability] {}", message);
            default -> GunsmithLib.LOGGER.warn("[Durability] {}", message);
        }
    }

    private static String message(Exception ex) {
        return ex.getMessage() == null ? ex.getClass().getSimpleName() : ex.getMessage();
    }
}
