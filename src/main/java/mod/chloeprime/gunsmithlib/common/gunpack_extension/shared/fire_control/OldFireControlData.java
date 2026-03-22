package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control;

import com.google.gson.annotations.SerializedName;

@Deprecated
@SuppressWarnings("DeprecatedIsStillUsed")
public record OldFireControlData(
        @SerializedName("aim_cone_angle") float aimConeAngle
) {
}
