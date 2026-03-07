package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.rpc.*;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.server.AmmoHitBlockEvent;
import com.tacz.guns.entity.EntityKineticBullet;
import it.unimi.dsi.fastutil.doubles.DoubleList;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.internal.BulletReadyToTraceEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import mod.chloeprime.gunsmithlib.mixin.EntityKineticBulletAccessor;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.NeoForge;

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
    public static final Component MSG_TOO_FAR = Component
            .translatable("%s.message.airburst_rangefinder.too_far".formatted(GunsmithLib.MOD_ID))
            .withStyle(ChatFormatting.RED);

    public static int getSelectedDistanceIndex(ItemStack stack) {
        return Objects.requireNonNullElse(stack.get(GunsmithLib.DataComponents.SELECTED_AIRBURST_DISTANCE_INDEX), 0);
    }

    public static OptionalDouble getSelectedDistance(GunInfo gun) {
        var custom = getAirburstRangefinderStoredDistance(gun.gunStack());
        if (custom > 0) {
            return OptionalDouble.of(custom);
        }
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

    /**
     * 获取弹道计算机测距上限
     *
     * @since 4.10.0
     */
    public static @Nonnull OptionalDouble getAirburstRangefinderMaxDistance(GunInfo gun) {
        return GunExplosiveData.fromGun(gun)
                .map(GunExplosiveData::getAirburstRangefinderMaxDistance)
                .orElse(OptionalDouble.empty());
    }

    /**
     * 获取弹道计算机存入的空爆距离
     *
     * @since 4.10.0
     */
    public static double getAirburstRangefinderStoredDistance(ItemStack stack) {
        var isSupported = GunExplosiveData
                .fromGun(stack)
                .filter(explosive -> explosive.getAirburstRangefinderMaxDistance().orElse(0) > 0)
                .isPresent();
        if (!isSupported) {
            return 0;
        }
        return Objects.requireNonNullElse(stack.get(GunsmithLib.DataComponents.CUSTOM_AIRBURST_DISTANCE), 0.0);
    }

    /**
     * 设置弹道计算机存入的空爆距离
     *
     * @since 4.10.0
     */
    public static void setAirburstRangefinderStoredDistance(ItemStack stack, double value) {
        stack.set(GunsmithLib.DataComponents.CUSTOM_AIRBURST_DISTANCE, value);
    }

    /**
     * 清除弹道计算机存入的空爆距离
     *
     * @since 4.10.0
     */
    public static boolean clearAirburstRangefinderStoredDistance(ItemStack stack) {
        return stack.remove(GunsmithLib.DataComponents.CUSTOM_AIRBURST_DISTANCE) != null;
    }

    public static double getAirburstDistanceDistribution(GunInfo gun) {
        return GunExplosiveData.fromGun(gun)
                .map(GunExplosiveData::getAirburstDistancesDistribution)
                .map(OptionalDouble::of)
                .orElse(OptionalDouble.empty())
                .orElse(0);
    }

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER, callLocally = true)
    public static void onSelectAirburstIndex() {
        var user = RPCContext.isCalledThroughRPC() ? RPCContext.getSenderPlayer() : null;
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

    @RemoteCallable
    public static void onBallisticComputerPressed(Player user) {
        if (user == null || user.level().isClientSide || !user.isAlive()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(user.getMainHandItem()).orElse(null);
        if (gun == null) {
            return;
        }
        var maxDistance = AirburstSystem.getAirburstRangefinderMaxDistance(gun).orElse(-1);
        if (maxDistance <= 0) {
            return;
        }
        var operator = IGunOperator.fromLivingEntity(user);
        if (operator.getSynIsAiming()) {
            // 进行测距并存入结果
            var result = Rangefinder.clip(user, user.getEyePosition(), user.getLookAngle(), 0, maxDistance);
            if (result.asHitResult().getType() != HitResult.Type.MISS) {
                setAirburstRangefinderStoredDistance(gun.gunStack(), result.getLength());
                rangefinderFeedback(user);
            } else {
                user.displayClientMessage(MSG_TOO_FAR, true);
            }
        } else {
            // 未瞄准时按住中键则清除弹道计算机测距结果
            if (clearAirburstRangefinderStoredDistance(gun.gunStack())) {
                rangefinderFeedback(user);
            }
        }
    }

    private static void rangefinderFeedback(Player user) {
        if (user instanceof ServerPlayer ssp) {
            RPC.call(RPCTarget.to(ssp), AirburstSystem::rangefinderFeedback);
        } else {
            rangefinderFeedback();
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void rangefinderFeedback() {
        GunsmithLibClient.playComputerButtonSound();
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
            var explodePos = posBefore.lerp(posAfter, 1 + newDistance / delta);
            // 发布 AmmoHitBlockEvent 事件以触发命中粒子效果
            boolean canceled;
            if (bullet instanceof EntityKineticBullet ekb) {
                var dir = posAfter.subtract(posBefore);
                var hit = new BlockHitResult(explodePos, Direction.getNearest(dir.x(), dir.y(), dir.z()), BlockPos.containing(explodePos), true);
                canceled = NeoForge.EVENT_BUS.post(new AmmoHitBlockEvent(bullet.level(), hit, Blocks.AIR.defaultBlockState(), ekb)).isCanceled();
            } else {
                canceled = false;
            }
            // 爆炸！
            if (!canceled) {
                GsHelper.syncBulletExplodePos(bullet, explodePos);
                accessor.setExplosionDelayCount(0);
            }
        }
    }

    private AirburstSystem() {
    }
}
