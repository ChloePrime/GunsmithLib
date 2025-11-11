package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import cn.chloeprime.commons.rpc.RPC;
import cn.chloeprime.commons.rpc.RPCTarget;
import com.mojang.blaze3d.platform.InputConstants;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ChargeableTriggerSystem;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

import java.util.function.IntConsumer;

public class ChargeableTriggerInput {
    public static void onSemiInput(int glfwAction, IntConsumer setGlfwActionFunc, Runnable cancelFunc) {
        var player = Minecraft.getInstance().player;
        if (player == null || player.isSpectator()) {
            return;
        }
        var gun = Gunsmith.getGunInfo(player.getMainHandItem()).orElse(null);
        if (gun == null || GunsmithLibGunDataExtension.of(gun).filter(GunsmithLibGunDataExtension::isChargeable).isEmpty()) {
            return;
        }
        switch (glfwAction) {
            // 按下鼠标左键时不射击
            case InputConstants.PRESS -> {
                beginCharge(gun.gunStack());
                cancelFunc.run();
            }
            // 松开鼠标左键时射击
            case InputConstants.RELEASE -> setGlfwActionFunc.accept(InputConstants.PRESS);
        }
    }

    private static void beginCharge(ItemStack gun) {
        GunsmithLibClient.triggerAnimation(gun, GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_BEGIN_CHARGING);
        RPC.call(RPCTarget.toServer(), ChargeableTriggerSystem::beginCharging);
    }
}
