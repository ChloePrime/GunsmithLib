package mod.chloeprime.gunsmithlib.proxies;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import mod.chloeprime.gunsmithlib.mixin.LevelAccessor;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.core.RegistryAccess;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddReloadListenerEvent;
import org.joml.Matrix4f;
import org.joml.Vector4f;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

@EventBusSubscriber(Dist.CLIENT)
class ClientProxyImpl {
    private static final Minecraft MC = Minecraft.getInstance();
    private static final PoseStack POSE = new PoseStack();
    private static RegistryAccess fallbackRegistryAccess;

    static float getPartialTick() {
        return MC.getTimer().getGameTimeDeltaPartialTick(false);
    }

    public static Optional<RegistryAccess> getRegistryAccess() {
        var result = Optional.ofNullable(MC.level).map(Level::registryAccess).orElse(fallbackRegistryAccess);
        return Optional.ofNullable(result);
    }

    @SubscribeEvent
    private static void updateFallbackRegistryAccessOnResourceReload(AddReloadListenerEvent event) {
        fallbackRegistryAccess = event.getRegistryAccess();
    }

    static Vec3 bobCompensation(Vec3 original) {
        if (MC.options.getCameraType() != CameraType.FIRST_PERSON || !MC.options.bobView().get()) {
            return original;
        }
        var player = MC.player;
        if (player == null) {
            return original;
        }

        var pPartialTicks = getPartialTick();
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
}
