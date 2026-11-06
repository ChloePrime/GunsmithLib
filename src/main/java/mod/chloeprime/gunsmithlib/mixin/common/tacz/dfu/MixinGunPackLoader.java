package mod.chloeprime.gunsmithlib.mixin.common.tacz.dfu;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.resource.GunPackLoader;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.StartupConfig;
import mod.chloeprime.gunsmithlib.common.dfu.GunpackUpgraderWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.nio.file.Path;

@Mixin(GunPackLoader.class)
public class MixinGunPackLoader {
    @ModifyReturnValue(method = "fromDirPath", at = @At("RETURN"))
    private static GunPackLoader.GunPack autoUpgradeGunpackDirs(GunPackLoader.GunPack original, Path path) {
        return gunsmithlib$upgrade(path, original);
    }

    @ModifyReturnValue(method = "fromZipPath", at = @At("RETURN"))
    private static GunPackLoader.GunPack autoUpgradeGunpackZips(GunPackLoader.GunPack original, Path path) {
        return gunsmithlib$upgrade(path, original);
    }

    @Unique
    private static GunPackLoader.GunPack gunsmithlib$upgrade(Path path, GunPackLoader.GunPack original) {
        if (StartupConfig.DISABLE_PACK_UPGRADER.get()) {
            return original;
        }
        try {
            var upgraded = GunpackUpgraderWrapper.upgrade(path);
            if (upgraded.equals(path)) {
                return original;
            }
            // 升级器把 1.21.1 资源放在 overlay 里，必须使用升级后枪包的 overlay 列表。
            // original 为 null 说明原枪包被 TaCZ 的依赖检查拒绝（例如只声明支持 1.20.1），
            // 此时从升级后的枪包读取命名空间。
            var name = original != null ? original.name() : GunpackUpgraderWrapper.readName(upgraded);
            return new GunPackLoader.GunPack(upgraded, name, GunpackUpgraderWrapper.resolveOverlays(upgraded));
        } catch (Exception ex) {
            GunsmithLib.LOGGER.error("Failed to upgrade gunpack {}", path, ex);
            return original;
        }
    }
}
