package mod.chloeprime.gunsmithlib.client.animation;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.client.model.IFunctionalRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

public abstract class VanillaItemRenderer implements IFunctionalRenderer {
    protected final BedrockGunModelExtension model;
    protected final HumanoidArm arm;
    protected static final Minecraft MC = Minecraft.getInstance();

    public VanillaItemRenderer(BedrockGunModelExtension model, HumanoidArm arm) {
        this.model = model;
        this.arm = arm;
    }

    @Override
    public void render(PoseStack poseStack0, VertexConsumer vertexBuffer, ItemDisplayContext transformType, int light, int overlay) {
        var holder = model.getCurrentHolder().orElse(null);
        if (holder == null) {
            return;
        }
        var item = getItem();
        var arm = getTransformType();

        var pose = new PoseStack();
        var tran = switch (arm) {
            case LEFT -> ItemDisplayContext.FIRST_PERSON_LEFT_HAND;
            case RIGHT -> ItemDisplayContext.FIRST_PERSON_RIGHT_HAND;
        };
        var isLeftHand = arm == HumanoidArm.LEFT;

        poseStack0.pushPose();
        {
            poseStack0.mulPose(Axis.ZP.rotationDegrees(180f));
            pose.last().pose().mul(poseStack0.last().pose());
            pose.last().normal().mul(poseStack0.last().normal());
        }
        poseStack0.popPose();

        model.model.delegateRender(((poseStack1, vertexBuffer1, transformType1, light1, overlay1) -> {
            poseStack1.pushPose();
            {
                poseStack1.setIdentity();
                poseStack1.last().pose().mul(pose.last().pose());
                poseStack1.last().normal().mul(pose.last().normal());
                var buffer = Minecraft.getInstance().renderBuffers().bufferSource();
                MC.gameRenderer.itemInHandRenderer.renderItem(holder, item, tran, isLeftHand, poseStack1, buffer, light);
                buffer.endBatch();
            }
            poseStack1.popPose();
        }));
    }

    public abstract ItemStack getItem();

    public HumanoidArm getTransformType() {
        return this.arm;
    }
}
