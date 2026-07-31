package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.EntityStates;
import mod.chloeprime.gunsmithlib.client.papi.framework.PapiGroup;
import mod.chloeprime.gunsmithlib.common.util.LightType;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;

import java.util.Locale;
import java.util.Optional;

/**
 * 射手受到的不同种类光照的光照等级
 * <ul>
 *   <li>{@code %gunsmithlib:block_light%}</li>
 *   <li>{@code %gunsmithlib:sky_light%}</li>
 *   <li>{@code %gunsmithlib:realtime_light%}</li>
 *   <li>{@code %gunsmithlib:realtime_sky_light%}</li>
 *   <li>{@code %gunsmithlib:theoretical_light%}</li>
 * </ul>
 * 0-15 范围内的整数
 *
 * @since 6.4
 */
@EventBusSubscriber(Dist.CLIENT)
public class LightPapi extends PapiGroup<LightType> {
    public LightPapi(LightType key, String id) {
        super(key, id.formatted(key.name().toLowerCase(Locale.ROOT)));
    }

    public static void register() {
        PapiGroup.register(LightPapi::new, GunsmithLib.MOD_ID + ":%s_light", LightType.values());
    }

    @Override
    public String apply(ItemStack stack) {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(EntityStates::of)
                .map(states -> switch (this.key) {
                    case THEORETICAL -> states.theoretical_light_level();
                    case BLOCK -> states.block_light_level();
                    case SKY -> states.sky_light_level();
                    case REALTIME -> states.light_level();
                    case REALTIME_SKY -> states.realtime_sky_light_level();
                })
                .map(String::valueOf)
                .orElse(FALLBACK_TEXT);
    }

    @SubscribeEvent
    public static void refreshSkyDarkenAtClient(ClientTickEvent.Pre event) {
        Optional.ofNullable(Minecraft.getInstance().level).ifPresent(Level::updateSkyBrightness);
    }
}
