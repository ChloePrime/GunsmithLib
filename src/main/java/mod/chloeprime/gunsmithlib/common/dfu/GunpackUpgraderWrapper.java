package mod.chloeprime.gunsmithlib.common.dfu;

import com.google.common.hash.Funnels;
import com.google.common.hash.Hashing;
import com.google.common.io.ByteStreams;
import com.google.common.io.MoreFiles;
import me.muksc.taczpackupgrader.Upgrader;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.neoforged.fml.loading.FMLPaths;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.file.PathUtils;

import java.io.IOException;
import java.nio.file.*;

public class GunpackUpgraderWrapper {
    public static final String VERSION = "v1";
    public static final Path ROOT_TEMP_DIR = FMLPaths.GAMEDIR.relative().resolve("." + GunsmithLib.MOD_ID);
    public static final Path WORKING_DIR = ROOT_TEMP_DIR.resolve("temp");
    public static final Path UPGRADE_CACHE = ROOT_TEMP_DIR.resolve("upgrade_cache").resolve(VERSION);

    @SuppressWarnings("UnstableApiUsage")
    public static Path upgrade(Path pack1201) throws IOException {
        if (Files.isDirectory(pack1201)) {
            GunsmithLib.LOGGER.info("Upgrading gunpack folder at {}", pack1201);
            return upgrade(pack1201, UPGRADE_CACHE.resolve(pack1201.getFileName().toString() + ".zip"));
        }
        String hash;
        try (var stream = Files.newInputStream(pack1201)) {
            var hasher = Hashing.sha256().newHasher();
            ByteStreams.copy(stream, Funnels.asOutputStream(hasher));
            hash = hasher.hash().toString();
        }
        Path cachedUpgradeResult = UPGRADE_CACHE.resolve(hash + ".zip");
        if (Files.isRegularFile(cachedUpgradeResult)) {
            GunsmithLib.LOGGER.info("Found upgraded gunpack from cache for {}", pack1201);
            return cachedUpgradeResult;
        } else {
            GunsmithLib.LOGGER.info("Upgrading gunpack file at {}", pack1201);
            return upgrade(pack1201, cachedUpgradeResult);
        }
    }

    public static synchronized Path upgrade(Path pack1201, Path target) throws IOException {
        FileUtils.forceMkdir(WORKING_DIR.toFile());
        FileUtils.forceMkdir(UPGRADE_CACHE.toFile());

        try {
            String name = Files.isDirectory(pack1201) ? pack1201.getFileName().toString() : MoreFiles.getNameWithoutExtension(pack1201);
            if ("tacz_default_gun".equals(name)) {
                return pack1201;
            } else {
                Path unzipped1201 = Upgrader.INSTANCE.initializePack(pack1201, WORKING_DIR);
                try (var subdirectories = Files.list(unzipped1201.resolve("data"))) {
                    subdirectories.filter(Files::isDirectory).forEach(subdirectory -> {
                        Upgrader.INSTANCE.upgradeBlockDatas(subdirectory);
                        Upgrader.INSTANCE.upgradeRecipes(subdirectory);
                    });
                }
                Upgrader.INSTANCE.zip(unzipped1201, target, 1024);
                return target;
            }
        } finally {
            try {
                PathUtils.deleteDirectory(WORKING_DIR);
            } catch (IOException ex) {
                GunsmithLib.LOGGER.error("Failed to cleanup gunpack upgrader's temp folder", ex);
            }
        }
    }
}
