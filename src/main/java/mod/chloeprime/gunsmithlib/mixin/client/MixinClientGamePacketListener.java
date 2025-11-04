package mod.chloeprime.gunsmithlib.mixin.client;

import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPacketListener.class)
public abstract class MixinClientGamePacketListener extends ClientCommonPacketListenerImpl {
    /**
     * 触发枪盾动画
     */
    @Inject(method = "handleItemCooldown", at = @At("TAIL"))
    private void triggerCooldownTransition(ClientboundCooldownPacket packet, CallbackInfo ci) {
        if (packet.duration() == 0) {
            return;
        }
        ItemStack gun = Objects.requireNonNull(minecraft.player).getMainHandItem();
        if (packet.item() != gun.getItem()) {
            return;
        }
        GunsmithLibClient.triggerAnimation(gun, GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_COOLDOWN_START);
    }

    public MixinClientGamePacketListener(Minecraft minecraft, Connection connection, CommonListenerCookie commonListenerCookie) {
        super(minecraft, connection, commonListenerCookie);
    }
}
