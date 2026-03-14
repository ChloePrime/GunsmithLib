package mod.chloeprime.gunsmithlib.api.client;

import mod.chloeprime.gunsmithlib.api.client.scripting_v2.GunsmithLibAnimationStateMachineScriptExtension;
import mod.chloeprime.gunsmithlib.api.common.CommonScriptingExtension;

/**
 * @since 3.7.0
 */
public interface GunAnimationStateContextExtension extends CommonScriptingExtension {
    @Override
    GunsmithLibAnimationStateMachineScriptExtension gunsmithlib_extension();
}
