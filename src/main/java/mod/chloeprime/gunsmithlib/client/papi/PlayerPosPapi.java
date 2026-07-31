package mod.chloeprime.gunsmithlib.client.papi;

import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.client.papi.framework.Vector3dPapi;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

/**
 * 玩家脚底位置
 * <p>
 * {@code %gunsmithlib:player_pos%}
 * <p>
 * 格式为 "x, y, z"，精确到小数点后 1 位
 *
 * @since 6.4
 */
public enum PlayerPosPapi implements Vector3dPapi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("player_pos").toString();

    @Override
    public Optional<Vec3> getVector() {
        return Optional.ofNullable(Minecraft.getInstance().player).map(Entity::position);
    }
}
