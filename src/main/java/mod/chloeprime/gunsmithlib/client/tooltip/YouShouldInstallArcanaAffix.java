package mod.chloeprime.gunsmithlib.client.tooltip;

import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check.ArcanaCheckSystem;
import net.minecraft.network.chat.Component;

public class YouShouldInstallArcanaAffix extends DescriptionalGunAffix.DescriptionalGunAffixBase {
    public YouShouldInstallArcanaAffix() {
        super(Component.translatable("gunsmithlib.affix.arcana_hint"));
    }

    @Override
    public boolean shouldShow(GunInfo gunInfo) {
        return ArcanaCheckSystem.shouldHintArcanaInstallation(gunInfo);
    }

    @Override
    public boolean shouldShowPrefix(GunInfo gunInfo) {
        return false;
    }
}
