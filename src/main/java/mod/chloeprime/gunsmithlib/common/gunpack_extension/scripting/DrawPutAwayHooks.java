package mod.chloeprime.gunsmithlib.common.gunpack_extension.scripting;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunDrawEvent;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Optional;

/**
 * @since 6.1
 */
@EventBusSubscriber
public final class DrawPutAwayHooks {
    @SubscribeEvent
    public static void onGunDraw(GunDrawEvent event) {
        var shooter = event.getEntity();
        if (shooter == null || shooter.level().isClientSide()) {
            return;
        }
        runHookFor(shooter, event.getPreviousGunItem(), "gunsmithlib_on_put_away");
        runHookFor(shooter, event.getCurrentGunItem(), "gunsmithlib_on_draw");
    }

    private static void runHookFor(LivingEntity shooter, ItemStack item, String hookName) {
        var gun = Gunsmith.getGunInfo(item).orElse(null);
        if (gun == null) {
            return;
        }
        // Create API
        var api = new ModernKineticGunScriptAPI();
        api.setShooter(shooter);
        api.setDataHolder(IGunOperator.fromLivingEntity(shooter).getDataHolder());
        api.setItemStack(item);
        // Execute Script
        Optional.ofNullable(gun.index().getScript())
                .map(script -> GsHelper.checkFunction(script.get(hookName)))
                .ifPresent(func -> func.call(CoerceJavaToLua.coerce(api)));
    }

    private DrawPutAwayHooks() {
    }
}
