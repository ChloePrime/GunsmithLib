package mod.chloeprime.gunsmithlib.mixin.client;

import com.tacz.guns.client.particle.AmmoParticleSpawner;
import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.BulletTrailSystem;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AmmoParticleSpawner.class, remap = false)
public class MixinAmmoParticleSpawner {
    @Inject(method = "addParticle", at = @At("HEAD"), cancellable = true)
    private static void cancelIfEffekTrailIsConfigured(EntityKineticBullet bullet, CallbackInfo ci) {
        if (BulletTrailSystem.hideVanillaTrailCached(bullet, bullet.getGunId())) {
            ci.cancel();
        }
    }
}
