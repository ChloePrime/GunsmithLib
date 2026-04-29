package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import com.google.gson.annotations.SerializedName;

/**
 * @since 5.11.0
 */
public enum FragDistribution {
    @SerializedName("random")  RANDOM,
    @SerializedName("uniform") UNIFORM;

    public static int computeUniformModeCount(int dataCount, boolean isHalfBall) {
        return computeUniformModeCount((double) dataCount, isHalfBall);
    }

    public static int computeUniformModeCount(double dataCount, boolean isHalfBall) {
        return Math.round((float) Math.sqrt(dataCount / (isHalfBall ? 1 : 2)));
    }
}
