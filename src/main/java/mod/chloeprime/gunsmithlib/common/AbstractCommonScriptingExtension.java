package mod.chloeprime.gunsmithlib.common;

import cn.chloeprime.commons.async.TaskScheduler;
import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.common.CommonScriptingExtension;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ChargeableTriggerSystem;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.LogicalSide;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaInteger;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

import java.util.Optional;
import java.util.function.BooleanSupplier;

@ApiStatus.Internal
public interface AbstractCommonScriptingExtension extends CommonScriptingExtension {
    ItemStack gunsmithlib$getCurrentItem();
    IGun gunsmithlib$getGunItemInterface();
    Optional<LivingEntity> gunsmithlib$getShooter();
    LogicalSide gunsmithlib$getSide();

    default String gunsmith$getGunIdHelper() {
        var gunInterface = gunsmithlib$getGunItemInterface();
        return gunInterface == null ? "" : gunInterface.getGunId(gunsmithlib$getCurrentItem()).toString();
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    default double gunsmith_getEstimatedRange() {
        int pierce = gunsmithlib$getShooter()
                .map(IGunOperator::fromLivingEntity)
                .map(IGunOperator::getCacheProperty)
                .map(cache -> cache.getCache(GunProperties.PIERCE))
                .orElse(0);
        return gunsmith_getEstimatedRange(pierce);
    }

    @Override
    default double gunsmith_getEstimatedRange(int pierce) {
        Entity shooter = gunsmithlib$getShooter().orElse(null);
        if (shooter == null) {
            return 0;
        }

        ItemStack gunStack = gunsmithlib$getCurrentItem();
        IGun gunItem = gunsmithlib$getGunItemInterface();
        if (gunStack == null || gunItem == null) {
            return 0;
        }

        double range = GsHelper.getEstimatedMaxRange(shooter, gunStack, gunItem);
        return Rangefinder.clip(shooter, shooter.getEyePosition(), shooter.getLookAngle(), pierce, range).getLength();
    }

    @Override
    default double gunsmith_getChargingTime() {
        var shooter = gunsmithlib$getShooter().orElse(null);
        var gunStack = gunsmithlib$getCurrentItem();
        if (shooter == null || gunStack == null) {
            return 0;
        }
        return ChargeableTriggerSystem.getChargeTime(gunStack, shooter.level().getGameTime()) / 20.0;
    }

    @Override
    default void gunsmith_asyncRunDelayed(LuaValue value, int delayTicks) {
        var gunId = gunsmithlib$getGunItemInterface().getGunId(gunsmithlib$getCurrentItem());
        var luaFunc = value.checkfunction();
        AsyncHelper.of(gunsmithlib$getSide())
                .withCondition(AsyncHelper.isHoldingCurrentWeapon(this, gunId))
                .delay(delayTicks).thenRun(() -> luaFunc.call(CoerceJavaToLua.coerce(this)));
    }

    @Override
    default void gunsmith_asyncRunCycled(LuaValue value, int period, int count) {
        var gunId = gunsmithlib$getGunItemInterface().getGunId(gunsmithlib$getCurrentItem());
        var luaFunc = value.checkfunction();
        int[] counter = {0};
        AsyncHelper.of(gunsmithlib$getSide())
                .withCondition(AsyncHelper.isHoldingCurrentWeapon(this, gunId))
                .countdown(period, count, task -> {
                    if (!luaFunc.call(CoerceJavaToLua.coerce(this), LuaInteger.valueOf(counter[0])).checkboolean()) {
                        task.stop();
                    } else {
                        counter[0]++;
                    }
                });
    }

    class AsyncHelper {
        private static BooleanSupplier isHoldingCurrentWeapon(
                AbstractCommonScriptingExtension api,
                ResourceLocation gunId
        ) {
            var shooter = api.gunsmithlib$getShooter().orElse(null);
            if (shooter == null) {
                return () -> false;
            }
            return () -> shooter.isAlive() && Gunsmith.getGunInfo(shooter.getMainHandItem())
                    .map(GunInfo::gunId)
                    .filter(gunId::equals)
                    .isPresent();
        }

        private static final TaskScheduler S = TaskScheduler.createTickBased(LogicalSide.SERVER);
        private static final TaskScheduler C = TaskScheduler.createTickBased(LogicalSide.CLIENT);

        public static TaskScheduler of(LogicalSide side) {
            return side.isClient() ? C : S;
        }
    }
}
