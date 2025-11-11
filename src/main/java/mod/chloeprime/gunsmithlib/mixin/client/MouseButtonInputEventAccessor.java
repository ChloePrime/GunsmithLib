package mod.chloeprime.gunsmithlib.mixin.client;

import net.neoforged.neoforge.client.event.InputEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = InputEvent.MouseButton.class, remap = false)
public interface MouseButtonInputEventAccessor {
    @Accessor @Mutable void setAction(int glfwAction);
}
