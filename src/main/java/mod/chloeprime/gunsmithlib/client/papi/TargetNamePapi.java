package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import mod.chloeprime.gunsmithlib.client.papi.framework.PapiTargetCache;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.EntityHitResult;

/**
 * 瞄准目标的名称
 * <p>
 * {@code %gunsmithlib:target_name%}
 * <p>
 * 字符串，不含格式字符
 *
 * @since 6.4
 */
public enum TargetNamePapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("target_name").toString();

    @Override
    public String apply(ItemStack stack) {
        return PapiTargetCache.current()
                .map(Rangefinder.Result::asHitResult)
                .map(r -> r instanceof EntityHitResult er ? er.getEntity() : null)
                .map(Entity::getDisplayName)
                .map(Component::getString)
                .map(ChatFormatting::stripFormatting)
                .orElse(FALLBACK_TEXT);
    }
}
