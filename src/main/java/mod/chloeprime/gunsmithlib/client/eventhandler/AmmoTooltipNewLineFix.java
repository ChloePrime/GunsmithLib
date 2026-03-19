package mod.chloeprime.gunsmithlib.client.eventhandler;

import com.tacz.guns.api.item.IAmmo;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.util.thread.EffectiveSide;

import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber(Dist.CLIENT)
public class AmmoTooltipNewLineFix {
    public static final boolean ENABLED = true;

    @SubscribeEvent
    public static void onTooltipEvent(ItemTooltipEvent event) {
        if (!ENABLED) {
            return;
        }
        // 服务端不修改
        var user = event.getEntity();
        if (user != null && !user.level().isClientSide()) {
            return;
        }
        if (user == null && EffectiveSide.get().isServer()) {
            return;
        }

        if (!(event.getItemStack().getItem() instanceof IAmmo)) {
            return;
        }
        var buf = BUF.get();
        var mutates = new boolean[] {false};
        try {
            for (var component : event.getToolTip()) {
                if (component instanceof MutableComponent mut && mut.getContents() instanceof TranslatableContents content) {
                    var localized = I18n.get(content.getKey());
                    if (localized.indexOf('\n') >= 0) {
                        for (String line : localized.split("\n")) {
                            buf.add(Component.literal(line).withStyle(component.getStyle()));
                        }
                        mutates[0] = true;
                        continue;
                    }
                }
                buf.add(component);
            }
            if (mutates[0]) {
                event.getToolTip().clear();
                event.getToolTip().addAll(buf);
            }
        } catch (Exception ex) {
            GunsmithLib.LOGGER.error("Error slicing ammo item tooltip", ex);
        } finally {
            buf.clear();
        }
    }

    private static final ThreadLocal<List<Component>> BUF = ThreadLocal.withInitial(ArrayList::new);
}
