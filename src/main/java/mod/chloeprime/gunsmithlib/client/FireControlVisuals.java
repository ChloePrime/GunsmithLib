package mod.chloeprime.gunsmithlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

@EventBusSubscriber(Dist.CLIENT)
public class FireControlVisuals {
    private static @Nullable TargetSearcher.SearchResult aiming;
    private static long lastAimCalculationTime;
    private static float lastAimCalculationPartialTicks;
    private static final Minecraft MC = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        refreshCachedAimingEntity(event.getPartialTick());
        if (aiming == null || aiming.entity() != event.getEntity()) {
            return;
        }
        render(event.getEntity(), aiming.pos(),
                MC.getEntityRenderDispatcher(), event.getRenderer().getFont(),
                event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
    }

    private static void refreshCachedAimingEntity(float partialTick) {
        var level = MC.level;
        if (level == null) {
            return;
        }

        long now = level.getGameTime();
        if (lastAimCalculationTime != now || lastAimCalculationPartialTicks != partialTick) {
            lastAimCalculationTime = now;
            lastAimCalculationPartialTicks = partialTick;
            if (!(MC.getCameraEntity() instanceof LivingEntity player)) {
                aiming = null;
                return;
            }
            aiming = Gunsmith.getGunInfo(player.getMainHandItem())
                    // 用PartialTick计算会有bug，传入1反而更稳定
                    .flatMap(gun -> TargetSearcher.search(player, gun, 1))
                    .orElse(null);
        }
    }

    private static final Component AIMING_GLYPH = Component.literal("[+]").withStyle(ChatFormatting.RED);

    private static void render(Entity pEntity, Vec3 aimPos,
                               EntityRenderDispatcher dispatcher, Font font,
                               PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var pDisplayName = AIMING_GLYPH;
        var offset = aimPos.subtract(pEntity.position());
        int i = 0;
        poseStack.pushPose();
        {
            poseStack.translate(offset.x(), offset.y(), offset.z());
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(-0.05F, -0.05F, 0.05F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f2 = (float) (-font.width(pDisplayName) / 2);
            font.drawInBatch(pDisplayName, f2, (float) i, 0xFFFF_FFFF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, packedLight);
        }
        poseStack.popPose();
    }
}
