package mod.chloeprime.gunsmithlib.mixin.client.minecraft;

import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundCooldownPacket;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Objects;

@Mixin(ClientPacketListener.class)
public class MixinClientGamePacketListener {
    @Shadow @Final private Minecraft minecraft;

    @Inject(method = "handleItemCooldown", at = @At("TAIL"))
    private void triggerCooldownTransition(ClientboundCooldownPacket packet, CallbackInfo ci) {
        if (packet.getDuration() == 0) {
            return;
        }
        ItemStack gun = Objects.requireNonNull(minecraft.player).getMainHandItem();
        if (packet.getItem() != gun.getItem()) {
            return;
        }
        GunsmithLibClient.triggerAnimation(gun, GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_COOLDOWN_START);
    }
}
