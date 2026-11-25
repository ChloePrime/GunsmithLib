package mod.chloeprime.gunsmithlib.compat.jei.client;

import com.google.common.collect.Iterables;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IJeiHelpers;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.GunAmmoVariantSet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class GunAmmoVariantSystemQueryCategory implements IRecipeCategory<GunAmmoVariantSet> {
    public static final Component TITLE = Component.translatable("gunsmithlib.message.jei.category.gun_ammo_variant_query");
    public static final ResourceLocation ICON = GunsmithLib.loc("textures/gui/jei_gavs_category_icon.png");
    private final IJeiHelpers jeiHelper;
    private final RecipeType<GunAmmoVariantSet> recipeType;

    public GunAmmoVariantSystemQueryCategory(RecipeType<GunAmmoVariantSet> type, IJeiHelpers jeiHelper) {
        this.recipeType = type;
        this.jeiHelper = jeiHelper;
    }

    @Override
    public @Nonnull RecipeType<GunAmmoVariantSet> getRecipeType() {
        return this.recipeType;
    }

    @Override
    public @Nonnull Component getTitle() {
        return TITLE;
    }

    @Override
    public @Nullable IDrawable getIcon() {
        return jeiHelper.getGuiHelper().drawableBuilder(ICON, 0, 0, 16, 16)
                .setTextureSize(16, 16)
                .build();
    }

    @Override
    public int getWidth() {
        return 162;
    }

    @Override
    public int getHeight() {
        return 156;
    }

    @Override
    @ParametersAreNonnullByDefault
    public void draw(GunAmmoVariantSet recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        var text = Component.translatable("gunsmithlib.message.jei.category.gun_ammo_variant_query.separator", recipe.allGunIds().size());
        guiGraphics.drawString(font, text, (162 - font.width(text)) / 2, 22, 0xFF555555, false);
    }

    @Override
    @ParametersAreNonnullByDefault
    public void setRecipe(IRecipeLayoutBuilder builder, GunAmmoVariantSet recipe, IFocusGroup focuses) {
        var gunIds = new ArrayList<>(recipe.allGunIds());
        var master = recipe.masterId().or(() -> gunIds.stream().findFirst()).orElse(null);
        gunIds.removeIf(id -> Objects.equals(id, master));

        var itemStackRenderer = jeiHelper.getIngredientManager().getIngredientRenderer(VanillaTypes.ITEM_STACK);
        var i = new int[]{0};
        Iterables.concat(Optional.ofNullable(master).map(List::of).orElse(List.of()), gunIds)
                .forEach(gunId -> {
                    var ammoId = TimelessAPI.getCommonGunIndex(gunId)
                            .map(CommonGunIndex::getGunData)
                            .map(GunData::getAmmoId)
                            .orElse(null);
                    if (ammoId == null) {
                        return;
                    }
                    int columns = 9;
                    int x = 1 + i[0] % columns * 18;
                    int y = 35 + i[0] / columns * 18;
                    builder.addInputSlot(x, y)
                            .addItemStack(Gunsmith.createAmmoItemFromId(ammoId))
                            .setCustomRenderer(VanillaTypes.ITEM_STACK, new GunVariantIconRenderer(Gunsmith.createGunItemFromId(gunId), itemStackRenderer))
                            .setBackground(jeiHelper.getGuiHelper().getSlotDrawable(), -1, -1);
                    i[0]++;
                });

        builder.addOutputSlot(73, 1)
                .addItemStack(master == null ? Items.BARRIER.getDefaultInstance() : Gunsmith.createGunItemFromId(master))
                .setBackground(jeiHelper.getGuiHelper().getSlotDrawable(), -1, -1);

        builder.addOutputSlot()
                .setPosition(0, -99999999)
                .addItemStacks(gunIds.stream().map(Gunsmith::createGunItemFromId).toList());
    }
}
