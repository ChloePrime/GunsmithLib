package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;
import mod.chloeprime.gunsmithlib.client.AbstractGunAnimationStateContextExtension;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import org.jetbrains.annotations.ApiStatus;

/**
 * 示例用法：{@code api:gunsmithlib_extension():play_overheat_sound()}
 *
 * @see GunScriptAPIExtension#gunsmithlib_extension()
 * @since 5.6.0
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GunsmithLibAnimationStateMachineScriptExtension extends GunsmithLibCommonScriptExtension {

    // 下面是内部 API

    private final GunAnimationStateContext api;
    private final AbstractGunAnimationStateContextExtension v1;

    @ApiStatus.Internal
    public GunsmithLibAnimationStateMachineScriptExtension(GunAnimationStateContext ctx) {
        super((AbstractCommonScriptingExtension) ctx);
        this.api = ctx;
        this.v1 = (AbstractGunAnimationStateContextExtension) ctx;
    }
}
