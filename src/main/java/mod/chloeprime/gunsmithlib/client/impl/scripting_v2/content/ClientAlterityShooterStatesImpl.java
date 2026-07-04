package mod.chloeprime.gunsmithlib.client.impl.scripting_v2.content;

import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BaseShooterStatesImpl;
import net.minecraft.world.entity.LivingEntity;

/**
 * 客户端玩家以外的实体状态的实现
 *
 * @since 6.2
 */
public class ClientAlterityShooterStatesImpl extends BaseShooterStatesImpl {
    public ClientAlterityShooterStatesImpl(LivingEntity shooter) {
        super(shooter);
    }
}
