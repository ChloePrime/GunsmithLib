package mod.chloeprime.gunsmithlib.common.dfu;

import cn.chloeprime.commons.ContextUtil;
import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.tacz.guns.resource.GunPackOverlayResolver;
import com.tacz.guns.resource.PackMeta;
import com.tacz.guns.resource.convert.PackConversionRules;
import com.tacz.guns.resource.convert.UniversalPackConverter;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.RegistryAccess;
import net.minecraft.core.registries.BuiltInRegistries;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.file.PathUtils;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipFile;

/**
 * 使用 TaCZ 1.21.1 官方内置的通用枪包升级器（{@link UniversalPackConverter}，即 {@code /tacz universal} 背后的实现）
 * 把 1.20.1 格式的枪包升级成同时兼容 1.20.1 与 1.21.1 的枪包。
 *
 * @since 7.0.0
 */
public final class GunpackUpgraderWrapper {
    /**
     * 压缩包缓存版本号。升级器实现变化时需要递增，避免复用旧实现产生的缓存。
     */
    public static final String VERSION = "v4";

    public static final Path ROOT_TEMP_DIR = FMLPaths.GAMEDIR.relative().resolve("." + GunsmithLib.MOD_ID);

    /**
     * 压缩包枪包的升级缓存。文件夹枪包不参与缓存，每次启动/重载都会重新升级。
     */
    public static final Path UPGRADE_CACHE_DIR = ROOT_TEMP_DIR.resolve("upgrade_cache").resolve(VERSION);

    /**
     * 枪包升级过程中的临时输出目录。转换完成后只有缓存文件会被移动到 {@link #UPGRADE_CACHE_DIR}。
     */
    private static final Path UPGRADE_TEMP_DIR = ROOT_TEMP_DIR.resolve("upgrade_temp");

    private static final String DEFAULT_PACK_NAME = "tacz_default_gun";
    private static final String SOURCE_VERSION = "1.20.1";
    private static final String TARGET_VERSION = SharedConstants.getCurrentVersion().getName();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();
    private static final Object LOCK = new Object();
    private static final AtomicInteger FAIL_COUNT = new AtomicInteger();

    static {
        cleanTempFolder();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            int fails = FAIL_COUNT.get();
            if (fails == 0) {
                cleanTempFolder();
            } else {
                System.out.printf("[GunsmithLib] Upgrade temp not cleared because there are %d error(s)%n", fails);
            }
        }));
    }

    private GunpackUpgraderWrapper() {
    }

    /**
     * 升级一个 1.20.1 格式的枪包。
     * <p>
     * 压缩包按内容 SHA-256 缓存升级结果，缓存文件名为该指纹；文件夹不计算指纹、不读写缓存，每次调用都会重新升级。
     *
     * @return 升级后的枪包路径；如果枪包不需要或无法升级，则返回原路径
     * @since 7.0.0
     */
    public static Path upgrade(Path source) throws IOException {
        if (Files.isDirectory(source)) {
            return upgradeDirectory(source);
        }
        var stem = MoreFiles.getNameWithoutExtension(source);
        if (DEFAULT_PACK_NAME.equals(stem)) {
            return source;
        }
        var meta = readMeta(source);
        if (meta == null || isAlreadyUpdated(meta)) {
            return source;
        }
        var hash = fingerprint(source);
        var cached = UPGRADE_CACHE_DIR.resolve(hash);
        if (Files.isRegularFile(cached)) {
            GunsmithLib.LOGGER.info("Found upgraded gunpack from cache for {}", source);
            return cached;
        }

        synchronized (LOCK) {
            if (Files.isRegularFile(cached)) {
                return cached;
            }
            Files.createDirectories(UPGRADE_TEMP_DIR);
            var outputDir = Files.createTempDirectory(UPGRADE_TEMP_DIR, "zip-");
            GunsmithLib.LOGGER.info("Upgrading gunpack {} with TaCZ's built-in universal converter", source);
            var result = newConverter().convert(source, outputDir, true);
            var upgraded = acceptResult(source, result);
            if (upgraded.equals(source)) {
                return source;
            }
            Files.createDirectories(UPGRADE_CACHE_DIR);
            try {
                Files.move(upgraded, cached);
            } catch (FileAlreadyExistsException ex) {
                if (!Files.isRegularFile(cached)) {
                    throw ex;
                }
            }
            GunsmithLib.LOGGER.info("Cached upgraded gunpack at {}", cached);
            try {
                PathUtils.deleteDirectory(outputDir);
            } catch (IOException ex) {
                GunsmithLib.LOGGER.info("Failed to clean gunpack conversion cache at {}", outputDir, ex);
            }
            return cached;
        }
    }

    /**
     * 文件夹枪包不缓存：每次都在新的临时目录里重新升级，避免开发时改动枪包后仍加载旧产物。
     */
    private static Path upgradeDirectory(Path source) throws IOException {
        var stem = source.getFileName().toString();
        if (DEFAULT_PACK_NAME.equals(stem)) {
            return source;
        }
        var meta = readMeta(source);
        if (meta == null || isAlreadyUpdated(meta)) {
            return source;
        }

        synchronized (LOCK) {
            Files.createDirectories(UPGRADE_TEMP_DIR);
            var outputDir = Files.createTempDirectory(UPGRADE_TEMP_DIR, "pack-%s-".formatted(source.getFileName()));
            GunsmithLib.LOGGER.info("Upgrading gunpack folder {} with TaCZ's built-in universal converter", source);
            var result = newConverter().convert(source, outputDir, true);
            return acceptResult(source, result);
        }
    }

    private static Path acceptResult(Path source, UniversalPackConverter.Result result) {
        var report = result.report();
        if (report.hasErrors() || result.packPath() == null) {
            logFailure(source, result);
            return source;
        }
        if (report.isPartial()) {
            GunsmithLib.LOGGER.warn("Gunpack {} was upgraded partially: {} recipe(s) skipped. Report: {}",
                    source, report.skippedRecipes, result.reportPath());
        } else {
            GunsmithLib.LOGGER.info("Gunpack {} upgraded: {} overlay file(s). Report: {}",
                    source, report.overlayFiles, result.reportPath());
        }
        return result.packPath();
    }

    /**
     * 读取枪包针对当前游戏版本选中的 overlay 目录列表。
     * 升级后的枪包把 1.21.1 的资源放在 {@link UniversalPackConverter#OVERLAY} 里，
     * 因此不能沿用原枪包的 overlay 列表，必须重新读取。
     *
     * @since 7.0.0
     */
    public static List<String> resolveOverlays(Path pack) throws IOException {
        var meta = readMeta(pack);
        if (meta == null) {
            return List.of();
        }
        return GunPackOverlayResolver.resolve(GSON.fromJson(meta, PackMeta.class), TARGET_VERSION);
    }

    /**
     * 读取枪包的命名空间。用于原枪包被 TaCZ 的依赖检查拒绝、但仍成功升级的情况。
     *
     * @since 7.0.0
     */
    public static String readName(Path pack) throws IOException {
        var meta = readMeta(pack);
        if (meta == null || !meta.has("namespace") || meta.get("namespace").isJsonNull()) {
            throw new IOException("Missing gun pack namespace: " + pack);
        }
        return meta.get("namespace").getAsString();
    }

    private static UniversalPackConverter newConverter() {
        var installedVersions = new HashMap<String, String>();
        ModList.get().getMods().forEach(mod -> installedVersions.put(mod.getModId(), mod.getVersion().toString()));
        return new UniversalPackConverter(registries(), installedVersions, rules());
    }

    private static HolderLookup.Provider registries() {
        // 枪包扫描发生在注册表可用之前，此时只能退回内置注册表。
        // 内置注册表足以校验物品 ID（含模组物品），数据包注册表（如附魔）不可用。
        var current = ContextUtil.getRegistryAccessSafely();
        return current != null ? current : RegistryAccess.fromRegistryOfRegistries(BuiltInRegistries.REGISTRY);
    }

    private static PackConversionRules rules() {
        var override = FMLPaths.CONFIGDIR.get().resolve("tacz/gunpack-conversion-rules.json");
        try {
            return PackConversionRules.load(override);
        } catch (Exception ex) {
            GunsmithLib.LOGGER.warn("Failed to load gunpack conversion rules from {}, using bundled defaults", override, ex);
            return PackConversionRules.defaults();
        }
    }

    private static boolean isAlreadyUpdated(JsonObject meta) {
        var overlays = meta.getAsJsonObject("overlays");
        if (overlays != null && overlays.has("entries")) {
            for (var element : overlays.getAsJsonArray("entries")) {
                if (!element.isJsonObject()) {
                    continue;
                }
                var entry = element.getAsJsonObject();
                if (!entry.has("directory") || entry.get("directory").isJsonNull()) {
                    continue;
                }
                if (UniversalPackConverter.OVERLAY.equals(entry.get("directory").getAsString())) {
                    return true;
                }
                if (entry.has("minecraft") && !entry.get("minecraft").isJsonNull()
                        && GunPackOverlayResolver.matches(entry.get("minecraft").getAsString(), TARGET_VERSION)) {
                    return true;
                }
            }
        }
        // 官方升级器只接受声明支持（或未声明）1.20.1 的枪包。
        var dependencies = meta.getAsJsonObject("dependencies");
        if (dependencies != null && dependencies.has("minecraft")) {
            return !GunPackOverlayResolver.matches(dependencies.get("minecraft").getAsString(), SOURCE_VERSION);
        }
        return false;
    }

    private static @Nullable JsonObject readMeta(Path pack) throws IOException {
        if (Files.isDirectory(pack)) {
            var metaFile = pack.resolve("gunpack.meta.json");
            if (!Files.isRegularFile(metaFile)) {
                return null;
            }
            try (var reader = Files.newBufferedReader(metaFile, StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
        try (var zip = new ZipFile(pack.toFile())) {
            var entry = zip.getEntry("gunpack.meta.json");
            if (entry == null) {
                return null;
            }
            try (var reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                return JsonParser.parseReader(reader).getAsJsonObject();
            }
        }
    }

    /**
     * 计算压缩包缓存键。文件夹枪包每次都会重新升级，不参与缓存。
     */
    @SuppressWarnings("UnstableApiUsage")
    private static String fingerprint(Path source) throws IOException {
        var hasher = Hashing.sha256().newHasher();
        try (var stream = Files.newInputStream(source)) {
            ByteStreams.copy(stream, Funnels.asOutputStream(hasher));
        }
        return hasher.hash().toString();
    }

    private static void logFailure(Path source, UniversalPackConverter.Result result) {
        var report = result.report();
        GunsmithLib.LOGGER.error("Failed to upgrade gunpack {}. Report: {}", source, result.reportPath());
        report.diagnostics.stream()
                .filter(diagnostic -> diagnostic.severity().equals("error"))
                .limit(16)
                .forEach(diagnostic -> GunsmithLib.LOGGER.error("  {} {} [{}] {}",
                        diagnostic.file(), diagnostic.path(), diagnostic.code(), diagnostic.message()));
        FAIL_COUNT.getAndIncrement();
    }

    private static void cleanTempFolder() {
        synchronized (LOCK) {
            if (Files.isDirectory(UPGRADE_TEMP_DIR)) {
                try {
                    PathUtils.deleteDirectory(UPGRADE_TEMP_DIR);
                } catch (IOException ex) {
                    System.out.println("[ERROR] Failed to delete gunpack upgrading temp files");
                }
            }
        }
    }
}
