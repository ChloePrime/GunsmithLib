package mod.chloeprime.gunsmithlib.client.gunpack_extension.screen_shake;

import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake.ScreenShakeData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake.ScreenShakeEntryData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.screen_shake.ScreenShakeType;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import mod.chloeprime.gunsmithlib.mixin.client.minecraft.CameraAccessor;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.ApiStatus;
import rbasamoyai.ritchiesprojectilelib.effects.screen_shake.ScreenShakeEffect;

import java.util.Optional;

/**
 * @since 7.0.0
 */
@Mod.EventBusSubscriber(Dist.CLIENT)
public final class ScreenShakeSystemClient {
    public static void applyScreenShakeAt(Vec3 pos, LivingEntity shooter, ResourceLocation gunId, ScreenShakeType.Field field) {
        var cameraPos = Optional.ofNullable(Minecraft.getInstance().cameraEntity)
                .or(() -> Optional.ofNullable(Minecraft.getInstance().player))
                .map(Entity::getEyePosition)
                .orElse(null);
        if (cameraPos == null) {
            return;
        }
        var config = GsHelper.gunInfoFromShooter(shooter, gunId)
                .flatMap(ScreenShakeData::of)
                .flatMap(field)
                .orElse(null);
        if (config == null) {
            return;
        }
        var handler = ScreenShakeHandlers.next();
        var power = (float) (getPower(config, pos.distanceToSqr(cameraPos)) * config.getIntensity());
        handler.bindData(config);
        handler.addEffect(new ScreenShakeEffect(-1,
                        (float) config.getYawMagnitude() * power, (float) config.getPitchMagnitude() * power, (float) config.getRollMagnitude() * power,
                        0, 0, 0,
                        pos.x(), pos.y(), pos.z()),
                config.getTranslationMagnitudeX() * power, config.getTranslationMagnitudeY() * power, config.getTranslationMagnitudeZ() * power);
    }

    private static double getPower(ScreenShakeEntryData data, double distanceSqr) {
        var range = data.getRange();
        if (range == 0) {
            return 0;
        }
        var f = Math.max(1 - distanceSqr / (range * range), 0);
        return Math.min(45, f * data.getBasePower());
    }

    @SubscribeEvent
    public static void moveCamera(ViewportEvent.ComputeCameraAngles event) {
        var camera = (CameraAccessor) event.getCamera();
        ScreenShakeHandlers.all().forEach(handler -> handler.applyTranslateEffects((x, y, z) -> camera.invokeMove(z, y, x)));
    }

    @ApiStatus.Internal
    public static void init() {
        ScreenShakeHandlers.init();
    }

    private ScreenShakeSystemClient() {
    }
}
