package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;

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

    /**
     * 当前武器在弹种轮盘菜单上显示的名称。
     * 支持填入本地化键。
     *
     * @since 4.12.0
     */
    @GunpackProperty
    private @Nullable String variant_name;

    /**
     * 当前武器在弹种轮盘菜单上显示的图标。
     * 需要包含 textures/ 文件夹和 .png 后缀。
     * 示例："gunsmithlib:textures/dreadnought_cat.png"
     *
     * @since 4.12.0
     */
    @GunpackProperty
    private @Nullable ResourceLocation variant_icon;

    public boolean hideHeatBarOverlay() {
        return hide_heat_bar_overlay;
    }

    public CurrentAmmoDisplayType getCurrentAmmoDisplayType() {
        return current_ammo_display_type;
    }

    public @Nullable String getVariantName() {
        return variant_name;
    }

    public @Nullable ResourceLocation getVariantIcon() {
        return variant_icon;
    }
}
