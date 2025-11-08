package mod.chloeprime.gunsmithlib.client.gui;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import com.mojang.datafixers.util.Either;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.builder.AmmoItemBuilder;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import it.unimi.dsi.fastutil.ints.IntArrayList;
import it.unimi.dsi.fastutil.ints.IntList;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplay;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import mod.chloeprime.gunsmithlib.client.input.SwitchPartOrAmmoTypeKey;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.AmmoVariantSystem;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * 感谢黄毛白鼠（hamsterbaron）提供的轮盘菜单代码 ~
 */
public class GunVariantSelectWheelScreen extends Screen {
    private final List<Component> names;
    private final List<Either<ItemStack, ResourceLocation>> icons;
    private final List<ResourceLocation> gunIds;
    private final IntList ammoCountInBackpack;
    private int selectedIndex = -1;

    public GunVariantSelectWheelScreen(List<ResourceLocation> gunIds) {
        this(gunIds, Component.literal("Ammo Select"));
    }

    public GunVariantSelectWheelScreen(List<ResourceLocation> gunIds, Component title) {
        super(title);
        this.gunIds = gunIds;
        this.names = new ArrayList<>(gunIds.size());
        this.icons = new ArrayList<>(gunIds.size());
        this.ammoCountInBackpack = new IntArrayList(gunIds.size());
        initNamesAndIcons();
        fetchBackpackAmmoCounts();
    }

    private void initNamesAndIcons() {
        for (var gunId : gunIds) {
            var index = TimelessAPI.getClientGunIndex(gunId).orElse(null);
            if (index == null) {
                continue;
            }
            var display = ((EnhancedGunDisplay) index.getDefaultDisplay()).gunsmith$getGunsmithLibExtension();
            var name = display.map(GunsmithLibGunDisplayExtension::getVariantName).orElse(null);
            if (name != null) {
                names.add(Component.translatable(name));
            } else {
                var ammo = AmmoItemBuilder.create().setId(index.getGunData().getAmmoId()).build();
                names.add(ammo.getHoverName());
            }

            var icon = display.map(GunsmithLibGunDisplayExtension::getVariantIcon).orElse(null);
            if (icon != null) {
                icons.add(Either.right(icon));
            } else {
                var bullet = AmmoItemBuilder.create()
                        .setId(index.getGunData().getAmmoId())
                        .setCount(1)
                        .build();
                icons.add(Either.left(bullet));
            }
        }
    }

    /**
     * 如果背包内弹药过多或有无限弹药则返回 {@link Integer#MAX_VALUE}
     */
    private void fetchBackpackAmmoCounts() {
        ammoCountInBackpack.size(gunIds.size());
        for (int i = 0; i < gunIds.size(); i++) {
            // 如果没有自定义图标（用的子弹图标）则向服务端请求子弹数量
            if (icons.get(i).left().isPresent()) {
                RPC.call(RPCTarget.toServer(), AmmoVariantSystem::fetchBackpackAmmoCountFromServer, gunIds.get(i), i);
            }
        }
    }

    /**
     * 如果背包内弹药过多或有无限弹药则 {@param count} 的值为 {@link Integer#MAX_VALUE}
     */
    public static void receiveBackpackAmmoCount(int slot, int count) {
        if (Minecraft.getInstance().screen instanceof GunVariantSelectWheelScreen screen) {
            if (slot >= 0 && slot < screen.ammoCountInBackpack.size()) {
                screen.ammoCountInBackpack.set(slot, count);
            }
        }
    }

    public static boolean isMouseOverSlot(
            double mouseX, double mouseY, double centerX, double centerY,
            double innerR, double outerR, double startAngle, double endAngle
    ) {
        var dx = mouseX - centerX;
        var dy = mouseY - centerY;

        var distSq = dx * dx + dy * dy;
        var dist = Math.sqrt(distSq);

        if (dist < innerR || dist > outerR) {
            return false;
        }

        var angle = Math.atan2(dy, dx);
        if (angle < -Math.PI / 2) {
            angle += 2 * Math.PI;
        }

        if (startAngle < -Math.PI / 2) {
            startAngle += 2 * Math.PI;
        }
        if (endAngle < -Math.PI / 2) {
            endAngle += 2 * Math.PI;
        }

        return endAngle < startAngle
                ? (angle >= startAngle || angle <= endAngle)
                : (angle >= startAngle && angle <= endAngle);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.render(graphics, mouseX, mouseY, partialTick);
        selectedIndex = -1;

        var centerX = width / 2;
        var centerY = height / 2;
        var segmentsPerSlot = 20;

        var player = Minecraft.getInstance().player;

        for (int i = 0; i < icons.size(); i++) {
            var angleStep = 2 * Math.PI / icons.size();
            var startAngle = angleStep * i - Math.PI / 2 - angleStep / 2;
            var endAngle = startAngle + angleStep;

            int radius = 80;
            int innerRadius = radius - 32;
            int outerRadius = radius + 32;
            var isSelected = isMouseOverSlot(
                    mouseX, mouseY, centerX, centerY,
                    innerRadius, outerRadius, startAngle, endAngle);
            if (isSelected) {
                selectedIndex = i;
            }

            var sectorColor = isSelected ? -0x3f000001 : 0x40FFFFFF;

            drawSector(
                    graphics.pose(),
                    centerX,
                    centerY,
                    innerRadius,
                    outerRadius,
                    startAngle,
                    endAngle,
                    segmentsPerSlot,
                    sectorColor
            );

            var itemAngle = (startAngle + endAngle) / 2;
            var x = centerX + radius * Math.cos(itemAngle);
            var y = centerY + radius * Math.sin(itemAngle);

            var itemScale = 2.F;
            var poseStack = graphics.pose();

            var icon = icons.get(i);
            // 渲染物品图标）
            poseStack.pushPose();
            {
                poseStack.translate(x, y, 0);
                poseStack.scale(itemScale, itemScale, itemScale);

                icon.ifLeft(ammoItem -> graphics.renderItem(ammoItem, -8, -8));
                icon.ifRight(png -> graphics.blit(png, -8, -8, 0, 0, 0, 16, 16, 16, 16));
            }
            poseStack.popPose();

            int ammoCount;
            var willRenderCount = icon.left().isPresent();
            if (willRenderCount) {
                String ammoCountText;
                if (player == null) {
                    ammoCount = 0;
                    ammoCountText = "";
                } else {
                    var gun = Gunsmith.getGunInfo(GunItemBuilder.create().setId(gunIds.get(i)).build(player.registryAccess())).orElse(null);
                    if (gun == null) {
                        ammoCount = 0;
                        ammoCountText = "";
                    } else {
                        var invAmmo = GsHelper.scanInventoryAmmo(player, gun);
                        var bpAmmo = ammoCountInBackpack.getInt(i);
                        if (invAmmo.isEmpty() || bpAmmo == Integer.MAX_VALUE) {
                            ammoCount = 9999;
                            ammoCountText = "∞";
                        } else {
                            ammoCount = invAmmo.getAsInt() + bpAmmo;
                            ammoCountText = String.valueOf(ammoCount);
                        }
                    }
                }
                var textWidth = font.width(ammoCountText);
                var textHeight = font.lineHeight;

                poseStack.pushPose();
                {
                    var scale = 1F;
                    poseStack.translate(x, y + 20, 0);
                    poseStack.scale(scale, scale, 1);
                    graphics.drawString(
                            font,
                            ammoCountText,
                            -textWidth / 2,
                            -textHeight / 2,
                            ammoCount <= 0 ? 0xffff0000 : 0xffffffff,
                            true);
                }
                poseStack.popPose();
            } else {
                // 渲染图标时使用有子弹时的配色
                ammoCount = 1;
            }
            if (isSelected) {
                var name = names.get(i);
                var nameWidth = font.width(name);
                var nameColor = ammoCount <= 0 ? 0xffff0000 : 0xffffffff;
                graphics.drawString(font, name, (width - nameWidth) / 2, (height - font.lineHeight) / 2, nameColor, true);
            }
        }
    }

    @SuppressWarnings("SameParameterValue")
    private void drawSector(
            PoseStack pose, int centerX, int centerY,
            double innerR, double outerR,
            double startRad, double endRad,
            int segments, int color
    ) {
        var tesselator = Tesselator.getInstance();
        var buffer = tesselator.begin(VertexFormat.Mode.TRIANGLE_STRIP, DefaultVertexFormat.POSITION_COLOR);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);

        for (int i = 0; i < segments; i++) {
            var angle = startRad + (endRad - startRad) * i / segments;
            var cos = Math.cos(angle);
            var sin = Math.sin(angle);

            var x1 = centerX + outerR * cos;
            var y1 = centerY + outerR * sin;
            var x2 = centerX + innerR * cos;
            var y2 = centerY + innerR * sin;

            var a = (color >> 24) & 0xFF;
            var r = (color >> 16) & 0xFF;
            var g = (color >> 8) & 0xFF;
            var b = (color) & 0xFF;

            buffer.addVertex(pose.last().pose(), (float) x1, (float) y1, 0f).setColor(r, g, b, a);
            buffer.addVertex(pose.last().pose(), (float) x2, (float) y2, 0f).setColor(r, g, b, a);
        }

        BufferUploader.drawWithShader(Objects.requireNonNull(buffer.build()));

        RenderSystem.disableBlend();
    }

    @Override
    public boolean keyReleased(int keyCode, int scanCode, int modifiers) {
        if (SwitchPartOrAmmoTypeKey.KEY_MAPPING.matches(keyCode, scanCode)) {
            switchVariant();
            return false;
        }
        return super.keyReleased(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (SwitchPartOrAmmoTypeKey.KEY_MAPPING.matchesMouse(button)) {
            switchVariant();
            return false;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    private void switchVariant() {
        if (selectedIndex >= 0) {
            RPC.call(RPCTarget.toServer(), AmmoVariantSystem::switchToVariant, selectedIndex);
        }
        onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
