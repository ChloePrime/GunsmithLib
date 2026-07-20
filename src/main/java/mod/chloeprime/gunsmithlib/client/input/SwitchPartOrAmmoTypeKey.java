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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.client.settings.KeyModifier;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.Optional;

@Mod.EventBusSubscriber(Dist.CLIENT)
public final class SwitchPartOrAmmoTypeKey {
    public static final long SHORT_CLICK_THRESHOLD = 500;
    private static long lastPressTime;
    private static int isPressing;

    public static final KeyMapping KEY_MAPPING = new KeyMapping(
            "key.%s.switch_part_or_ammo_type.desc".formatted(GunsmithLib.MOD_ID),
            GunsmithLibInput.KeyConflictContexts.UNIVERSAL_CONCURRENT,
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
        if (!KeyConflictContext.IN_GAME.isActive()) {
            return;
        }
        var now = System.currentTimeMillis();
        if (glfwAction == InputConstants.PRESS) {
            isPressing++;
            lastPressTime = now;
            return;
        }
        if (glfwAction == InputConstants.REPEAT) {
            return;
        }
        isPressing = Math.max(0, isPressing - 1);

        if (now - lastPressTime < SHORT_CLICK_THRESHOLD) {
            isPressing = 0;
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
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        if (isPressing == 0) {
            return;
        }

        var now = System.currentTimeMillis();
        if (now - lastPressTime >= SHORT_CLICK_THRESHOLD) {
            isPressing = 0;
            var mc = Minecraft.getInstance();
            var player = mc.player;
            if (player == null || mc.screen instanceof GunVariantSelectWheelScreen) {
                return;
            }
            if (GunAmmoVariantSet.of(player.getMainHandItem()).isEmpty()) {
                return;
            }
            var ids = AmmoVariantSystem.getAvailableVariants(player.getMainHandItem());
            if (ids.size() <= 1) {
                return;
            }
            mc.setScreen(new GunVariantSelectWheelScreen(ids));
        }
    }
}
