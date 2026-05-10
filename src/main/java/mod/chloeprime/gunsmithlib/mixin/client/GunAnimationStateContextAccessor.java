package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(value = GunAnimationStateContext.class, remap = false)
public interface GunAnimationStateContextAccessor {
    @Accessor ItemStack getCurrentGunItem();
    @Accessor IGun getIGun();
}
