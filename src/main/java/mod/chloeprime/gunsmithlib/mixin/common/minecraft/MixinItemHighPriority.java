package mod.chloeprime.gunsmithlib.mixin.common.minecraft;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilitySystem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Dynamic;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

@Mixin(value = Item.class, priority = 800)
public class MixinItemHighPriority {

    // 耐久系统

    /**
     * @since 7.2.0
     */
    @Dynamic
    @ModifyReturnValue(method = "isDamageable", remap = false, at = @At("RETURN"))
    private boolean gunIsDamageableIfHasDurabilityConfig(boolean original, ItemStack stack) {
        return DurabilitySystem.hasDurabilityCapability(stack)
                // 默认实现缺乏 Stack 上下文，为了让耐久内容能附魔经验修补所以对所有枪械和配件来说，original 皆为 true，
                // 所以这里必须忽视 original
                ? DurabilityData.of(stack).isPresent()
                : original;
    }
}
