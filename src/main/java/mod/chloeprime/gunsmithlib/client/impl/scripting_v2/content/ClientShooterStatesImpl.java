package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.client.scripting_v2.content.ClientShootStates;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import net.minecraft.world.entity.LivingEntity;

/**
 * @since 6.0.0
 */
public class ClientShooterStatesImpl extends BaseShooterStatesImpl implements ClientShootStates {
    public ClientShooterStatesImpl(LivingEntity shooter) {
        super(shooter);
    }
}
