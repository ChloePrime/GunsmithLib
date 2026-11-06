package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import com.mojang.math.Divisor;
import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public abstract class AbstractGuiGraphicsAPIImpl implements GuiGraphicsAPI {
    /**
     * GUI 贴图的默认尺寸。
     * <p>
     * 原版的九宫格拉伸、以及不指定贴图尺寸的平铺重载都按 256x256 处理贴图。
     */
    protected static final int DEFAULT_TEXTURE_SIZE = 256;

    protected final Deque<Style> styles = new ArrayDeque<>();

    @Override
    public void push_format(String chat_formatting) {
        ChatFormatting formatObj;
        try {
            formatObj = ChatFormatting.valueOf(chat_formatting.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ignored) {
            formatObj = null;
        }
        if (formatObj == null && !chat_formatting.isEmpty()) {
            formatObj = ChatFormatting.getByCode(chat_formatting.charAt(0));
        }
        if (formatObj == null) {
            throw new IllegalArgumentException("Unknown chat format: %s".formatted(chat_formatting));
        }
        push_style(Style.EMPTY.applyFormat(formatObj));
    }

    @Override
    public void push_style(Style style) {
        var base = styles.isEmpty() ? Style.EMPTY : styles.peek();
        styles.push(style.applyTo(base));
    }

    @Override
    public void pop_style() {
        styles.poll();
    }

    public Component parseText(String text) {
        var component = Component.translatable(text);
        return styles.isEmpty()
                ? component
                : component.setStyle(styles.peek());
    }

    public static ResourceLocation parseTexture(String id) {
        var location = ResourceLocation.tryParse(id);
        if (location == null) {
            throw new IllegalArgumentException("Invalid texture id: " + id);
        }
        return location;
    }

    // 贴图

    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int slice_size, int u, int v, int source_width, int source_height
    ) {
        blit_nine_sliced(texture, x, y, width, height,
                slice_size, slice_size, slice_size, slice_size,
                u, v, source_width, source_height);
    }

    /**
     * 九宫格拉伸。
     * <p>
     * MC 1.21 移除了 {@code GuiGraphics#blitNineSliced}（原版改为读取 GUI 贴图定义里的九宫格元数据），
     * 这里按 1.20.1 的实现把贴图拆成九块：四角缩放、四边与中心平铺，
     * 保证迁移前后的观感一致。
     */
    @Override
    public void blit_nine_sliced(
            String texture, int x, int y, int width, int height,
            int left_slice, int top_slice, int right_slice, int bottom_slice,
            int u, int v, int source_width, int source_height
    ) {
        var location = parseTexture(texture);
        // 四边切片不能超过目标区域的一半，否则会互相重叠（原版同样会做这个收敛）
        left_slice = Math.min(left_slice, width / 2);
        right_slice = Math.min(right_slice, width / 2);
        top_slice = Math.min(top_slice, height / 2);
        bottom_slice = Math.min(bottom_slice, height / 2);
        if (width == source_width && height == source_height) {
            // 目标尺寸和取样区域一致，整块画出来即可
            blitRegion1211(location, x, y, width, height, u, v, width, height,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
        } else if (height == source_height) {
            // 只有水平方向需要拉伸：左右两块 + 中间平铺
            blitRegion1211(location, x, y, left_slice, height, u, v, left_slice, height,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x + left_slice, y, width - left_slice - right_slice, height,
                    u + left_slice, v, source_width - left_slice - right_slice, source_height,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRegion1211(location, x + width - right_slice, y, right_slice, height,
                    u + source_width - right_slice, v, right_slice, height,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
        } else if (width == source_width) {
            // 只有竖直方向需要拉伸：上下两块 + 中间平铺
            blitRegion1211(location, x, y, width, top_slice, u, v, width, top_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x, y + top_slice, width, height - top_slice - bottom_slice,
                    u, v + top_slice, source_width, source_height - top_slice - bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRegion1211(location, x, y + height - bottom_slice, width, bottom_slice,
                    u, v + source_height - bottom_slice, width, bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
        } else {
            // 两个方向都要拉伸：四角 + 四边平铺 + 中心平铺
            blitRegion1211(location, x, y, left_slice, top_slice, u, v, left_slice, top_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x + left_slice, y, width - left_slice - right_slice, top_slice,
                    u + left_slice, v, source_width - left_slice - right_slice, top_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRegion1211(location, x + width - right_slice, y, right_slice, top_slice,
                    u + source_width - right_slice, v, right_slice, top_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);

            blitRepeating1211(location, x, y + top_slice, left_slice, height - top_slice - bottom_slice,
                    u, v + top_slice, left_slice, source_height - top_slice - bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x + left_slice, y + top_slice,
                    width - left_slice - right_slice, height - top_slice - bottom_slice,
                    u + left_slice, v + top_slice,
                    source_width - left_slice - right_slice, source_height - top_slice - bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x + width - right_slice, y + top_slice,
                    right_slice, height - top_slice - bottom_slice,
                    u + source_width - right_slice, v + top_slice,
                    right_slice, source_height - top_slice - bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);

            blitRegion1211(location, x, y + height - bottom_slice, left_slice, bottom_slice,
                    u, v + source_height - bottom_slice, left_slice, bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRepeating1211(location, x + left_slice, y + height - bottom_slice,
                    width - left_slice - right_slice, bottom_slice,
                    u + left_slice, v + source_height - bottom_slice,
                    source_width - left_slice - right_slice, bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
            blitRegion1211(location, x + width - right_slice, y + height - bottom_slice,
                    right_slice, bottom_slice,
                    u + source_width - right_slice, v + source_height - bottom_slice,
                    right_slice, bottom_slice,
                    DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
        }
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height
    ) {
        blit_repeating(texture, x, y, width, height, u, v, source_width, source_height, DEFAULT_TEXTURE_SIZE, DEFAULT_TEXTURE_SIZE);
    }

    @Override
    public void blit_repeating(
            String texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height,
            int texture_width, int texture_height
    ) {
        blitRepeating1211(parseTexture(texture), x, y, width, height, u, v, source_width, source_height, texture_width, texture_height);
    }

    /**
     * 把贴图上 {@code (u, v)} 起、{@code u_width}×{@code v_height} 的像素区域
     * 拉伸绘制到 {@code (x, y)} 起、{@code width}×{@code height} 的矩形。
     * <p>
     * MC 1.21 的 {@code GuiGraphics#blit} 不再有「目标尺寸和取样尺寸不同」之外的九宫格/平铺重载，
     * 因此这里把「区域贴图」抽成一个原语，由各实现类对接自己的 {@code GuiGraphics}
     * （普通 HUD 直接画，世界坐标 HUD 还要叠一次锚点变换）。
     *
     * @param texture        贴图 id
     * @param x              目标矩形左上角 x 坐标
     * @param y              目标矩形左上角 y 坐标
     * @param width          目标矩形宽度
     * @param height         目标矩形高度
     * @param u              取样区域左上角 x 坐标，单位为像素
     * @param v              取样区域左上角 y 坐标，单位为像素
     * @param u_width        取样区域宽度，单位为像素
     * @param v_height       取样区域高度，单位为像素
     * @param texture_width  贴图总宽度，单位为像素
     * @param texture_height 贴图总高度，单位为像素
     */
    protected abstract void blitRegion1211(
            ResourceLocation texture, int x, int y, int width, int height,
            int u, int v, int u_width, int v_height,
            int texture_width, int texture_height
    );

    /**
     * 在指定矩形内平铺贴图的一块取样区域。
     * <p>
     * 平铺方式与原版 1.20.1 的 {@code GuiGraphics#blitRepeating} 一致：
     * 目标区域切成若干块，不足一整块的边角料居中取样，
     * 因此边角料画出来的是源区域正中间那一条，而不是被拉伸的样子。
     */
    private void blitRepeating1211(
            ResourceLocation texture, int x, int y, int width, int height,
            int u, int v, int source_width, int source_height,
            int texture_width, int texture_height
    ) {
        if (width <= 0 || height <= 0 || source_width <= 0 || source_height <= 0) {
            return;
        }
        // 需要平铺的行列数（等价于 Mth.positiveCeilDiv）
        var column_count = (width - 1) / source_width + 1;
        var columns = new Divisor(width, column_count);
        for (int column_x = x; columns.hasNext(); ) {
            var column_width = columns.nextInt();
            var column_u = u + (source_width - column_width) / 2;
            var row_count = (height - 1) / source_height + 1;
            var rows = new Divisor(height, row_count);
            for (int row_y = y; rows.hasNext(); ) {
                var row_height = rows.nextInt();
                var row_v = v + (source_height - row_height) / 2;
                blitRegion1211(texture, column_x, row_y, column_width, row_height,
                        column_u, row_v, column_width, row_height, texture_width, texture_height);
                row_y += row_height;
            }
            column_x += column_width;
        }
    }
}
