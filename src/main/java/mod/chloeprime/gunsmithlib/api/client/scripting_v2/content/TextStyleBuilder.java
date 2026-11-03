package mod.chloeprime.gunsmithlib.api.client.scripting_v2.content;

import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.ResourceLocation;

import java.awt.*;
import java.util.Objects;

@SuppressWarnings("unused")
public class TextStyleBuilder {
    private Style style = Style.EMPTY;

    public Style create() {
        return Objects.requireNonNull(style);
    }

    public TextStyleBuilder bold() {
        return bold(true);
    }

    public TextStyleBuilder bold(boolean value) {
        style = style.withBold(value);
        return this;
    }

    public TextStyleBuilder color(Color color) {
        style = style.withColor(TextColor.fromRgb(color.getRGB()));
        return this;
    }

    public TextStyleBuilder font(ResourceLocation fontId) {
        style = style.withFont(fontId);
        return this;
    }

    public TextStyleBuilder italic() {
        return italic(true);
    }

    public TextStyleBuilder italic(boolean value) {
        style = style.withItalic(value);
        return this;
    }

    public TextStyleBuilder obfuscated() {
        return obfuscated(true);
    }

    public TextStyleBuilder obfuscated(boolean value) {
        style = style.withObfuscated(value);
        return this;
    }

    public TextStyleBuilder strikethrough() {
        return strikethrough(true);
    }

    public TextStyleBuilder strikethrough(boolean value) {
        style = style.withStrikethrough(value);
        return this;
    }

    public TextStyleBuilder underline() {
        return underline(true);
    }

    public TextStyleBuilder underline(boolean value) {
        style = style.withUnderlined(value);
        return this;
    }
}
