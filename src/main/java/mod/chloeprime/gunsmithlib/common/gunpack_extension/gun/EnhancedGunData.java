package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.arcana_check.ArcanaExtras;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.OldFireControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlData;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public interface EnhancedGunData {
    Optional<GunsmithLibGunDataExtension> gunsmith$getGunsmithLibExtension();

    Optional<ArcanaExtras> gunsmith$getArcanaExtras();

    /**
     * TaCZ Fire Control Extension 模组的火控数据。
     * @deprecated 已弃用，留着它是为了兼容旧版枪包。新适配武器请使用 {@link FireControlData}
     */
    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    Optional<OldFireControlData> gunsmith$getOldFireControlSystemData();
}
