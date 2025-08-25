package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.api.item.IAttachment;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.attachment.AttachmentType;
import mod.chloeprime.gunsmithlib.common.internal.MagicReloadImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

import static mod.chloeprime.gunsmithlib.proxies.ClientProxy.*;

public class Gunsmith {
    /**
     * @since 1.0.0
     */
    public static Optional<GunInfo> getGunInfo(ItemStack gun) {
        var kun = IGun.getIGunOrNull(gun);
        if (kun == null) {
            return Optional.empty();
        }
        var gunId = kun.getGunId(gun);
        return TimelessAPI.getCommonGunIndex(gunId).map(index -> new GunInfo(gun, kun, gunId, index));
    }

    /**
     * @since 3.4.0
     */
    public static Optional<AmmoInfo> getAmmoInfo(ItemStack ammo) {
        var ait = IAmmo.getIAmmoOrNull(ammo);
        if (ait == null) {
            return Optional.empty();
        }
        var aid = ait.getAmmoId(ammo);
        return TimelessAPI.getCommonAmmoIndex(aid).map(index -> new AmmoInfo(ammo, ait, aid, index));
    }

    /**
     * @since 3.4.0
     */
    public static Optional<AttachmentInfo> getAttachmentInfo(ItemStack attachment) {
        var ait = IAttachment.getIAttachmentOrNull(attachment);
        if (ait == null) {
            return Optional.empty();
        }
        var aid = ait.getAttachmentId(attachment);
        return TimelessAPI.getCommonAttachmentIndex(aid).map(index -> new AttachmentInfo(attachment, ait, aid, index));
    }

    /**
     * Instantly reloads some ammo from inventory to inside the gun
     * @param shooter shooter entity
     * @param gun the gun stack
     * @param reloadCount ammo count to find and load.
     * @param options options.
     * @return actually loaded ammo count.
     */
    public static int magicReload(LivingEntity shooter, ItemStack gun, int reloadCount, MagicReloadOptions... options) {
        return MagicReloadImpl.magicReload(shooter, gun, reloadCount, options);
    }

    public static Vec3 getProximityMuzzlePos(LivingEntity shooter) {
        return getProximityMuzzlePos(shooter, shooter.getEyePosition());
    }

    public static Vec3 getProximityMuzzlePos(LivingEntity shooter, float partial) {
        return getProximityMuzzlePos(shooter, shooter.getEyePosition(partial));
    }

    private static Vec3 getProximityMuzzlePos(LivingEntity shooter, Vec3 eyePosition) {
        var op = IGunOperator.fromLivingEntity(shooter);
        var adsProgress = op.getSynAimingProgress();

        var axisZ = shooter.getLookAngle();
        var axisX = axisZ.cross(UP);
        var axisY = axisX.cross(axisZ);

        var x = Mth.lerp(adsProgress,0.06F, 0);
        var y = Mth.lerp(adsProgress, -0.08F, hasScope(shooter) ? -0.2F : 0);
        var z = Mth.lerp(adsProgress, 0.8F, 0.6F);

        var offset = bobCompensation(sideOf(shooter.level()), new Vec3(x, y, z));
        return eyePosition.add(axisX.scale(offset.x).add(axisY.scale(offset.y)).add(axisZ.scale(offset.z)));
    }

    private static final double EYE_TO_HAND_X = 6.0 / 16;
    private static final double EYE_TO_HAND_Y = 5.0 / 16;
    private static final Vec3 UP = new Vec3(0, 1, 0);

    private static boolean hasScope(LivingEntity shooter) {
        var info = getGunInfo(shooter.getMainHandItem()).orElse(null);
        if (info == null) {
            return false;
        }
        return !info.gunItem().getAttachment(shooter.registryAccess(), info.gunStack(), AttachmentType.SCOPE).isEmpty();
    }
}
