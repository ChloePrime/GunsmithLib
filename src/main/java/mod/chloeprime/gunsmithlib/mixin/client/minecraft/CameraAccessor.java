package mod.chloeprime.gunsmithlib.mixin.client.minecraft;

import net.minecraft.client.Camera;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(Camera.class)
public interface CameraAccessor {
    @Invoker
    void invokeMove(double dz, double dy, double dx);
}
