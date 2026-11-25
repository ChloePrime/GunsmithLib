package mod.chloeprime.gunsmithlib.compat.jei.client;

import com.mojang.datafixers.util.Either;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.GunsmithLibGunDisplayExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.List;
import java.util.Optional;

@ParametersAreNonnullByDefault
public record GunVariantIconRenderer(
        ItemStack gun,
        IIngredientRenderer<ItemStack> delegate
) implements IIngredientRenderer<ItemStack> {
    public static final Component UNKNOWN_NAME = Component.literal("?");
    public static final ResourceLocation UNKNOWN_ICON = GunsmithLib.loc("textures/gui/unknown_entry.png");

    @Override
    public void render(GuiGraphics guiGraphics, ItemStack ignored) {
        render(guiGraphics, ignored, 0, 0);
    }

    @Override
    public void render(GuiGraphics guiGraphics, ItemStack ignored, int posX, int posY) {
        var iconConfig = icon();
        iconConfig.ifLeft(ammo -> delegate.render(guiGraphics, ammo, posX, posY));
        iconConfig.ifRight(icon -> renderIcon(guiGraphics, icon, posX, posY));
    }

    private void renderIcon(GuiGraphics gui, ResourceLocation icon, int posX, int posY) {
        gui.blit(icon, posX, posY, 0, 0, 0, 16, 16, 16, 16);
    }

    @SuppressWarnings("removal")
    @Override
    public @Nonnull List<Component> getTooltip(ItemStack ignored, TooltipFlag tooltipFlag) {
        var nameConfig = name();
        var lines = new MutableObject<List<Component>>();
        nameConfig.ifLeft(ammo -> lines.setValue(ammo.getTooltipLines(Minecraft.getInstance().player, tooltipFlag)));
        nameConfig.ifRight(name -> lines.setValue(List.of(name)));
        return lines.getValue();
    }

    private Either<ItemStack, Component> name() {
        return GunsmithLibGunDisplayExtension.of(gun)
                .map(GunsmithLibGunDisplayExtension::getVariantName)
                .map(name -> (Component) Component.translatable(name))
                .map(Either::<ItemStack, Component>right)
                .or(() -> ammo().map(Either::left))
                .orElseGet(() -> Either.right(UNKNOWN_NAME));
    }

    private Either<ItemStack, ResourceLocation> icon() {
        return GunsmithLibGunDisplayExtension.of(gun)
                .map(GunsmithLibGunDisplayExtension::getVariantIcon)
                .map(Either::<ItemStack, ResourceLocation>right)
                .or(() -> ammo().map(Either::left))
                .orElseGet(() -> Either.right(UNKNOWN_ICON));
    }

    private Optional<ItemStack> ammo() {
        return Gunsmith.getGunInfo(gun)
                .map(GunInfo::index)
                .map(CommonGunIndex::getGunData)
                .map(GunData::getAmmoId)
                .map(Gunsmith::createAmmoItemFromId);
    }
}
