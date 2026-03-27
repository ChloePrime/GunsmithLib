package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive;

import com.mojang.authlib.GameProfile;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.entity.EntityKineticBullet;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.internal.InternalEvent;
import mod.chloeprime.gunsmithlib.common.util.LauncherContext;
import net.minecraft.commands.arguments.EntityAnchorArgument;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.joml.Vector3d;

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
        var launcherGunData = launcherInfo.index().getGunData();
        var launcherBulletData = launcherInfo.index().getBulletData();
        var launcherAmmoId = Objects.requireNonNullElse(launcherGunData.getAmmoId(), DefaultAssets.EMPTY_AMMO_ID);
        var isTracer = launcherBulletData.getTracerCountInterval() >= 0;
        var rng = shooter.getRandom();
        var hit = event.getHitResult();
        var hitPos = hit.getLocation();
        var fragCreator = setupFragCreator(serverLevel, launcherInfo);
        fragCreator.setPos(hitPos.x(), hitPos.y() - fragCreator.getEyeHeight(), hitPos.z());

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
            }
        } finally {
            ctx.pop();
        }
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
}
