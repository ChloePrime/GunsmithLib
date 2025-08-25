package mod.chloeprime.gunsmithlib.client.laser;

import com.mojang.blaze3d.vertex.PoseStack;
import mod.chloeprime.gunsmithlib.common.util.InternalBulletCreateEvent;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.loading.FMLLoader;

import java.util.Deque;
import java.util.LinkedList;

@EventBusSubscriber(Dist.CLIENT)
public class LaserManager {
    private static final Deque<LaserInstance> INSTANCES = new LinkedList<>();
    private static final boolean DEV_ENV = !FMLLoader.isProduction();
    private static final boolean ENABLED = false;

    @SubscribeEvent
    public static void onClientShoot(InternalBulletCreateEvent eventWrapper) {
        if (!DEV_ENV || !ENABLED) {
            return;
        }
        var event = eventWrapper.getImpl();
        var bullet = event.getBullet();
        if (!bullet.level().isClientSide()) {
            return;
        }
        Vec3 bulletVelocity = bullet.getDeltaMovement();
        double bulletSpeed = bulletVelocity.length();
        if (bulletSpeed == 0) {
            return;
        }
        MagicLaserUtils
                .clip(LaserType.PULSE, event.getShooter(), event.getShooter().getEyePosition(), bulletVelocity.scale(1 / bulletSpeed), bulletSpeed * 50)
                .ifPresent(INSTANCES::add);
    }

    @SubscribeEvent
    public static void onRenderEntityEnd(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_ENTITIES) {
            return;
        }
        var now = System.nanoTime();
        for (var iterator = INSTANCES.iterator(); iterator.hasNext(); ) {
            LaserInstance instance = iterator.next();
            if ((now - instance.localSpawnTime) * MagicLaserUtils.NANO_TO_SECOND >= 0.5) {
                iterator.remove();
            } else {
                if (instance.type == LaserType.CONTINUOUS) {
//                    instance.refresh(event.getPartialTick());
                }
                PoseStack pose = event.getPoseStack();
                pose.pushPose();
                {
                    MagicLaserUtils.stickLaserToMuzzle(instance, event.getPartialTick().getGameTimeDeltaPartialTick(false), pose);
                    MagicLaserUtils.render(instance, event.getCamera(), event.getPartialTick().getGameTimeDeltaPartialTick(false), pose);
                }
                pose.popPose();
            }
        }
    }
}
