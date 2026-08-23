package mod.chloeprime.gunsmithlib.mixin.client.tacz;

import com.tacz.guns.client.gui.overlay.GunHudOverlay;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GunHudOverlay.class, remap = false)
public interface GunHudOverlayAccessor {
    @Accessor static int getCacheInventoryAmmoCount() {
        throw new AbstractMethodError();
    }
}
