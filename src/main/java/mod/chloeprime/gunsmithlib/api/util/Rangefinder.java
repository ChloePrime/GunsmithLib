package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.util.EntityUtil;
import mod.chloeprime.gunsmithlib.common.util.RangefinderSupport;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.mutable.MutableDouble;
import org.apache.commons.lang3.mutable.MutableObject;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Stream;

public class Rangefinder {
    public interface Result {
        double getLength();

        default HitResult asHitResult() {
            return Objects.requireNonNull((HitResult) this);
        }
    }

    public static class BlockResult extends BlockHitResult implements Result {
        private final boolean miss;
        private final double length;

        public BlockResult(
                double length, boolean miss,
                Vec3 location, Direction direction, BlockPos pos, boolean inside
        ) {
            super(location, direction, pos, inside);
            this.miss = miss;
            this.length = length;
        }

        public double getLength() {
            return length;
        }

        @Override
        public @Nonnull Type getType() {
            return this.miss ? HitResult.Type.MISS : HitResult.Type.BLOCK;
        }
    }

    public static class EntityResult extends EntityHitResult implements Result {
        private final double length;

        public EntityResult(double length, Entity pEntity, Vec3 pLocation) {
            super(pEntity, pLocation);
            this.length = length;
        }

        public double getLength() {
            return length;
        }
    }

    public static class MissResult extends HitResult implements Result {
        private final double length;

        protected MissResult(double length, Vec3 hitLocation) {
            super(hitLocation);
            this.length = length;
        }

        @Override
        public @Nonnull Type getType() {
            return Type.MISS;
        }

        @Override
        public double getLength() {
            return length;
        }
    }

    public static Result clip(Entity shooter, Vec3 pos, Vec3 direction, int piercing, double range) {
        Vec3 hitLocation;
        double length;

        var ctx = new ClipContext(pos, pos.add(direction.scale(range)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, shooter);
        int entityLimit = piercing + 1;

        var block = shooter.level().clip(ctx);
        var blockLengthSqr = block.getLocation().distanceToSqr(ctx.getFrom());
        var entity = clipEntities(RangefinderSupport.getMarkerForEntityClipping(shooter), shooter, pos, pos.add(direction.scale(Math.sqrt(blockLengthSqr) + 0.125)), entityLimit);

        // 什么也没打中
        if (block.getType() == HitResult.Type.MISS && entity.isEmpty()) {
            length = (float) range;
            hitLocation = pos.add(direction.scale(range));
            return new MissResult(length, hitLocation);
        }
        // 伤害实体并统计数量
        var furthestHit = new MutableObject<EntityHitResult>(null);
        var lengthSqrByEntity = new MutableDouble(0);
        long entityCount = entity.stream().mapToLong(stream -> stream
                .filter(result -> result.getEntity() != shooter)
                .map(result -> new EntityHitResult(result.getEntity(), result.getLocation()))
                .filter(hit -> {
                    var lengthSqr = hit.getLocation().distanceToSqr(ctx.getFrom());
                    // 防止穿墙射击
                    if (lengthSqr > blockLengthSqr + 0.125) {
                        return true;
                    }
                    if (lengthSqr > lengthSqrByEntity.doubleValue()) {
                        lengthSqrByEntity.setValue(lengthSqr);
                        furthestHit.setValue(hit);
                    }
                    return true;
                })
                .count()
        ).findAny().orElse(0);
        // 击中方块
        boolean isBlock;
        if (entityCount < entityLimit && block.getType() != HitResult.Type.MISS) {
            isBlock = true;
            hitLocation = block.getLocation();
        } else {
            isBlock = false;
            hitLocation = Optional.ofNullable(furthestHit.getValue())
                    .map(HitResult::getLocation)
                    .orElse(pos);
        }
        length = hitLocation.distanceTo(ctx.getFrom());
        if (isBlock) {
            return new BlockResult(length, false, hitLocation, block.getDirection(), block.getBlockPos(), block.isInside());
        }
        var hitEntity = Optional.ofNullable(furthestHit.getValue()).map(EntityHitResult::getEntity).orElse(null);
        if (hitEntity != null) {
            return new EntityResult(length, hitEntity, hitLocation);
        }
        return new MissResult(length, hitLocation);
    }

    private static Optional<Stream<EntityHitResult>> clipEntities(Projectile projectile, Entity shooter, Vec3 from, Vec3 to, int limit) {
        projectile.setPos(from);
        projectile.setDeltaMovement(to.subtract(from));
        if (limit == 1) {
            return Optional.ofNullable(EntityUtil.findEntityOnPath(projectile, from, to))
                    .map(Rangefinder::mapResult)
                    .map(Stream::of);
        }
        return Optional.of(EntityUtil.findEntitiesOnPath(projectile, from, to)).map(list -> list.stream()
                // 阻止伤害到自己和自己的坐骑
                .filter(result -> result.getEntity() != shooter && result.getEntity() != shooter.getVehicle())
                // 按距离排序
                .sorted(Comparator.comparing(result -> result.getHitPos().distanceToSqr(from)))
                .limit(limit)
                .map(Rangefinder::mapResult)
        );
    }

    private static EntityHitResult mapResult(EntityKineticBullet.EntityResult result) {
        return new EntityHitResult(result.getEntity(), result.getHitPos());
    }
}
