package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun;

import cn.chloeprime.commons.rpc.RPCContext;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.GunShootEvent;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Optional;

@EventBusSubscriber
public class ChargeableTriggerSystem {
    /**
     * @since 6.0.0
     */
    public static final String DEPRECATION_MESSAGE =
            "Trying to get GunsmithLib's charge system, which is deprecated and not functional. " +
            "Please migrate to TaCZ 1.1.8's charge system.";

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER)
    public static void beginCharging() {
        var sender = RPCContext.isCalledThroughRPC() ? RPCContext.getSenderPlayer() : null;
        if (sender == null) {
            return;
        }
        beginCharging(sender, Gunsmith.getGunInfo(sender.getMainHandItem()).orElse(null));
    }

    public static void beginCharging(Player user, GunInfo gun) {
        if (user == null || gun == null || user.level().isClientSide()) {
            return;
        }

        setChargeBeginTime(gun.gunStack(), user.level().getGameTime());

        var api = new ModernKineticGunScriptAPI();
        api.setItemStack(gun.gunStack());
        api.setShooter(user);
        api.setDataHolder(IGunOperator.fromLivingEntity(user).getDataHolder());

        Optional.ofNullable(gun.index().getScript())
                .map(script -> GsHelper.checkFunction(script.get("gunsmithlib_begin_charging")))
                .ifPresent(func -> func.call(CoerceJavaToLua.coerce(api)));
    }

    @SubscribeEvent
    public static void onPlayerShoot(GunShootEvent event) {
        var shooter = event.getShooter();
        if (shooter == null || shooter.level().isClientSide()) {
            return;
        }
        setChargeFinished(event.getGunItemStack());
    }

    public static boolean isCharging(ItemStack gun) {
        return gun.getOrDefault(GunsmithLib.DataComponents.CHARGING, false);
    }

    public static boolean isInChargingSequence(ItemStack gun) {
        return gun.has(GunsmithLib.DataComponents.CHARGE_BEGIN_TIME);
    }

    public static long getChargeBeginTime(ItemStack gun) {
        return gun.getOrDefault(GunsmithLib.DataComponents.CHARGE_BEGIN_TIME, 0L);
    }

    /**
     * 单位为刻
     */
    public static long getChargeTime(ItemStack gun, long now) {
        return isInChargingSequence(gun) ? Math.max(0, now - getChargeBeginTime(gun)) : 0;
    }

    public static void setChargeBeginTime(ItemStack gun, long value) {
        gun.set(GunsmithLib.DataComponents.CHARGING, true);
        gun.set(GunsmithLib.DataComponents.CHARGE_BEGIN_TIME, value);
    }

    public static void setChargeFinished(ItemStack gun) {
        gun.remove(GunsmithLib.DataComponents.CHARGING);
    }

    public static void removeChargeBeginTimeIfNeeded(ItemStack gun) {
        if (!isCharging(gun)) {
            GsHelper.tryRemoveComponent(gun, GunsmithLib.DataComponents.CHARGE_BEGIN_TIME);
        }
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (event.getEntity().level().isClientSide()) {
            return;
        }
        removeChargeBeginTimeIfNeeded(event.getEntity().getMainHandItem());
    }
}
