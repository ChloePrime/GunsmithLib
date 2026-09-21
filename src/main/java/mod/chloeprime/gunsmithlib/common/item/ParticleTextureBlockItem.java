package mod.chloeprime.gunsmithlib.common.item;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class ParticleTextureBlockItem extends BlockItem {
    public static final Component USAGE_HINT = Component
            .translatable("gunsmithlib.message.particle_block_hint")
            .withStyle(ChatFormatting.DARK_GRAY);

    public ParticleTextureBlockItem(Supplier<? extends Block> block, Properties properties) {
        super(block.get(), properties);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> lines, TooltipFlag flag) {
        super.appendHoverText(stack, level, lines, flag);
        if (flag.isAdvanced() && !hasLore(stack)) {
            lines.add(USAGE_HINT);
        }
    }

    private static boolean hasLore(ItemStack stack) {
        if (!stack.hasTag()) {
            return false;
        }
        var tag = Objects.requireNonNull(stack.getTag());
        if (!tag.contains(ItemStack.TAG_DISPLAY, Tag.TAG_COMPOUND)) {
            return false;
        }
        return tag.getCompound(ItemStack.TAG_DISPLAY).contains(ItemStack.TAG_LORE, Tag.TAG_LIST);
    }
}
