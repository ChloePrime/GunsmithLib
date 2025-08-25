package mod.chloeprime.gunsmithlib.client.eventhandler;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.client.input.AimKey;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;

import static com.tacz.guns.util.InputExtraCheck.isInGame;

@EventBusSubscriber(Dist.CLIENT)
public class CantAimWhenGunItemIsInCooldown {
    private static final Minecraft MC = Minecraft.getInstance();

    @SubscribeEvent
    private static void onPlayerTick(ClientTickEvent.Pre event) {
        tryCancelAiming();
    }

    @SubscribeEvent
    private static void onPlayerTick(ClientTickEvent.Post event) {
        tryCancelAiming();
    }

    @SubscribeEvent(priority = EventPriority.LOW)
    private static void onAimPress(InputEvent.MouseButton.Post event) {
        if (isInGame() && AimKey.AIM_KEY.matchesMouse(event.getButton())) {
            tryCancelAiming();
        }
    }

    private static void tryCancelAiming() {
        var player = MC.player;
        if (player == null || player.isSpectator() || !(player instanceof IClientPlayerGunOperator operator)) {
            return;
        }
        if (operator.isAim()) {
            if (player.getCooldowns().isOnCooldown(player.getMainHandItem().getItem())) {
                operator.aim(false);
            }
        }
    }
}
