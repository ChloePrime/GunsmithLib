package mod.chloeprime.gunsmithlib.proxies;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.chloeprime.gunsmithlib.mixin.LevelAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

class ClientProxyImpl {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final PoseStack POSE = new PoseStack();

    static Optional<Level> clientLevel() {
        return Optional.ofNullable(MC.level);
    }

    static Vec3 bobCompensation(Vec3 original) {
        if (MC.options.getCameraType() != CameraType.FIRST_PERSON || !MC.options.bobView().get()) {
            return original;
        }
        var player = MC.player;
        if (player == null) {
            return original;
        }

        var pPartialTicks = MC.getPartialTick();
        float f = player.walkDist - player.walkDistO;
        float f1 = -(player.walkDist + f * pPartialTicks);
        float f2 = Mth.lerp(pPartialTicks, player.oBob, player.bob);
        Matrix4f pose;
        POSE.pushPose();
        {
            POSE.translate(Mth.sin(f1 * (float) Math.PI) * f2 * 0.5F, -Math.abs(Mth.cos(f1 * (float) Math.PI) * f2), 0.0F);
            POSE.mulPose(Axis.ZP.rotationDegrees(Mth.sin(f1 * (float) Math.PI) * f2 * 3.0F));
            POSE.mulPose(Axis.XP.rotationDegrees(Math.abs(Mth.cos(f1 * (float) Math.PI - 0.2F) * f2) * 5.0F));
            pose = POSE.last().pose();
        }
        POSE.popPose();

        var affineVec = new Vector4f((float) original.x, (float) original.y, (float) original.z, 1);
        var transformed = pose.transform(affineVec);
        if (transformed.w == 0) {
            return original;
        }
        transformed.div(transformed.w);
        return new Vec3(transformed.x, transformed.y, transformed.z);
    }

    static @Nullable Entity getEntityByUuid(Level level, UUID uuid) {
        return ((LevelAccessor) level).invokeGetEntities().get(uuid);
    }

    static void receiveNoParticleExplodePacket(Vec3 pos, float power, BlockPos[] toBlow, Vec3 knockback) {
        var level = MC.level;
        var player = MC.player;
        if (level == null || player == null) {
            return;
        }
        Explosion explosion = new Explosion(level, null, pos.x(), pos.y(), pos.z(), power, Arrays.asList(toBlow));
        explosion.finalizeExplosion(false);
        player.setDeltaMovement(player.getDeltaMovement().add(knockback.x(), knockback.y(), knockback.z()));
    }

    public static long getGameTime() {
        return Optional.ofNullable(MC.level)
                .map(Level::getGameTime)
                .orElse(0L);
    }

    public static float getPartialTicks() {
        return MC.getPartialTick();
    }
}
