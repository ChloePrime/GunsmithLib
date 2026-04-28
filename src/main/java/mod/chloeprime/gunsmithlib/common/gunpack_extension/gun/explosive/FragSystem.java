package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import cn.chloeprime.commons.client.world.ClientEntityPostSpawnProcessing;
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
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.common.util.LauncherContext;
import mod.chloeprime.gunsmithlib.proxies.ClientProxy;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Marker;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.loading.FMLLoader;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.joml.Vector3d;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.UUID;

@EventBusSubscriber
public class FragSystem {
    private static final GameProfile FAKE_PLAYER_PROFILE = new GameProfile(UUID.fromString("bc6541b7-6ddb-42e8-aa51-764e8d7fc701"), "[Frag Creator]");

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
        int count = data.getCount();
        var launcher = data.getConfigSource().orElse(ItemStack.EMPTY);
        var launcherInfo = Gunsmith.getGunInfo(launcher).orElse(null);
        if (launcherInfo == null) {
            return;
        }
        var launcherId = launcherInfo.gunId();
        var launcherGunData = (GunData)  launcherInfo.index().getGunData();
        var launcherBulletData = (BulletData) launcherInfo.index().getBulletData();
        var launcherAmmoId = Objects.requireNonNullElse(launcherGunData.getAmmoId(), DefaultAssets.EMPTY_AMMO_ID);
        var isTracer = launcherBulletData.getTracerCountInterval() >= 0;
        var rng = shooter.getRandom();
        var hit = event.getHitResult();
        var hitPos = hit.getLocation();
        var fragCreator = setupFragCreator(serverLevel, launcherInfo);
        fragCreator.setPos(hitPos.x(), hitPos.y() - fragCreator.getEyeHeight(), hitPos.z());

        var frags = new int[count];
        var ctx = LauncherContext.STACK.get();
        try {
            ctx.push(gun);
            for (int i = 0; i < count; i++) {
                var direction = randomDirection(rng, hit);
                var velocity = (float) data.sampleFragVelocity(rng);
                fragCreator.lookAt(EntityAnchorArgument.Anchor.EYES, hitPos.add(direction.x(), direction.y(), direction.z()));

                var frag = new EntityKineticBullet(ammo.level(), fragCreator, launcher, launcherAmmoId, launcherId, isTracer, launcherGunData, launcherBulletData);
                frag.setPos(hitPos);
                frag.shoot(direction.x(), direction.y(), direction.z(), velocity, 0);
                level.addFreshEntity(frag);
                frag.setOwner(shooter);
                frags[i] = frag.getId();
            }
        } finally {
            ctx.pop();
        }
        RPC.call(RPCTarget.near(ammo), FragSystem::rpcSetupTracerChairs, hitPos, frags);
    }

    private static Player setupFragCreator(ServerLevel level, GunInfo launcher) {
        var fragCreator = FakePlayerFactory.get(level, FAKE_PLAYER_PROFILE);
        fragCreator.setItemInHand(InteractionHand.MAIN_HAND, launcher.gunStack());
        AttachmentPropertyManager.postChangeEvent(fragCreator, launcher.gunStack());
        return fragCreator;
    }

    private static final Vector3d RANDOM_UNIT_BUFFER = new Vector3d();

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

    private static void sit(Entity chair, @Nonnull Level level, int passenger) {

    }
}
