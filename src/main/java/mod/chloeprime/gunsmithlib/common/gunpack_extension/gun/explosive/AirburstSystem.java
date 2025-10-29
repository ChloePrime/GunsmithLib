package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RemoteCallable;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.internal.BulletReadyToTraceEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.OptionalDouble;

/**
 * 空 爆 榴 弹
 * 捞 薯 神 器
 * 嘻嘻
 *
 * @since 4.9.0
 */
@EventBusSubscriber
public class AirburstSystem {
    public static final String PDK_AIRBURST_DISTANCE = GunsmithLib.loc("airburst_distance").toString();

    public static int getSelectedDistanceIndex(ItemStack stack) {
        return Objects.requireNonNullElse(stack.get(GunsmithLib.DataComponents.SELECTED_AIRBURST_DISTANCE_INDEX), 0);
    }

    public static OptionalDouble getSelectedDistance(GunInfo gun) {
        var distances = getAirburstDistances(gun);
        if (distances.isEmpty()) {
            return OptionalDouble.empty();
        }
        var index = getSelectedDistanceIndex(gun.gunStack());
        var result = distances.getDouble(Mth.clamp(index, 0, distances.size() - 1));
        return OptionalDouble.of(result);
    }

    public static void setSelectedDistanceIndex(ItemStack stack, int value) {
        stack.set(GunsmithLib.DataComponents.SELECTED_AIRBURST_DISTANCE_INDEX, value);
    }

    public static @Nonnull DoubleList getAirburstDistances(GunInfo gun) {
        return GunExplosiveData.fromGun(gun)
                .map(GunExplosiveData::getAirburstDistances)
                .orElse(DoubleList.of());
    }

    public static double getAirburstDistanceDistribution(GunInfo gun) {
        return GunExplosiveData.fromGun(gun)
                .map(GunExplosiveData::getAirburstDistancesDistribution)
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty())
                .orElse(0);
    }

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER, callLocally = true)
    public static void onSelectAirburstIndex(Player user) {
        if (user == null) {
            return;
        }
        var gun = Gunsmith.getGunInfo(user.getMainHandItem()).orElse(null);
        if (gun == null) {
            return;
        }
        var count = getAirburstDistances(gun).size();
        if (count < 2) {
            return;
        }
        setSelectedDistanceIndex(gun.gunStack(), (getSelectedDistanceIndex(gun.gunStack()) + 1) % count);
    }

    @SubscribeEvent
    public static void onBulletCreate(InternalBulletCreateEvent eventWrapper) {
        var event = eventWrapper.getImpl();
        if (event.getShooter().level().isClientSide) {
            return;
        }
        var gun = event.getGunInfo();
        var selectedDistance = getSelectedDistance(gun);
        if (selectedDistance.isEmpty()) {
            return;
        }

        var finalDistance = selectedDistance.getAsDouble();
        var distribution = getAirburstDistanceDistribution(gun);
        if (distribution > 0) {
            finalDistance = finalDistance * GsHelper.infDist(event.getShooter().getRandom()::nextGaussian, 1, distribution);
        }

        event.getBullet().getPersistentData().putDouble(PDK_AIRBURST_DISTANCE, finalDistance);
    }

    @SubscribeEvent
    public static void onBulletReadyToTrace(BulletReadyToTraceEvent event) {
        var bullet = event.getEntity();
        var pd = bullet.getPersistentData();
        if (bullet.level().isClientSide || !pd.contains(PDK_AIRBURST_DISTANCE)) {
            return;
        }

        var posBefore = event.getStartPos();
        var posAfter = event.getEndPos();

        if (!bullet.isAlive()) {
            return;
        }
        var delta = posBefore.distanceTo(posAfter);
        var newDistance = pd.getDouble(PDK_AIRBURST_DISTANCE) - delta;
        if (newDistance > 0) {
            pd.putDouble(PDK_AIRBURST_DISTANCE, newDistance);
        } else if (bullet instanceof EntityKineticBulletAccessor accessor) {
            GsHelper.syncBulletExplodePos(bullet, posBefore.lerp(posAfter, 1 + newDistance / delta));
            accessor.setExplosionDelayCount(0);
        }
    }
}
