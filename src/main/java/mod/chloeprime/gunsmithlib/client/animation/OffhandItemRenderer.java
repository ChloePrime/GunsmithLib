package mod.chloeprime.gunsmithlib.client.animation;

import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class OffhandItemRenderer extends VanillaItemRenderer {
    public OffhandItemRenderer(BedrockGunModelExtension model, HumanoidArm arm) {
        super(model, arm);
    }

    @Override
    public ItemStack getItem() {
        return model.getCurrentHolder().map(LivingEntity::getOffhandItem).orElse(ItemStack.EMPTY);
    }
}
