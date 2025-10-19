package mod.chloeprime.gunsmithlib.common.gunpack_extension.attachment;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.GunsmithLibSharedDataExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.shield.ShieldData;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;

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
    @GunpackProperty
    private ShieldData shield;

    public ShieldData getShieldData() {
        return shield;
    }
}
