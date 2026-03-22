package mod.chloeprime.gunsmithlib.client;

import mod.chloeprime.gunsmithlib.api.client.RenderGunTooltipTextEvent;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;

public class ClientInternalEvents {
    public static class RenderGunTooltipTextPre extends InternalEvent<RenderGunTooltipTextEvent> {
        public RenderGunTooltipTextPre(RenderGunTooltipTextEvent impl) {
            super(impl);
        }
    }
}
