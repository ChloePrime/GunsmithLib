package mod.chloeprime.gunsmithlib.client.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.resource.index.ClientAmmoIndex;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.ShaderInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.client.event.RegisterShadersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.io.IOException;
import java.util.Optional;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public final class AmmoTypeHud {
    public static final int ICON_WIDTH = 16;
    public static final int ICON_HEIGHT = 16;
    public static final String SPLIT_TEXT = "×";

    private static ShaderInstance SHADER;

    public static int totalWidth() {
        return ICON_WIDTH + 4 + Minecraft.getInstance().font.width(SPLIT_TEXT);
    }

    public static void render(GuiGraphics graphics, int width, int height) {
        var slotTexture = Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .flatMap(Gunsmith::getGunInfo)
                .map(gi -> gi.index().getGunData().getAmmoId())
                .flatMap(TimelessAPI::getClientAmmoIndex)
                .map(ClientAmmoIndex::getSlotTextureLocation)
                .orElse(null);
        if (slotTexture == null) {
            return;
        }
        var textScale = 1.0f;
        var font = Minecraft.getInstance().font;
        graphics.pose().pushPose();
        graphics.pose().scale(textScale, textScale, 1);
        graphics.drawString(font, SPLIT_TEXT, (int) ((width - 20) / textScale), (int) ((height - 40f) / textScale), 0xfff0f0f0, false);
        graphics.pose().popPose();

        blitAmmoTypeIcon(graphics, slotTexture, width - 18 + (int) (textScale * font.width(SPLIT_TEXT)), height - 44, ICON_WIDTH, ICON_HEIGHT);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
    }

    @SuppressWarnings("SameParameterValue")
    private static void blitAmmoTypeIcon(GuiGraphics graphics, ResourceLocation atlas, int x, int y, int width, int height) {
        blitAmmoTypeIcon0(graphics, atlas, x, x + width, y, y + height, 0, 0, 1, 0, 1);
    }

    private static void blitAmmoTypeIcon0(GuiGraphics graphics, ResourceLocation atlas, int x1, int x2, int y1, int y2, int dz, float minU, float maxU, float minV, float maxV) {
        RenderSystem.setShaderTexture(0, atlas);
        RenderSystem.setShader(() -> Optional.ofNullable(SHADER).orElseGet(GameRenderer::getPositionTexShader));
        var pose = graphics.pose().last().pose();
        var bufferbuilder = Tesselator.getInstance().getBuilder();
        bufferbuilder.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX);
        bufferbuilder.vertex(pose, x1, y1, dz).uv(minU, minV).endVertex();
        bufferbuilder.vertex(pose, x1, y2, dz).uv(minU, maxV).endVertex();
        bufferbuilder.vertex(pose, x2, y2, dz).uv(maxU, maxV).endVertex();
        bufferbuilder.vertex(pose, x2, y1, dz).uv(maxU, minV).endVertex();
        BufferUploader.drawWithShader(bufferbuilder.end());
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
