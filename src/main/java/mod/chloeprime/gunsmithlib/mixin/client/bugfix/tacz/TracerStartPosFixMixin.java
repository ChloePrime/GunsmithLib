package mod.chloeprime.gunsmithlib.mixin.client.bugfix.tacz;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.mojang.blaze3d.vertex.PoseStack;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.model.bedrock.BedrockModel;
import com.tacz.guns.client.renderer.entity.EntityBulletRenderer;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithClientConfig;
import mod.chloeprime.gunsmithlib.client.bugfix.tacz.TracerStartPosFixStatics;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import org.joml.Vector3f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;

@Mixin(value = EntityBulletRenderer.class, remap = false)
public class TracerStartPosFixMixin {
    @ModifyExpressionValue(
            method = "/lambda\\$renderTracerAmmo\\$\\d+/",
            at = @At(value = "CONSTANT", args = "doubleValue=50.0"))
    private double fixTracerTooLowAtLowRange(
            double original,
            EntityKineticBullet bullet
    ) {
        double range = TracerStartPosFixStatics.CLIENT_RANGES.getOrDefault(bullet, original);
        return Math.min(range, original);
    }

    @ModifyVariable(
            method = "/lambda\\$renderTracerAmmo\\$\\d+/",
            name = "offset",
            at = @At(value = "INVOKE", ordinal = 0, target = "Lcom/tacz/guns/entity/EntityKineticBullet;getCameraXRot()F"),
            slice = @Slice(
                    from = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;getFirstPersonRenderOffset()Lorg/joml/Vector3f;"),
                    to = @At(value = "INVOKE", target = "Lcom/tacz/guns/entity/EntityKineticBullet;getTracerSizeOverride()F")))
    private Vector3f modifyOffsetToDownWhenAiming(
            Vector3f original,
            EntityKineticBullet bullet, PoseStack poseStack, float partialTicks, int packedLight, float[] tracerColor, BedrockModel model
    ) {
        if (!GunsmithClientConfig.IMPROVE_TRACER_START_POSITION_WHEN_AIMING.get()) {
            return original;
        }
        var shooter = Minecraft.getInstance().player;
        if (shooter == null || original == null || !Gunsmith.hasScope(shooter)) {
            return original;
        }
        var aimProgress = IClientPlayerGunOperator.fromLocalPlayer(shooter).getClientAimingProgress(partialTicks);
        return new Vector3f(original.x(), Mth.lerp(aimProgress, original.y(), -0.75F), original.z());
    }
}
