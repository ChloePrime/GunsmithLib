package mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;

/**
 * @since 3.4.0
 */
@SuppressWarnings("unused")
public class GunsmithLibAttachmentDataExtension extends GunsmithLibSharedDataExtension {
    /**
     * 枪盾数据
     *
     * @since 3.4.0
     */
    private ShieldData shield;

    public ShieldData getShieldData() {
        return shield;
    }

    private FireControlData fire_control;

    public FireControlData getFireControlData(){
        return fire_control;
    }
}
