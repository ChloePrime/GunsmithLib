package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;

/**
 * @since 4.5.0
 */
public class GunsmithLibGunDisplayExtension {
    /**
     * 隐藏热量条 hud
     *
     * @since 4.5.0
     */
    @GunpackProperty
    private boolean hide_heat_bar_overlay;

    /**
     * 当前剩余弹药显示模式
     * default: 默认，如果为充电武器则显示电池，否则显示计数器
     * counter: 显示计数器，无论是否为充电武器
     * battery: 显示电池，无论是否为充电武器
     *
     * @since 4.8.0
     */
    @GunpackProperty
    private CurrentAmmoDisplayType current_ammo_display_type = CurrentAmmoDisplayType.DEFAULT;

    public boolean hideHeatBarOverlay() {
        return hide_heat_bar_overlay;
    }

    public CurrentAmmoDisplayType getCurrentAmmoDisplayType() {
        return current_ammo_display_type;
    }
}
