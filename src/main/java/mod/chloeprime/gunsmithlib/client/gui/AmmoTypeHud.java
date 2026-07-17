package mod.chloeprime.gunsmithlib.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import com.tacz.guns.client.resource.pojo.display.gun.AmmoCountStyle;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithClientConfig;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterShadersEvent;

import java.io.IOException;
import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public final class AmmoTypeHud {
    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;
    public static final String SPLIT_TEXT = "×";

    private static ShaderInstance SHADER;

    public static boolean isIconEnabled() {
        return GunsmithClientConfig.HUD_AMMO_TYPE_ICON_OPTION.get() != GunsmithClientConfig.AmmoTypeHudMode.DISABLE;
    }

    public static boolean isIconShaded() {
        return GunsmithClientConfig.HUD_AMMO_TYPE_ICON_OPTION.get() == GunsmithClientConfig.AmmoTypeHudMode.STYLED;
    }

    public static float totalIconWidth() {
        return ICON_WIDTH + 4 + Minecraft.getInstance().font.width(SPLIT_TEXT) + iconXOffset();
    }

    public static float iconXOffset() {
        var isPercentDisplay = GunsmithLibClient.currentGunDisplay()
                .filter(display -> display.getAmmoCountStyle() == AmmoCountStyle.PERCENT)
                .isPresent();
        return isPercentDisplay ? 1.5f * Minecraft.getInstance().font.width("%") : 0;
    }

    public static void renderIcon(GuiGraphics graphics, int width, int height) {
        if (!isIconEnabled()) {
            return;
        }
        var slotTexture = GunsmithLibClient.currentGunInfo()
                .map(GsHelper::getAmmoId)
                .flatMap(TimelessAPI::getClientAmmoIndex)
                .map(ClientAmmoIndex::getSlotTextureLocation)
                .orElse(null);
        if (slotTexture == null) {
            return;
        }
        var textScale = 1.0f;
        var font = Minecraft.getInstance().font;
        var pose = graphics.pose();
        pose.pushPose();
        {
            pose.translate(iconXOffset(), 0, 0);
            pose.scale(textScale, textScale, 1);
            graphics.drawString(font, SPLIT_TEXT, (int) ((width - 20) / textScale), (int) ((height - 40f) / textScale), 0xfff0f0f0, false);
            pose.scale(1 / textScale, 1 / textScale, 1);

            var shaded = isIconShaded();
            pose.translate(0, -2, 0);
            blitAmmoTypeIcon(graphics, slotTexture, shaded, width - 18 + (int) (textScale * font.width(SPLIT_TEXT)), height - 44, ICON_WIDTH, ICON_HEIGHT);
            if (shaded) {
                RenderSystem.setShader(GameRenderer::getPositionTexShader);
            }
        }
        pose.popPose();
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitAmmoTypeIcon(GuiGraphics graphics, ResourceLocation atlas, boolean shaded, int x, int y, int width, int height) {
        blitAmmoTypeIcon0(graphics, atlas, shaded, x, x + width, y, y + height, 0, 0, 1, 0, 1);
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitAmmoTypeIcon0(GuiGraphics graphics, ResourceLocation atlas, boolean shaded, int x1, int x2, int y1, int y2, int dz, float minU, float maxU, float minV, float maxV) {
        if (shaded) {
            RenderSystem.setShader(() -> Optional.ofNullable(SHADER).orElseGet(GameRenderer::getPositionTexShader));
        } else {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
        }
        RenderSystem.setShaderTexture(0, atlas);
        var pose = graphics.pose().last().pose();
        var bufferbuilder = Tesselator.getInstance().begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.addVertex(pose, x1, y1, dz).setUv(minU, minV);
        bufferbuilder.addVertex(pose, x1, y2, dz).setUv(minU, maxV);
        bufferbuilder.addVertex(pose, x2, y2, dz).setUv(maxU, maxV);
        bufferbuilder.addVertex(pose, x2, y1, dz).setUv(maxU, minV);
        BufferUploader.drawWithShader(bufferbuilder.buildOrThrow());
    }

    public static Optional<String> getAmmoName() {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .flatMap(Gunsmith::getGunInfo)
                .map(GsHelper::getAmmoId)
                .flatMap(TimelessAPI::getClientAmmoIndex)
                .map(ClientAmmoIndex::getName)
                .map(I18n::get);
    }

    @SubscribeEvent
    public static void onRegisterShaders(RegisterShadersEvent event) throws IOException {
        event.registerShader(
                new ShaderInstance(event.getResourceProvider(), GunsmithLib.loc("ammo_type"), DefaultVertexFormat.POSITION_TEX),
                instance -> SHADER = instance);
    }

    private AmmoTypeHud() {
    }
}
