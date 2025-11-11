package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.tacz.guns.client.input.ShootKey;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.ChargeableTriggerInput;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.event.InputEvent;
import org.apache.commons.lang3.mutable.MutableBoolean;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.mojang.blaze3d.platform.InputConstants.*;
import static com.tacz.guns.util.InputExtraCheck.isInGame;

@Mixin(value = ShootKey.class, remap = false)
public class MixinShootKey {
    @Shadow
    private static boolean lastTimeShootSuccess;

    @Inject(method = "semiShoot", at = @At("HEAD"), cancellable = true)
    private static void chargeableSemiShoot(InputEvent.MouseButton.Post event, CallbackInfo ci) {
        if (isInGame() && SHOOT_KEY.matchesMouse(event.getButton())) {
            Runnable canceller = () -> {
                ci.cancel();
                lastTimeShootSuccess = true;
            };
            ChargeableTriggerInput.onSemiInput(event.getAction(), ((MouseButtonInputEventAccessor) event)::setAction, canceller);
        }
    }

    @WrapMethod(method = "semiShootController")
    private static boolean chargeableSemiShootGamepad(boolean isPress, Operation<Boolean> original) {
        var decoratedAction = new MutableBoolean(isPress);
        if (isInGame()) {
            int originalAction = isPress ? PRESS : RELEASE;
            var canceled = new MutableBoolean(false);
            Runnable canceller = () -> {
                canceled.setTrue();
                lastTimeShootSuccess = true;
            };
            ChargeableTriggerInput.onSemiInput(originalAction, action -> decoratedAction.setValue(action == PRESS), canceller);
            if (canceled.isTrue()) {
                return false;
            }
        }
        return original.call(decoratedAction.booleanValue());
    }

    @Shadow @Final public static KeyMapping SHOOT_KEY;
}
