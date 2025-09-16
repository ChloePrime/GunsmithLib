package mod.chloeprime.gunsmithlib.client.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.tacz.guns.api.TimelessAPI;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.common.entity.MagicLaser;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.ParametersAreNonnullByDefault;

@EventBusSubscriber(Dist.CLIENT)
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MagicLaserRenderer<T extends MagicLaser> extends EntityRenderer<T> {
    public static final ResourceLocation TEXTURE_LOCATION = GunsmithLib.loc("textures/entity/magic_laser.png");
    public static final boolean SUPPORTS_PRECISE_MUZZLE_POS = false;

    private final MagicLaserModel<T> model;

    public MagicLaserRenderer(EntityRendererProvider.Context context) {
        super(context);
        model = new MagicLaserModel<>(context.bakeLayer(MagicLaserModel.LAYER_LOCATION), RenderType::entityTranslucentEmissive);
    }

    @Override
    public void render(T entity, float pEntityYaw, float pPartialTick, PoseStack poseStack, MultiBufferSource buffer, int pPackedLight) {
        super.render(entity, pEntityYaw, pPartialTick, poseStack, buffer, pPackedLight);
        poseStack.pushPose();

        var xRot = entity.getViewXRot(pPartialTick);
        var yRot = entity.getViewYRot(pPartialTick);
        var zRot = entity.getRoll();

        poseStack.mulPose(Axis.YP.rotationDegrees(-yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        model.setupAnim(zRot);

        var lifetime = (System.nanoTime() - entity.getLocalSpawnTime()) * MagicLaserUtils.NANO_TO_SECOND;
        var sizeFactor = Math.max(0, lifetime);
        var scale = (float) Math.pow(4, -20 * sizeFactor);
        var alpha = (float) Math.pow(4, -15 * sizeFactor);
        var length = entity.getLength();
        poseStack.scale(scale, scale, length);
        poseStack.translate(0, -1.5, 0);

        if (SUPPORTS_PRECISE_MUZZLE_POS) {
            var MC = Minecraft.getInstance();
            if (MC.options.getCameraType() == CameraType.FIRST_PERSON && MC.getCameraEntity() instanceof LivingEntity fpEntity && fpEntity == entity.getShooter()) {
                TimelessAPI
                        .getGunDisplay(fpEntity.getMainHandItem())
                        .ifPresent(gun -> MagicLaserUtils.getPreciseMuzzleOffset(gun, poseStack));
            }
        }

        VertexConsumer consumer = buffer.getBuffer(model.renderType(getTextureLocation(entity)));
        model.renderToBuffer(poseStack, consumer, pPackedLight, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.colorFromFloat(alpha, alpha, alpha, 1));

        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(T pEntity) {
        return TEXTURE_LOCATION;
    }


    @SubscribeEvent
    public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(MagicLaserModel.LAYER_LOCATION, MagicLaserModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GunsmithLib.EntityTypes.MAGIC_LASER.get(), MagicLaserRenderer::new);
    }
}