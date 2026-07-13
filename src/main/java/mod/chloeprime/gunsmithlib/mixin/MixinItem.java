package mod.chloeprime.gunsmithlib.mixin;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks.ModifyDisplayPropertyHook;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(Item.class)
public class MixinItem {

    // 耐久条 modify display property

    @ModifyReturnValue(method = "isBarVisible", at = @At("RETURN"))
    private static boolean modifyIsBarVisible(boolean original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return ModifyDisplayPropertyHook.modifyIsBarVisible(original, stack);
    }

    @ModifyReturnValue(method = "getBarWidth", at = @At("RETURN"))
    private static int modifyBarWidth(int original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return ModifyDisplayPropertyHook.modifyBarWidth(original, stack);
    }

    @ModifyReturnValue(method = "getBarColor", at = @At("RETURN"))
    private static int modifyBarColor(int original, ItemStack stack) {
        if (EffectiveSide.get().isServer()) {
            return original;
        }
        return ModifyDisplayPropertyHook.modifyBarColor(original, stack);
    }
}
