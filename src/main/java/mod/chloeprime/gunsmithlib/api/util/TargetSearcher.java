package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.resource.modifier.custom.EffectiveRangeModifier;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlAttributes;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.tuple.Pair;

import javax.annotation.Nullable;
import java.util.Comparator;
import java.util.Optional;

import static mod.chloeprime.gunsmithlib.common.util.GsHelper.getAttributeValueWithBase;

public class TargetSearcher {
    public static final double MAX_DISTANCE = 64 * 16;
    public record SearchResult(
            Entity entity,
            Vec3 pos
    ) {
    }

    public static Optional<SearchResult> search(LivingEntity shooter, GunInfo gun, float partialTicks) {
        @Nullable FireControlData data = FireControlData.fromGun(gun).orElse(null);
        var propCache = IGunOperator.fromLivingEntity(shooter).getCacheProperty();
        if (propCache == null) {
            return Optional.empty();
        }
        var baseRange = (data != null && data.getRangeOverride() >= 0)
                ? data.getRangeOverride()
                : propCache.getCache(EffectiveRangeModifier.ID) instanceof Number effRange ? effRange.doubleValue() : -1;
        if (baseRange <= 0) {
            return Optional.empty();
        }
        var baseAngularRange = Math.max(0, data != null ? data.getAngularRange() : getOldAimConeSizeOfGun(gun));
        var angularRange = getAttributeValueWithBase(shooter, FireControlAttributes.AIM_LOCK_ANGLE, baseAngularRange);
        if (angularRange < 0.5) {
            return Optional.empty();
        }
        var modifiedRange = getAttributeValueWithBase(shooter, FireControlAttributes.AIM_LOCK_RANGE, baseRange);
        return search(shooter, Math.min(modifiedRange, MAX_DISTANCE), Math.toRadians(angularRange / 2), partialTicks);
    }

    @Deprecated
    @SuppressWarnings("DeprecatedIsStillUsed")
    private static double getOldAimConeSizeOfGun(GunInfo gun) {
        var oldFireControlData = ((EnhancedGunData) gun.index().getGunData()).gunsmith$getOldFireControlSystemData().orElse(null);
        if (oldFireControlData != null) {
            return oldFireControlData.aimConeAngle();
        }
        return 0;
    }

    /**
     * @param maxAngle 最大锁定角度，应该等于角范围的一半，且单位为弧度
     */
    public static Optional<SearchResult> search(LivingEntity shooter, double range, double maxAngle, float partialTicks) {
        var lookAngle = shooter.getViewVector(partialTicks);
        var muzzle = shooter.getEyePosition(partialTicks);
        var testAreaAabb = shooter.getBoundingBox().inflate(range + 2);
        var cosConeAngle = Math.cos(maxAngle);
        var rangeSqr = range * range;
        var candidates = shooter.level().getEntities(EntityTypeTest.forClass(LivingEntity.class), testAreaAabb, candidate -> candidate != shooter && shooter.canAttack(candidate));
        return candidates.stream()
                .flatMap(entity -> getEstimatedHitPos(shooter, entity, partialTicks).map(pos -> new SearchResult(entity, pos)).stream())
                // 距离 <= 范围
                .filter(record -> record.pos().distanceToSqr(muzzle) <= rangeSqr)
                // 计算夹角
                .map(record -> {
                    var offset = record.pos().subtract(muzzle);
                    if (offset.lengthSqr() < 1e-6) {
                        return Pair.of(record, (Double)null);
                    }
                    var cos = offset.dot(lookAngle) / (offset.length() * 1/*lookAngle.length()*/);
                    return Pair.of(record, cos);
                })
                // 夹角 <= 自瞄范围
                .filter(pair -> pair.getRight() != null && pair.getRight() >= cosConeAngle)
                .min(Comparator.comparingDouble(pair -> getAimPriority(pair.getLeft().pos(), pair.getRight(), muzzle, range, maxAngle)))
                .map(Pair::getLeft);
    }

    public static Optional<Vec3> getEstimatedHitPos(LivingEntity shooter, Entity target, float partialTicks) {
        if (!(target instanceof LivingEntity)) {
            return getEstimatedHitPosForNonHumanoidTarget(shooter, target, partialTicks);
        } else {
            var targetBb = target.getBoundingBox();
            var maybeHumanoid = target.getPose() == Pose.STANDING && targetBb.getYsize() > Math.max(targetBb.getXsize(), targetBb.getZsize());
            if (!maybeHumanoid) {
                return getEstimatedHitPosForNonHumanoidTarget(shooter, target, partialTicks);
            }
            var footPos = target.getPosition(partialTicks);
            var eyePos = target.getEyePosition(partialTicks);
            for (var candidatePos : new Vec3[] {eyePos, eyePos.add(footPos).scale(0.5), footPos}) {
                if (hasLineOfSight(shooter, candidatePos, partialTicks)) {
                    return Optional.of(candidatePos);
                }
            }
            return Optional.empty();
        }
    }

    private static double getAimPriority(Vec3 targetPos, double cosAngle, Vec3 muzzle, double range, double aimConeAngle) {
        var distanceRatio = targetPos.distanceTo(muzzle) / range;
        var angleRatio = Math.acos(cosAngle) / aimConeAngle;
        return (distanceRatio * distanceRatio * 0.5 + angleRatio * angleRatio);
    }

    private static Optional<Vec3> getEstimatedHitPosForNonHumanoidTarget(LivingEntity shooter, Entity target, float partialTicks) {
        var position = target.getEyePosition(partialTicks);
        return hasLineOfSight(shooter, position, partialTicks)
                ? Optional.of(position)
                : Optional.empty();
    }

    private static boolean hasLineOfSight(LivingEntity shooter, Vec3 point, float partialTicks) {
        var start = shooter.getEyePosition(partialTicks);
        return shooter.level().clip(new ClipContext(start, point, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter)).getType() == HitResult.Type.MISS;
    }
}
