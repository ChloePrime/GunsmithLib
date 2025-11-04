package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.AirburstSystem;
import net.minecraft.client.Minecraft;

public class AirburstSelectInput {
    /**
     * 蹲下时切换开火模式会变为切换空爆挡位
     */
    public static void onFireSelectInput(Runnable canceller) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(player.getMainHandItem()).orElse(null);
        if (gun == null) {
            return;
        }
        var distances = AirburstSystem.getAirburstDistances(gun);
        if (distances.size() < 2) {
            return;
        }

        if (player.isShiftKeyDown()) {
            canceller.run();
            // 此处默认支持测距的空爆武器在设定上带有弹道计算机，所以播放计算机音效
            if (AirburstSystem.getAirburstRangefinderMaxDistance(gun).isPresent()) {
                GunsmithLibClient.playComputerButtonSound();
            } else {
                GunsmithLibClient.playFireSelectSound(gun.gunStack());
            }
            // 通知服务端切换空爆挡位
            RPC.call(RPCTarget.toServer(), AirburstSystem::onSelectAirburstIndex, player);
        }
    }
}
