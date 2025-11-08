package mod.chloeprime.gunsmithlib.client.input;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import com.mojang.blaze3d.platform.InputConstants;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.client.gui.GunVariantSelectWheelScreen;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.AmmoVariantSystem;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant.GunAmmoVariantSet;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.Connection;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.settings.KeyModifier;

import java.util.Optional;

@EventBusSubscriber(Dist.CLIENT)
public final class SwitchPartOrAmmoTypeKey {
    public static final long SHORT_CLICK_THRESHOLD = 500;
    private static long lastPressTime;
    private static int isPressing;

    public static final KeyMapping KEY_MAPPING = new KeyMapping(
            "key.%s.switch_part_or_ammo_type.desc".formatted(GunsmithLib.MOD_ID),
            GunsmithLibInput.KeyConflictContexts.IN_GAME_CONCURRENT,
            KeyModifier.NONE,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_X,
            GunsmithLibInput.CATEGORY);

    @SubscribeEvent
    public static void onMousePress(InputEvent.MouseButton.Post event) {
        if (!KEY_MAPPING.matchesMouse(event.getButton())) {
            return;
        }
        signal(event.getAction());
    }

    @SubscribeEvent
    public static void onKeyPress(InputEvent.Key event) {
        if (!KEY_MAPPING.matches(event.getKey(), event.getScanCode())) {
            return;
        }
        signal(event.getAction());
    }

    private static void signal(int glfwAction) {
        var now = System.currentTimeMillis();
        if (glfwAction == InputConstants.PRESS) {
            isPressing++;
            lastPressTime = now;
            return;
        }
        if (glfwAction == InputConstants.REPEAT) {
            return;
        }
        isPressing--;

        if (now - lastPressTime < SHORT_CLICK_THRESHOLD) {
            var isConnected = Optional.ofNullable(Minecraft.getInstance().getConnection())
                    .map(ClientPacketListener::getConnection)
                    .filter(Connection::isConnected)
                    .isPresent();
            if (isConnected) {
                RPC.call(RPCTarget.toServer(), AmmoVariantSystem::switchToNextPart);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        if (isPressing == 0) {
            return;
        }

        var now = System.currentTimeMillis();
        if (now - lastPressTime >= SHORT_CLICK_THRESHOLD) {
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null || mc.screen instanceof GunVariantSelectWheelScreen) {
                return;
            }
            if (GunAmmoVariantSet.of(player.getMainHandItem()).isEmpty()) {
                return;
            }
            var ids = AmmoVariantSystem.getAvailableVariants(player.getMainHandItem());
            mc.setScreen(new GunVariantSelectWheelScreen(ids));
        }
    }
}
