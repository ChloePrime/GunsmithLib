package mod.chloeprime.gunsmithlib.client;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.api.util.TargetSearcher;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content.InWorldGuiGraphicsImpl;
import mod.chloeprime.gunsmithlib.client.impl.scripting_v2.hooks.RenderHudHook;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLivingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.jetbrains.annotations.Nullable;
import org.joml.Matrix4f;

import javax.annotation.Nullable;
import java.awt.*;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public class FireControlVisuals {
    private static @Nullable TargetSearcher.SearchResult aiming;
    private static int version;
    private static long lastAimCalculationTime;
    private static float lastAimCalculationPartialTicks;
    private static long lastAimSelectTime;
    private static float lastAimSelectPartialTicks;
    private static final Minecraft MC = Minecraft.getInstance();

    @SubscribeEvent
    public static void onRenderLiving(RenderLivingEvent.Post<?, ?> event) {
        refreshCachedAimingEntity(event.getPartialTick());
        if (aiming == null || aiming.entity() != event.getEntity()) {
            return;
        }
        var pos = aiming.pos();
        if (version == 1) {
            renderV1(event.getEntity(), pos,
                    MC.getEntityRenderDispatcher(), event.getRenderer().getFont(),
                    event.getPoseStack(), event.getMultiBufferSource(), event.getPackedLight());
        }
    }

    @SubscribeEvent
    public static void onRenderLevelLast(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_LEVEL) {
            return;
        }
        var curAiming = aiming;
        if (version == 2 && curAiming != null) {
            RenderHudHook.withHudMode(() -> renderV2(curAiming.entity(), curAiming.pos(), event.getCamera().getPosition(), event.getPartialTick()));
        }
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
            var oldEntity = Optional.ofNullable(aiming).map(TargetSearcher.SearchResult::entity).orElse(null);
            version = GunsmithLibGunDisplayExtension.of(player.getMainHandItem())
                    .map(GunsmithLibGunDisplayExtension::getFireControlHudVersion)
                    .orElse(GunsmithLibGunDisplayExtension.DEFAULT_FIRE_CONTROL_HUD_VERSION);
            aiming = Gunsmith.getGunInfo(player.getMainHandItem())
                    // 用PartialTick计算会有bug，传入1反而更稳定
                    .flatMap(gi -> TargetSearcher.search(player, gi, partialTick))
                    .orElse(null);
            var newEntity = Optional.ofNullable(aiming).map(TargetSearcher.SearchResult::entity).orElse(null);
            if (newEntity != oldEntity) {
                lastAimSelectTime = now;
                lastAimSelectPartialTicks = partialTick;
            }
        }
    }

    private static final Component AIMING_GLYPH = Component.literal("[+]").withStyle(ChatFormatting.RED);

    private static void renderV1(Entity pEntity, Vec3 aimPos,
                                 EntityRenderDispatcher dispatcher, Font font,
                                 PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        var pDisplayName = AIMING_GLYPH;
        var offset = aimPos.subtract(pEntity.position());
        int i = 0;
        poseStack.pushPose();
        {
            poseStack.translate(offset.x(), offset.y(), offset.z());
            poseStack.mulPose(dispatcher.cameraOrientation());
            poseStack.scale(0.05F, -0.05F, 0.05F);
            Matrix4f matrix4f = poseStack.last().pose();
            float f2 = (float) (-font.width(pDisplayName) / 2);
            font.drawInBatch(pDisplayName, f2, (float) i, 0xFFFF_FFFF, false, matrix4f, buffer, Font.DisplayMode.SEE_THROUGH, 0, packedLight);
        }
        poseStack.popPose();
    }

    private static final String TEX_V2_HOSTILE_INNER = GunsmithLib.loc("textures/gui/v2_crosshair_hostile_inner.png").toString();
    private static final String TEX_V2_HOSTILE_OUTER = GunsmithLib.loc("textures/gui/v2_crosshair_hostile_outer.png").toString();
    private static final String TEX_V2_NEUTRAL_INNER = GunsmithLib.loc("textures/gui/v2_crosshair_neutral_inner.png").toString();
    private static final String TEX_V2_NEUTRAL_OUTER = GunsmithLib.loc("textures/gui/v2_crosshair_neutral_outer.png").toString();
    private static final String TEX_V2_PASSIVE_INNER = GunsmithLib.loc("textures/gui/v2_crosshair_passive_inner.png").toString();
    private static final String TEX_V2_PASSIVE_OUTER = GunsmithLib.loc("textures/gui/v2_crosshair_passive_outer.png").toString();

    private static void renderV2(Entity target, Vec3 aimPos, Vec3 cameraPos, float partialTick) {
        var mc = Minecraft.getInstance();
        var iconRadius = 12;
        var gg = new GuiGraphics(mc, mc.renderBuffers().bufferSource());
        var gui = InWorldGuiGraphicsImpl.at(gg, aimPos, 1, false);
        RenderSystem.enableBlend();
        RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
        // 根据敌我识别结果加载不用资源
        var faction = iff(mc.player, target);
        var inner = switch (faction) {
            case -1 -> TEX_V2_HOSTILE_INNER;
            case 0 -> TEX_V2_NEUTRAL_INNER;
            default -> TEX_V2_PASSIVE_INNER;
        };
        var outer = switch (faction) {
            case -1 -> TEX_V2_HOSTILE_OUTER;
            case 0 -> TEX_V2_NEUTRAL_OUTER;
            default -> TEX_V2_PASSIVE_OUTER;
        };
        var color = switch (faction) {
            case -1 -> ChatFormatting.RED;
            case 0 -> ChatFormatting.YELLOW;
            default -> ChatFormatting.AQUA;
        };
        // Inner
        gui.blit(inner, -iconRadius, -iconRadius, 2 * iconRadius, 2 * iconRadius, 0, 0, 0, 1, 1);
        // Outer
        float iconScale = Optional.ofNullable(mc.level)
                .map(Level::getGameTime)
                .map(gt -> (gt - lastAimSelectTime) + (partialTick - lastAimSelectPartialTicks))
                .map(aimTime -> aimTime / 10)
                .map(alpha -> Mth.clamp(alpha, 0, 1))
                .map(alpha -> Mth.lerp(Mth.sqrt(Mth.sin(alpha * Mth.PI / 2)), 3, 1))
                .orElse(1F);
        var rotCycle = 2.0;
        var now = System.currentTimeMillis() / 1000.0;
        var rot = (now % 1) * 360 / -rotCycle;
        gui.push_pose();
        gui.rotate("z", (float) rot);
        gui.scale(iconScale, iconScale, iconScale);
        gui.blit(outer, -iconRadius, -iconRadius, 2 * iconRadius, 2 * iconRadius, 0, 0, 0, 1, 1);
        gui.pop_pose();
        // Target Name
        if (target != null && aimPos.distanceToSqr(cameraPos) > 16 * 16) {
            gui.push_pose();
            var textScale = 0.75F;
            gui.scale(textScale, textScale, textScale);
            gui.push_style(Style.EMPTY.applyLegacyFormat(color));
            gui.draw_string_centered(target.getDisplayName().getString(), 0, (int) ((-iconRadius - 6) / textScale), Color.WHITE);
            gui.pop_style();
            gui.pop_pose();
        }
        RenderSystem.disableBlend();
    }

    private static int iff(@Nullable Entity actor, Entity target) {
        // getOwner 优先于宠物的 isAlliedTo
        if (target instanceof TraceableEntity traceable && traceable.getOwner() != null && traceable.getOwner() != target) {
            return iff(actor, traceable.getOwner());
        }
        if (target instanceof OwnableEntity ownable && ownable.getOwner() != null && ownable.getOwner() != target) {
            return iff(actor, ownable.getOwner());
        }

        if (actor != null && target.isAlliedTo(actor)) {
            return 1;
        } else if (target instanceof Enemy) {
            return -1;
        } else if (target instanceof NeutralMob) {
            return 0;
        } else {
            return 1;
        }
    }
}
