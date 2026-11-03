package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.GuiGraphicsAPI;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Locale;

public abstract class AbstractGuiGraphicsAPIImpl implements GuiGraphicsAPI {
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
}
