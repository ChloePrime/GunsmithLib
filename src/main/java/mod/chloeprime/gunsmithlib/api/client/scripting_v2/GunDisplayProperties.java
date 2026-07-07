package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import com.tacz.guns.api.ValueModifiableAtRuntime;
import mod.chloeprime.gunsmithlib.GunsmithLib;

public class GunDisplayProperties {
    /**
     * 这个属性在渲染右下角 HUD 和枪械 Tooltip 时都会触发。
     */
    @MaybeInanimatable
    @ValueModifiableAtRuntime(int.class)
    public static final String AMMO_AMOUNT = "%s_displayed_ammo_amount".formatted(GunsmithLib.MOD_ID);
}
