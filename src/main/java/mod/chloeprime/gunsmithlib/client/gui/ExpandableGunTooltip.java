package mod.chloeprime.gunsmithlib.client.gui;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import java.util.ArrayList;
import java.util.List;

public final class ExpandableGunTooltip {
    public static final Component EXPAND_HINT = Component
            .translatable("gunsmithlib.message.press_sheft_to_expand_tooltip")
            .withStyle(ChatFormatting.LIGHT_PURPLE);

    public static List<FormattedCharSequence> expand(List<FormattedCharSequence> raw, List<FormattedCharSequence> limited) {
        // <= 3 行，直接使用原逻辑
        if (raw.size() <= limited.size()) {
            return limited;
        }
        // 按住 Shift 时展开全部 tooltip
        if (Screen.hasShiftDown()) {
            return raw;
        }
        // 提示用户按住 Shift 展开
        var result = new ArrayList<FormattedCharSequence>(limited.size() + 1);
        result.addAll(limited);
        result.add(limited.size(), EXPAND_HINT.getVisualOrderText());
        return result;
    }

    private ExpandableGunTooltip() {
    }
}
