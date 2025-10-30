package mod.chloeprime.gunsmithlib.client.input;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import com.mojang.blaze3d.platform.InputConstants;
import com.tacz.guns.util.InputExtraCheck;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.explosive.AirburstSystem;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

@EventBusSubscriber(Dist.CLIENT)
public class BallisticComputerKey {
    public static final KeyMapping KEY_MAPPING = new KeyMapping(
            "key.%s.ballistic_computer.desc".formatted(GunsmithLib.MOD_ID),
            GunsmithLibInput.KeyConflictContexts.IN_GAME_CONCURRENT,
            KeyModifier.NONE,
            InputConstants.Type.MOUSE,
            InputConstants.MOUSE_BUTTON_MIDDLE,
            GunsmithLibInput.CATEGORY);

    @SubscribeEvent
    public static void onMousePress(InputEvent.MouseButton.Post event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }
        if (!KEY_MAPPING.matchesMouse(event.getButton())) {
            return;
        }
        pressBallisticComputer();
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (event.getAction() != InputConstants.PRESS) {
            return;
        }
        if (!KEY_MAPPING.matches(event.getKey(), event.getScanCode())) {
            return;
        }
        pressBallisticComputer();
    }

    public static void pressBallisticComputer() {
        if (!InputExtraCheck.isInGame()) {
            return;
        }
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(player.getMainHandItem()).orElse(null);
        if (gun == null || AirburstSystem.getAirburstRangefinderMaxDistance(gun).isEmpty()) {
            return;
        }
        RPC.call(RPCTarget.toServer(), AirburstSystem::onBallisticComputerPressed, player);
    }
}
