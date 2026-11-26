package mod.chloeprime.gunsmithlib.common.dfu;

import cn.chloeprime.commons.ContextUtil;
import com.tacz.guns.resource.convert.PackConversionRules;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.loading.FMLPaths;

/**
 * GunsmithLib 使用的 1.20.1 → 1.21.1 枪包转换配置：注册表来源与转换规则。
 * <p>
 * 枪包升级器（{@link GunpackUpgraderWrapper}）和运行期的旧数据迁移（{@link RepairMaterialMigrator}）
 * 必须使用完全相同的规则，否则同一个枪包“升级时”和“运行时”的转换结果会不一致。
 *
 * @since 7.2.0
 */
public final class GunpackConversionSupport {
    private GunpackConversionSupport() {
    }

    /**
     * 获取转换用的注册表。
     * 枪包扫描发生在注册表可用之前，此时只能退回内置注册表。
     * 内置注册表足以校验物品 ID（含模组物品），数据包注册表（如附魔）不可用。
     */
    public static HolderLookup.Provider registries() {
        var current = ContextUtil.getRegistryAccessSafely();
        return current != null ? current : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    /**
     * 读取转换规则：优先使用用户在 {@code config/tacz/gunpack-conversion-rules.json} 中的覆盖，
     * 读取失败或不支持该文件时退回 TaCZ 内置的默认规则。
     */
    public static PackConversionRules conversionRules() {
        var override = FMLPaths.CONFIGDIR.get().resolve("tacz/gunpack-conversion-rules.json");
        try {
            return PackConversionRules.load(override);
        } catch (Exception ex) {
            GunsmithLib.LOGGER.warn("Failed to load gunpack conversion rules from {}, using bundled defaults", override, ex);
            return PackConversionRules.defaults();
        }
    }
}
