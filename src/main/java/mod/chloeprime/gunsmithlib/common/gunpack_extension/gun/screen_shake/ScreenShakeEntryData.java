package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake;

import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;

/**
 * @since 7.0
 */
public class ScreenShakeEntryData {
    /**
     * 基础震动力度
     * 受上限影响。
     */
    @GunpackProperty
    private double base_power = 20;

    /**
     * 震动范围
     */
    @GunpackProperty
    private double range = 32;

    /**
     * 震动方向 Yaw 分量
     */
    @GunpackProperty
    private double magnitude_yaw = 0.5;

    /**
     * 震动方向 Pitch 分量
     */
    @GunpackProperty
    private double magnitude_pitch = 1;

    /**
     * 震动方向 Roll 分量
     */
    @GunpackProperty
    private double magnitude_roll = 0.5;

    /**
     * 震动强度，会改变振幅上限。
     */
    @GunpackProperty
    private double intensity = 1.3;

    /**
     * 震动频率
     */
    @GunpackProperty
    private double restitution = 0.08;

    /**
     * 阻力大小
     */
    @GunpackProperty
    private double drag = 0.3;

    /**
     * 震动抖动方向 X 分量
     */
    @GunpackProperty
    private double translation_magnitude_x = 0;

    /**
     * 震动抖动方向 Y 分量
     */
    @GunpackProperty
    private double translation_magnitude_y = 0;

    /**
     * 震动抖动方向 Z 分量
     */
    @GunpackProperty
    private double translation_magnitude_z = 0;

    // ↓ For Java ↓


    public double getBasePower() {
        return base_power;
    }

    public double getRange() {
        return range;
    }

    public double getYawMagnitude() {
        return magnitude_yaw;
    }

    public double getPitchMagnitude() {
        return magnitude_pitch;
    }

    public double getRollMagnitude() {
        return magnitude_roll;
    }

    public double getIntensity() {
        return intensity;
    }

    public double getTranslationMagnitudeX() {
        return translation_magnitude_x;
    }

    public double getTranslationMagnitudeY() {
        return translation_magnitude_y;
    }

    public double getTranslationMagnitudeZ() {
        return translation_magnitude_z;
    }

    public double getRestitution() {
        return restitution;
    }

    public double getDrag() {
        return drag;
    }
}
