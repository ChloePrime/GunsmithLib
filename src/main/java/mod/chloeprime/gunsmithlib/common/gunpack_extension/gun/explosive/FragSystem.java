package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.client.world.ClientEntityPostSpawnProcessing;
import cn.chloeprime.commons.lang4.EmptyArrays;
import cn.chloeprime.commons.math.LinearAlgebraTypes;
import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCFlow;
import cn.chloeprime.commons.rpc.RPCTarget;
import cn.chloeprime.commons.rpc.RemoteCallable;
import com.mojang.authlib.GameProfile;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.BulletData;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.GunsmithLibGunProperties;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.common.util.LauncherContext;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2d;
import org.joml.Vector3d;

import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class FragSystem {
    private static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.fromString("bc6541b7-6ddb-42e8-aa51-764e8d7fc701"), "[Frag Creator]");

    @ApiStatus.Internal
    @FunctionalInterface
    public interface FragConstructor {
        Projectile create(Vector3d direction, float velocity);
    }

    @SubscribeEvent
    public static void onAmmoHitBlock(InternalEvent.AmmoHitAnything.Post eventWrapper) {
        var event = eventWrapper.getImpl();
        var ammo = event.getAmmo();
        var shooter = ammo.getOwner() instanceof LivingEntity st ? st : null;
        if (shooter == null) {
            return;
        }
        var level = Objects.requireNonNull(ammo.level());
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        var gun = Gunsmith.getGunInfo(Gunsmith.createGunItemFromId(event.getAmmo().getGunId(), ammo.registryAccess())).orElse(null);
        var data = gun == null ? null : GunExplosiveFragData.of(gun).orElse(null);
        if (data == null) {
            return;
        }
        // 发射破片
        var launcher = data.getConfigSource().orElse(ItemStack.EMPTY);
        var launcherInfo = Gunsmith.getGunInfo(launcher).orElse(null);
        if (launcherInfo == null) {
            return;
        }
        int dataCount = GsHelper.modifyProperty(gun, shooter, GunsmithLibGunProperties.FRAG_COUNT, Integer.class, data.getCount());
        var launcherId = launcherInfo.gunId();
        var launcherGunData = (GunData)  launcherInfo.index().getGunData();
        var launcherBulletData = (BulletData) launcherInfo.index().getBulletData();
        var launcherAmmoId = Objects.requireNonNullElse(launcherGunData.getAmmoId(), DefaultAssets.EMPTY_AMMO_ID);
        var isTracer = launcherBulletData.getTracerCountInterval() >= 0;
        var distribution = data.getDistribution();
        var rng = shooter.getRandom();
        var hit = event.getHitResult();
        var hitPos = hit.getLocation();
        var fragCreator = setupFragCreator(serverLevel, launcherInfo);
        fragCreator.setPos(hitPos.x(), hitPos.y() - fragCreator.getEyeHeight(), hitPos.z());

        var fragConstructor = (FragConstructor) (direction, velocity) -> {
            var frag = new EntityKineticBullet(ammo.level(), fragCreator, launcher, launcherAmmoId, launcherId, isTracer, launcherGunData, launcherBulletData);
            frag.setPos(hitPos);
            frag.shoot(direction.x(), direction.y(), direction.z(), velocity, 0);
            level.addFreshEntity(frag);
            frag.setOwner(shooter);
            return frag;
        };
        int[] frags;
        var ctx = LauncherContext.STACK.get();
        try {
            ctx.push(gun);
            switch (distribution) {
                case RANDOM -> {
                    var isBlockHit = hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult;
                    // 模拟另一半弹片直接卡进方块的情形。
                    // 可以在命中方块时节省一般性能，同时保证空爆和命中方块拥有一样的弹片密度期望。
                    int count = dataCount / (isBlockHit ? 2 : 1);
                    frags = new int[count];
                    for (int i = 0; i < count; i++) {
                        var direction = randomDirection(rng, hit);
                        if (isBlockHit) {
                            blockHitFaceFix(direction, ((BlockHitResult) hit).getDirection());
                        }
                        var velocity = (float) data.sampleFragVelocity(rng);
                        frags[i] = fragConstructor.create(direction, velocity).getId();
                    }
                }
                case UNIFORM -> {
                    if (hit.getType() == HitResult.Type.BLOCK && hit instanceof BlockHitResult blockHit) {
                        var normal = LinearAlgebraTypes.cast(blockHit.getDirection().getNormal());
                        var rot = fwd2rot(normal, ROTATION_BUFFER);
                        var count = FragDistribution.computeUniformModeCount(dataCount / 2, true);
                        var step = count <= 1 ? Math.PI : Math.PI / (count - 1);
                        double saltP = (rng.nextDouble() - rng.nextDouble()) / 2;
                        double saltY = (rng.nextDouble() - rng.nextDouble()) / 2;
                        var baseP = rot.x() - Math.PI / 2;
                        var baseY = -rot.y();

                        frags = new int[count * count];
                        for (int pi = 0; pi < count; pi++) {
                            var pitch = baseP + (pi + saltP) * step;
                            for (int yi = 0; yi < count; yi++) {
                                var yaw = baseY + (yi + saltY) * step;
                                var fwd = rot2fwd(pitch, yaw, FORWARD_BUFFER);
                                var i = pi * count + yi;
                                var velocity = (float) data.sampleFragVelocity(rng);
                                frags[i] = fragConstructor.create(fwd, velocity).getId();
                            }
                        }
                    } else {
                        int countP = FragDistribution.computeUniformModeCount(dataCount, false);
                        int countY = countP * 2;
                        double stepP = Math.PI / countP;
                        double stepY = 2 * Math.PI / countY;
                        double saltP = rng.nextDouble() - 0.5;
                        double saltY = rng.nextDouble() - 0.5;
                        var baseP = -Math.PI / 2;
                        var baseY = -Math.PI;

                        frags = new int[countP * countY];
                        for (int pi = 0; pi < countP; pi++) {
                            var pitch = baseP + (pi + saltP) * stepP;
                            for (int yi = 0; yi < countY; yi++) {
                                var yaw = baseY + (yi + saltY) * stepY;
                                var fwd = rot2fwd(pitch, yaw, FORWARD_BUFFER);
                                var i = pi * countY + yi;
                                var velocity = (float) data.sampleFragVelocity(rng);
                                frags[i] = fragConstructor.create(fwd, velocity).getId();
                            }
                        }
                    }
                }
                default -> frags = EmptyArrays.INT;
            }
        } finally {
            ctx.pop();
        }
        RPC.call(RPCTarget.near(ammo), FragSystem::rpcSetupTracerChairs, hitPos, frags);
    }

    private static Vector2d fwd2rot(Vec3 fwd, @SuppressWarnings("SameParameterValue") Vector2d dst) {
        double x = fwd.x;
        double y = fwd.y;
        double z = fwd.z;
        double xz = Math.sqrt(x * x + z * z);
        var rx = Mth.atan2(y, xz);
        var ry = Mth.atan2(z, x);
        dst.set(rx, ry);
        return dst;
    }

    private static Vector3d rot2fwd(double pitch, double yaw, @SuppressWarnings("SameParameterValue") Vector3d dst) {
        double cosP = Math.cos(pitch);
        double sinP = Math.sin(pitch);
        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        dst.set(sinY * cosP, sinP, cosY * cosP);
        return dst;
    }

    private static Player setupFragCreator(ServerLevel level, GunInfo launcher) {
        var fragCreator = FakePlayerFactory.get(level, FAKE_PLAYER_PROFILE);
        fragCreator.setItemInHand(InteractionHand.MAIN_HAND, launcher.gunStack());
        AttachmentPropertyManager.postChangeEvent(fragCreator, launcher.gunStack());
        return fragCreator;
    }

    private static final Vector3d RANDOM_UNIT_BUFFER = new Vector3d();
    private static final Vector2d ROTATION_BUFFER = new Vector2d();
    private static final Vector3d FORWARD_BUFFER = new Vector3d();

    private static Vector3d randomDirection(RandomSource rng, HitResult hit) {
        RANDOM_UNIT_BUFFER.x = rng.nextGaussian();
        RANDOM_UNIT_BUFFER.y = rng.nextGaussian();
        RANDOM_UNIT_BUFFER.z = rng.nextGaussian();
        RANDOM_UNIT_BUFFER.normalize();
        if (hit instanceof BlockHitResult blockHit) {
            var dir = blockHit.getDirection();
            switch (dir.getAxis()) {
                case X -> RANDOM_UNIT_BUFFER.x = Math.abs(RANDOM_UNIT_BUFFER.x) * dir.getAxisDirection().getStep();
                case Y -> RANDOM_UNIT_BUFFER.y = Math.abs(RANDOM_UNIT_BUFFER.y) * dir.getAxisDirection().getStep();
                case Z -> RANDOM_UNIT_BUFFER.z = Math.abs(RANDOM_UNIT_BUFFER.z) * dir.getAxisDirection().getStep();
            }
        }
        return RANDOM_UNIT_BUFFER;
    }

    private static void blockHitFaceFix(Vector3d vec, Direction dir) {
        switch (dir.getAxis()) {
            case X -> vec.x = Math.abs(vec.x) * dir.getAxisDirection().getStep();
            case Y -> vec.y = Math.abs(vec.y) * dir.getAxisDirection().getStep();
            case Z -> vec.z = Math.abs(vec.z) * dir.getAxisDirection().getStep();
        }
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void rpcSetupTracerChairs(Vec3 center, int[] frags) {
        if (frags.length == 0) {
            return;
        }
        var level = ClientProxy.clientLevel().orElse(null);
        if (level == null) {
            return;
        }
        var chair = new Marker(EntityType.MARKER, level);
        chair.lookAt(EntityAnchorArgument.Anchor.FEET, chair.position().add(0, 1, 0));
        chair.setPos(center);
        ClientEntityPostSpawnProcessing.process(frags, frag -> setupTracerChair(chair, frag));
    }

    private static void setupTracerChair(Entity chair, Entity passenger) {
        if (passenger instanceof Projectile frag) {
            frag.setOwner(chair);
        } else if (!FMLLoader.isProduction()) {
            var id = passenger == null ? null : passenger.getId();
            var type = passenger == null ? null : passenger.getClass().getSimpleName();
            GunsmithLib.LOGGER.warn("[{}] Entity {} ({}) is not a projectile", FragSystem.class.getSimpleName(), id, type);
        }
    }
}
