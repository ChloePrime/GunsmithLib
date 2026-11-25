package mod.chloeprime.gunsmithlib.compat.jei;

import cn.chloeprime.commons.ContextUtil;
import com.tacz.guns.api.TimelessAPI;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.recipe.vanilla.IJeiAnvilRecipe;
import mezz.jei.api.recipe.vanilla.IVanillaRecipeFactory;
import mezz.jei.api.registration.IRecipeRegistration;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.durability.DurabilityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

@JeiPlugin
public class DurabilitySystemJeiPlugin implements IModPlugin {
    public static final ResourceLocation ID = GunsmithLib.loc("durability_system");

    @Override
    public @Nonnull ResourceLocation getPluginUid() {
        return ID;
    }

    @Override
    public void registerRecipes(@Nonnull IRecipeRegistration registration) {
        var vFactory = registration.getJeiHelpers().getVanillaRecipeFactory();
        var recipes = getRepairData()
                .<IJeiAnvilRecipe>mapMulti((repairData, chute) -> makeRepairRecipes(repairData, vFactory, chute))
                .toList();
        registration.addRecipes(RecipeTypes.ANVIL, recipes);
    }

    private static void makeRepairRecipes(
            RepairData repairData,
            IVanillaRecipeFactory factory,
            Consumer<IJeiAnvilRecipe> outputChute
    ) {
        var contentId = repairData.contentId();
        var damage3of4 = switch (repairData.type()) {
            case RepairData.TYPE_GUN -> Gunsmith.createGunItemFromId(contentId, ContextUtil.getRegistryAccess());
            case RepairData.TYPE_ATTACHMENT -> Gunsmith.createAttachmentItemFromId(contentId);
            default -> ItemStack.EMPTY;
        };
        if (damage3of4 == null || damage3of4.isEmpty()) {
            return;
        }
        int maxDamage = damage3of4.getMaxDamage();
        damage3of4.setDamageValue(maxDamage * 3 / 4);
        var damageHalf = damage3of4.copy();
        damageHalf.setDamageValue(maxDamage / 2);

        outputChute.accept(factory.createAnvilRecipe(
                List.of(damage3of4),
                List.of(damage3of4),
                List.of(damageHalf),
                GunsmithLib.loc("tacz_self_repair.%s.%s".formatted(contentId.getNamespace(), contentId.getPath()))
        ));

        if (repairData.repairMaterial() != null && !repairData.repairMaterial().isEmpty()) {
            var broken = damage3of4.copy();
            broken.setDamageValue(broken.getMaxDamage());

            outputChute.accept(factory.createAnvilRecipe(
                    List.of(broken),
                    List.of(repairData.repairMaterial().getItems()),
                    List.of(damage3of4),
                    GunsmithLib.loc("tacz_material_repair.%s.%s".formatted(contentId.getNamespace(), contentId.getPath()))
            ));
        }
    }

    private record RepairData(
            ResourceLocation contentId,
            int type,
            @Nullable Ingredient repairMaterial
    ) {
        public static final int TYPE_GUN        = 0;
        public static final int TYPE_ATTACHMENT = 2;
    }

    private static Stream<RepairData> getRepairData() {
        var gun = TimelessAPI.getAllCommonGunIndex()
                .stream()
                .map(pair -> {
                    var key = new RepairData(pair.getKey(), RepairData.TYPE_GUN, null);
                    return Pair.of(key, DurabilityData.of(pair.getValue().getGunData()).orElse(null));
                });
        var attach = TimelessAPI.getAllCommonAttachmentIndex()
                .stream()
                .map(pair -> {
                    var key = new RepairData(pair.getKey(), RepairData.TYPE_ATTACHMENT, null);
                    return Pair.of(key, DurabilityData.of(pair.getValue().getData()).orElse(null));
                });

        return Stream.concat(gun, attach)
                .filter(pair -> pair.getValue() != null)
                .map(pair -> {
                    var archetype = pair.getKey();
                    var durData = Objects.requireNonNull(pair.getValue());
                    return new RepairData(archetype.contentId(), archetype.type(), durData.getRepairMaterial().orElse(null));
                });
    }
}
