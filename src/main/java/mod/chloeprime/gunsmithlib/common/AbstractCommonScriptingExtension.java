package mod.chloeprime.gunsmithlib.common;

import com.tacz.guns.api.GunProperties;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.api.common.CommonScriptingExtension;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ChargeableTriggerSystem;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.ApiStatus;

import java.util.Optional;

@ApiStatus.Internal
public interface AbstractCommonScriptingExtension extends CommonScriptingExtension {
    ItemStack gunsmithlib$getCurrentItem();
    IGun gunsmithlib$getGunItemInterface();
    Optional<LivingEntity> gunsmithlib$getShooter();

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
}
