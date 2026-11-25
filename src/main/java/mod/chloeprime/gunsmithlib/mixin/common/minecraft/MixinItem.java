package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks.ModifyDisplayPropertyHook;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.util.thread.EffectiveSide;
import net.neoforged.neoforge.common.extensions.IItemExtension;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.awt.*;

@Mixin(Item.class)
public abstract class MixinItem implements IItemExtension {

    // 耐久系统

    /**
     * @since 7.2.0
     */
    @ModifyReturnValue(method = "getBarColor", remap = false, at = @At("RETURN"))
    private int barColorFromDurabilitySystem(int original, ItemStack stack) {
        var durData = DurabilityData.of(stack).orElse(null);
        if (durData == null) {
            return original;
        }
        return durData.getBarColor(() -> {
            var damage = stack.getDamageValue();
            var maxDmg = stack.getMaxDamage();
            return 1 - damage / (float) maxDmg;
        }).map(Color::getRGB).orElse(original);
    }

    /**
     * 修理材料
     */
    @ModifyReturnValue(method = "isValidRepairItem", at = @At("RETURN"))
    public boolean isValidRepairItem(boolean original, ItemStack weapon, ItemStack material) {
        // 这个方法调用频率不高，不必太关系性能 :)
        return original || DurabilityData.of(weapon)
                .flatMap(DurabilityData::getRepairMaterial)
                .filter(repairItem -> repairItem.test(material))
                .isPresent();
    }

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
