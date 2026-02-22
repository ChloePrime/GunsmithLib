package mod.chloeprime.gunsmithlib.mixin.dfu;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.tacz.guns.resource.GunPackLoader;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.StartupConfig;
import mod.chloeprime.gunsmithlib.common.dfu.GunpackUpgraderWrapper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import java.io.IOException;

@Mixin(GunPackLoader.class)
public class MixinGunPackLoader {
    @ModifyReturnValue(method = "fromDirPath", at = @At("RETURN"))
    private static GunPackLoader.GunPack autoUpgradeGunpackDirs(GunPackLoader.GunPack original) {
        return gunsmithlib$upgrade(original);
    }

    @ModifyReturnValue(method = "fromZipPath", at = @At("RETURN"))
    private static GunPackLoader.GunPack autoUpgradeGunpackZips(GunPackLoader.GunPack original) {
        return gunsmithlib$upgrade(original);
    }

    @Unique
    private static GunPackLoader.GunPack gunsmithlib$upgrade(GunPackLoader.GunPack original) {
        if (StartupConfig.DISABLE_PACK_UPGRADER.get()) {
            return original;
        }
        if (original == null) {
            return null;
        }
        try {
            return new GunPackLoader.GunPack(GunpackUpgraderWrapper.upgrade(original.path()), original.name());
        } catch (IOException ex) {
            GunsmithLib.LOGGER.error("Failed to upgrade gunpack {}", original.name(), ex);
            return null;
        }
    }
}
