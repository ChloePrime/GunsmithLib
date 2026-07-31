package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Rangefinder;
import mod.chloeprime.gunsmithlib.client.papi.framework.PapiTargetCache;
import mod.chloeprime.gunsmithlib.client.papi.framework.Vector3dPapi;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 瞄准目标位置
 * <p>
 * {@code %gunsmithlib:target_pos%}
 * <p>
 * 格式为 "x, y, z"，精确到小数点后 1 位
 *
 * @since 6.4
 */
public enum TargetPosPapi implements Vector3dPapi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("target_pos").toString();

    @Override
    public Optional<Vec3> getVector() {
        return PapiTargetCache.current()
                .map(Rangefinder.Result::asHitResult)
                .map(HitResult::getLocation);
    }
}
