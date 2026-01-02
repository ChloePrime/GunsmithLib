package mod.chloeprime.gunsmithlib.client.animation;

import com.tacz.guns.client.model.BedrockGunModel;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Optional;

public class BedrockGunModelExtension {
    public BedrockGunModelExtension(BedrockGunModel model) {
        this.model = model;
    }

    public final BedrockGunModel model;
    public final Deque<LivingEntity> holderContext = new ArrayDeque<>();
    public final OffhandItemRenderer offhandItemOnLeftSideRenderer = new OffhandItemRenderer(this, HumanoidArm.LEFT);
    public final OffhandItemRenderer offhandItemOnRightSideRenderer = new OffhandItemRenderer(this, HumanoidArm.RIGHT);

    public Optional<LivingEntity> getCurrentHolder() {
        return Optional.ofNullable(holderContext.peek());
    }
}
