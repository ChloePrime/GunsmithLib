package mod.chloeprime.gunsmithlib.compat.jei.mc1211;

import cn.chloeprime.commons.ContextUtil;
import com.google.common.collect.Lists;
import com.tacz.guns.api.TimelessAPI;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;
import java.util.stream.Stream;

@JeiPlugin
public class DurabilityEnchantJeiPlugin1211 implements IModPlugin {
    public static final ResourceLocation ID = GunsmithLib.loc("durability_system");

    @Override
    public @Nonnull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        var registry = ContextUtil.getRegistryAccess();
        var enchants = registry.registry(Registries.ENCHANTMENT)
                .stream()
                .flatMap(Registry::holders)
                .filter(GunsmithLib.Items.DURABILITY_ENCHANT_PROBE_1211.get().getDefaultInstance()::supportsEnchantment)
                .map(EnchantmentData::new)
                .toList();
        if (enchants.isEmpty()) {
            return;
        }
        var vFactory = registration.getJeiHelpers().getVanillaRecipeFactory();
        var recipes = getRepairData()
                .<IJeiAnvilRecipe>mapMulti((repairData, chute) -> makeEnchantRecipes(repairData, vFactory, enchants, chute))
                .toList();
        registration.addRecipes(RecipeTypes.ANVIL, recipes);
    }

    private static void makeEnchantRecipes(
            RepairData repairData,
            IVanillaRecipeFactory factory,
            List<EnchantmentData> enchants,
            Consumer<IJeiAnvilRecipe> outputChute
    ) {
        var contentId = repairData.contentId();
        var content = switch (repairData.type()) {
            case RepairData.TYPE_GUN -> Gunsmith.createGunItemFromId(contentId, ContextUtil.getRegistryAccess());
            case RepairData.TYPE_ATTACHMENT -> Gunsmith.createAttachmentItemFromId(contentId);
            default -> ItemStack.EMPTY;
        };
        if (content == null || content.isEmpty()) {
            return;
        }
        enchants.stream()
                .filter(data -> data.canEnchant(content))
                .forEach(data -> {
                    var books = data.getEnchantedBooks(content);
                    var out = getEnchantedContents(content, books);
                    var encId = data.enchantment.key().location();
                    var id = GunsmithLib.loc("tacz_enchantment.%d.%s.%s.%s.%s".formatted(
                            repairData.type(),
                            contentId.getNamespace(), contentId.getPath(),
                            encId.getNamespace(), encId.getPath()));
                    outputChute.accept(factory.createAnvilRecipe(content, books, out, id));
                });
    }

    private record RepairData(
            ResourceLocation contentId,
            int type,
            @Nullable Ingredient repairMaterial,
            int maxDurability1211
    ) {
        public static final int TYPE_GUN        = 0;
        public static final int TYPE_ATTACHMENT = 2;
    }

    private static Stream<RepairData> getRepairData() {
        var gun = TimelessAPI.getAllCommonGunIndex()
                .stream()
                .map(pair -> {
                    var key = new RepairData(pair.getKey(), RepairData.TYPE_GUN, null, 0);
                    return Pair.of(key, DurabilityData.of(pair.getValue().getGunData()).orElse(null));
                });
        var attach = TimelessAPI.getAllCommonAttachmentIndex()
                .stream()
                .map(pair -> {
                    var key = new RepairData(pair.getKey(), RepairData.TYPE_ATTACHMENT, null, 0);
                    return Pair.of(key, DurabilityData.of(pair.getValue().getData()).orElse(null));
                });

        return Stream.concat(gun, attach)
                .filter(pair -> pair.getValue() != null)
                .map(pair -> {
                    var archetype = pair.getKey();
                    var durData = Objects.requireNonNull(pair.getValue());
                    return new RepairData(archetype.contentId(), archetype.type(), durData.getRepairMaterial().orElse(null), durData.getMaxDurability());
                });
    }

    private static final class EnchantmentData {
        private final Holder.Reference<Enchantment> enchantment;
        private final List<ItemStack> enchantedBooks;

        private EnchantmentData(Holder.Reference<Enchantment> enchantment) {
            this.enchantment = enchantment;
            this.enchantedBooks = getEnchantedBooks(enchantment);
        }

        public List<ItemStack> getEnchantedBooks(ItemStack ingredient) {
            var list = enchantedBooks.stream()
                    .filter(ingredient::isBookEnchantable)
                    .toList();
            // avoid using copy of list if it contains the exact same items
            return list.size() == enchantedBooks.size() ? enchantedBooks : list;
        }

        private boolean canEnchant(ItemStack ingredient) {
            try {
                return ingredient.supportsEnchantment(enchantment);
            } catch (RuntimeException e) {
                GunsmithLib.LOGGER.error("Failed to check if ingredient can be enchanted: {}", ingredient.getDisplayName().getString(), e);
                return false;
            }
        }

        private static List<ItemStack> getEnchantedBooks(Holder<Enchantment> enchantment) {
            return IntStream.rangeClosed(1, enchantment.value().getMaxLevel())
                    .mapToObj(level -> {
                        var bookEnchant = Items.ENCHANTED_BOOK.getDefaultInstance();
                        ItemEnchantments.Mutable itemEnchantments = new ItemEnchantments.Mutable(EnchantmentHelper.getEnchantmentsForCrafting(bookEnchant));
                        itemEnchantments.set(enchantment, level);
                        EnchantmentHelper.setEnchantments(bookEnchant, itemEnchantments.toImmutable());
                        return bookEnchant;
                    })
                    .toList();
        }
    }

    private static List<ItemStack> getEnchantedContents(ItemStack content, List<ItemStack> enchantedBooks) {
        return Lists.transform(enchantedBooks, enchantedBook -> getEnchantedContent(content, enchantedBook));
    }

    private static ItemStack getEnchantedContent(ItemStack unenchanted, ItemStack enchantedBook) {
        var enchantedIngredient = unenchanted.copy();
        var enchantments = EnchantmentHelper.getEnchantmentsForCrafting(enchantedBook);
        EnchantmentHelper.setEnchantments(enchantedIngredient, enchantments);
        return enchantedIngredient;
    }
}
