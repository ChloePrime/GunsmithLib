package mod.chloeprime.gunsmithlib.mixin;

import mod.chloeprime.gunsmithlib.Config;
import mod.chloeprime.gunsmithlib.common.internal.EnhancedKineticBullet;
import mod.chloeprime.gunsmithlib.common.internal.SuspiciousBehaviorFirewall;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class MixinEntity {
    @Shadow public abstract Level level();

    @Inject(method = "setRemoved", at = @At("HEAD"), cancellable = true)
    private void preventSuspiciousRemove(Entity.RemovalReason reason, CallbackInfo ci) {
        if (level().isClientSide()) {
            return;
        }
        if (!Config.ENABLE_REMOVE_INTERCEPTION.get()) {
            return;
        }
        if (!(this instanceof EnhancedKineticBullet)) {
            return;
        }
        if (reason.shouldDestroy() && SuspiciousBehaviorFirewall.isUnderSuspiciousContext()) {
            ci.cancel();
        }
    }
}
