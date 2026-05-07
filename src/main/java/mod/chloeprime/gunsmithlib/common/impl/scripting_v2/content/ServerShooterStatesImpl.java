package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ServerShootStates;
import net.minecraft.world.entity.LivingEntity;

/**
 * @since 6.0.0
 */
public class ServerShooterStatesImpl extends BaseShooterStatesImpl implements ServerShootStates {
    public ServerShooterStatesImpl(LivingEntity shooter) {
        super(shooter);
    }

    @Override
    public boolean shoot_needs_ammo() {
        return operator.needCheckAmmo();
    }

    @Override
    public boolean shoot_consumes_ammo() {
        return operator.consumesAmmoOrNot();
    }
}
