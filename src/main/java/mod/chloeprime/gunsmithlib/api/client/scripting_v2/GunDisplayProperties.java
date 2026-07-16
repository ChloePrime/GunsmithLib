package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import com.tacz.guns.api.ValueModifiableAtRuntime;
import mod.chloeprime.gunsmithlib.GunsmithLib;

import java.awt.*;

/**
 * 可被状态脚本 modify_display_property 修改的属性列表。
 *
 * @since 6.2
 */
public final class GunDisplayProperties {
    /**
     * 这个属性在渲染右下角 HUD 和枪械 Tooltip 时都会触发。
     * <p>
     * {@code "gunsmithlib_displayed_ammo_amount"}
     */
    @MaybeInanimatable
    @ValueModifiableAtRuntime(Integer.class)
    public static final String AMMO_AMOUNT = "%s_displayed_ammo_amount".formatted(GunsmithLib.MOD_ID);

    /**
     * 耐久条长度。
     * 默认值为 {@code -1}，如果返回值小于 {@code 0} 则不修改耐久条。
     * <p>
     * {@code "gunsmithlib_durability_bar_length"}
     */
    @MaybeInanimatable
    @ValueModifiableAtRuntime(Float.class)
    public static final String DUR_BAR_LENGTH = "%s_durability_bar_length".formatted(GunsmithLib.MOD_ID);

    /**
     * 耐久条颜色。
     * <p>
     * {@code "gunsmithlib_durability_bar_color"}
     */
    @MaybeInanimatable
    @ValueModifiableAtRuntime(Color.class)
    public static final String DUR_BAR_COLOR = "%s_durability_bar_color".formatted(GunsmithLib.MOD_ID);

    private GunDisplayProperties() {
    }
}
