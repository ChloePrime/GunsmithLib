package mod.chloeprime.gunsmithlib.client.papi.framework;

import net.minecraft.world.item.ItemStack;

import java.util.function.Function;

@FunctionalInterface
public interface Papi extends Function<ItemStack, String> {
    String FALLBACK_TEXT = "-";

    @Override
    String apply(ItemStack stack);
}
