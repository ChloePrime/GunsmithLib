package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import com.mojang.math.Axis;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import mod.chloeprime.gunsmithlib.mixin.client.minecraft.GuiGraphicsAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.Locale;
import java.util.Objects;

/**
 * {@link GuiGraphicsAPI} 的实现，直接委托给原版的 {@link GuiGraphics}。
 *
 * @since 7.0.0
 */
public class GuiGraphicsAPIImpl implements GuiGraphicsAPI {
    private final GuiGraphics impl;

    /**
     * @param impl 被封装的原版 {@link GuiGraphics}
     */
    public GuiGraphicsAPIImpl(GuiGraphics impl) {
        this.impl = Objects.requireNonNull(impl, "impl");
    }

    @Override
    public int width() {
        return impl.guiWidth();
    }

    @Override
    public int height() {
        return impl.guiHeight();
    }

    @Override
    public void push_pose() {
        impl.pose().pushPose();
    }

    @Override
    public void pop_pose() {
        impl.pose().popPose();
    }

    @Override
    public void translate(float x, float y, float z) {
        impl.pose().translate(x, y, z);
    }

    @Override
    public void rotate(String axis, float degrees) {
        var quaternion = switch (axis.toLowerCase(Locale.ROOT)) {
            case "x" -> Axis.XP.rotationDegrees(degrees);
            case "y" -> Axis.YP.rotationDegrees(degrees);
            case "z" -> Axis.ZP.rotationDegrees(degrees);
            default -> throw new IllegalArgumentException("Invalid rotation axis: " + axis + " (expected x, y or z)");
        };
        impl.pose().mulPose(quaternion);
    }

    @Override
    public void scale(float x, float y, float z) {
        impl.pose().scale(x, y, z);
    }

    @Override
    public void fill(int min_x, int min_y, int max_x, int max_y, Color color) {
        impl.fill(min_x, min_y, max_x, max_y, color.getRGB());
    }

    @Override
    public void fill_gradient(int x1, int y1, int x2, int y2, Color color_from, Color color_to) {
        impl.fillGradient(x1, y1, x2, y2, color_from.getRGB(), color_to.getRGB());
    }

    @Override
    public void h_line(int min_x, int max_x, int y, Color color) {
        impl.hLine(min_x, max_x, y, color.getRGB());
    }

    @Override
    public void v_line(int x, int min_y, int max_y, Color color) {
        impl.vLine(x, min_y, max_y, color.getRGB());
    }

    @Override
    public void render_outline(int x, int y, int width, int height, Color color) {
        impl.renderOutline(x, y, width, height, color.getRGB());
    }

    @Override
    public void set_color(Color color) {
        var rgba = color.getRGBComponents(GET_COLOR_BUFFER.get());
        impl.setColor(rgba[0], rgba[1], rgba[2], rgba[3]);
    }

    @Override
    public int draw_string(String text, float x, float y, Color color, boolean drop_shadow) {
        return impl.drawString(font(), Component.translatable(text), Mth.floor(x), Mth.floor(y), color.getRGB(), drop_shadow);
    }

    @Override
    public void draw_string_centered(String text, int x, int y, Color color) {
        impl.drawCenteredString(font(), Component.translatable(text), x, y, color.getRGB());
    }

    @Override
    public void draw_string_wrapped(String text, int x, int y, int line_width, Color color) {
        impl.drawWordWrap(font(), Component.translatable(text), x, y, line_width, color.getRGB());
    }

    @Override
    public void blit(String texture, int x, int y, int u, int v, int width, int height) {
        impl.blit(parseTexture(texture), x, y, 0, u, v, width, height, 256, 256);
    }

    @Override
    public void blit(String texture, int x, int y, int width, int height, int z, float u, float v, float u_width, float v_height) {
        ((GuiGraphicsAccessor) impl).invokeInnerBlit(parseTexture(texture), x, x + width, y, y + height, z, u, u + u_width, v, v + v_height);
    }

    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int slice_size, int u, int v, int source_width, int source_height
    ) {
        impl.blitNineSliced(parseTexture(texture), x, y, width, height,
                slice_size, slice_size, slice_size, slice_size,
                source_width, source_height, u, v);
    }

    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int left_slice, int top_slice, int right_slice, int bottom_slice,
            int u, int v, int source_width, int source_height
    ) {
        impl.blitNineSliced(parseTexture(texture), x, y, width, height,
                left_slice, top_slice, right_slice, bottom_slice,
                source_width, source_height, u, v);
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height
    ) {
        impl.blitRepeating(parseTexture(texture), x, y, width, height, u, v, source_width, source_height);
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height,
            int texture_width, int texture_height
    ) {
        impl.blitRepeating(parseTexture(texture), x, y, width, height, u, v, source_width, source_height, texture_width, texture_height);
    }

    @Override
    public void render_item(ItemStack stack, int x, int y) {
        impl.renderItem(stack, x, y);
    }

    @Override
    public void render_item_decorations(ItemStack stack, int x, int y) {
        impl.renderItemDecorations(font(), stack, x, y);
    }

    @Override
    public void enable_scissor(int min_x, int min_y, int max_x, int max_y) {
        impl.enableScissor(min_x, min_y, max_x, max_y);
    }

    @Override
    public void disable_scissor() {
        impl.disableScissor();
    }

    private static final ThreadLocal<float[]> GET_COLOR_BUFFER = ThreadLocal.withInitial(() -> new float[4]);

    private static Font font() {
        return Objects.requireNonNull(Minecraft.getInstance().font, "font");
    }

    private static ResourceLocation parseTexture(String id) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid texture id: " + id);
        }
        return location;
    }
}
