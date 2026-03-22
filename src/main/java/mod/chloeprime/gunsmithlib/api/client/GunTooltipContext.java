package mod.chloeprime.gunsmithlib.api.client;

import com.tacz.guns.client.tooltip.ClientGunTooltip;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record GunTooltipContext(
        @Nonnull ClientGunTooltip instance,
        @Nullable GunInfo gunInfo
) {
}
