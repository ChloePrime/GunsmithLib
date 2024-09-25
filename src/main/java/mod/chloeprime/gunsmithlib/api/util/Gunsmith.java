package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static mod.chloeprime.gunsmithlib.proxies.ClientProxy.*;

public class Gunsmith {
    public static Optional<GunInfo> getGunInfo(ItemStack gun) {
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return Optional.empty();
        }
        var gunId = kun.getGunId(gun);
        return TimelessAPI.getCommonGunIndex(gunId).map(index -> new GunInfo(gun, kun, gunId, index));
    }

    public static Vec3 getProximityMuzzlePos(LivingEntity shooter) {
        var op = IGunOperator.fromLivingEntity(shooter);
        var adsProgress = op.getSynAimingProgress();

        var axisZ = shooter.getLookAngle();
        var axisX = axisZ.cross(UP);
        var axisY = axisX.cross(axisZ);

        var x = Mth.lerp(adsProgress,0.06F, 0);
        var y = Mth.lerp(adsProgress, -0.08F, hasScope(shooter) ? -0.2F : 0);
        var z = Mth.lerp(adsProgress, 0.8F, 0.6F);

        var offset = bobCompensation(sideOf(shooter.level()), new Vec3(x, y, z));
        return shooter.getEyePosition().add(axisX.scale(offset.x).add(axisY.scale(offset.y)).add(axisZ.scale(offset.z)));
    }

    private static final double EYE_TO_HAND_X = 6.0 / 16;
    private static final double EYE_TO_HAND_Y = 5.0 / 16;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private static boolean hasScope(LivingEntity shooter) {
        var info = getGunInfo(shooter.getMainHandItem()).orElse(null);
        if (info == null) {
            return false;
        }
        return !info.gunItem().getAttachment(info.gunStack(), AttachmentType.SCOPE).isEmpty();
    }

}
