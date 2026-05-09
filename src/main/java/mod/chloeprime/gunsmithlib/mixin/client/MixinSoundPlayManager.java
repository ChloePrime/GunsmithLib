package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.tacz.guns.client.sound.EntityTrackingGunSoundInstance;
import com.tacz.guns.client.sound.GunSoundInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import mod.chloeprime.gunsmithlib.client.audio.AudioChecker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.client.sounds.WeighedSoundEvents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;

import javax.annotation.Nonnull;
import java.util.function.Supplier;

@Mixin(value = SoundPlayManager.class, remap = false)
public class MixinSoundPlayManager {
    @ModifyReturnValue(method = "hasSoundResource", at = @At("RETURN"))
    private static boolean hasVanillaSoundIsAlsoHasSoundSource(boolean original, Minecraft mc, ResourceLocation id) {
        return original || AudioChecker.isValidSoundEvent(AudioChecker.holder(id));
    }

    @WrapOperation(
            method = "playClientSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/resources/ResourceLocation;FFIZIZZ)Lcom/tacz/guns/client/sound/GunSoundInstance;",
            at = @At(value = "NEW", target = "com/tacz/guns/client/sound/GunSoundInstance"))
    private static GunSoundInstance playVanillaSoundEventIfPossible(
            SoundEvent se, SoundSource source, float volume, float pitch, Entity entity, int distance, ResourceLocation registryName, boolean mono, boolean relative,
            Operation<GunSoundInstance> original
    ) {
        var fallback = (Supplier<GunSoundInstance>) () -> original.call(se, source, volume, pitch, entity, distance, registryName, mono, relative);
        return gunsmithlib$playVanillaSoundEventIfPossible(fallback, entity, registryName, volume, pitch, distance, mono, relative, false);
    }

    @WrapOperation(
            method = "playClientSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/resources/ResourceLocation;FFIZIZZ)Lcom/tacz/guns/client/sound/GunSoundInstance;",
            at = @At(value = "NEW", target = "com/tacz/guns/client/sound/EntityTrackingGunSoundInstance"))
    private static EntityTrackingGunSoundInstance playVanillaSoundEventIfPossible(
            SoundEvent se, SoundSource source, float volume, float pitch, Entity entity, int distance, ResourceLocation registryName, boolean mono,
            Operation<EntityTrackingGunSoundInstance> original
    ) {
        var fallback = (Supplier<EntityTrackingGunSoundInstance>) () -> original.call(se, source, volume, pitch, entity, distance, registryName, mono);
        return gunsmithlib$playVanillaSoundEventIfPossible(fallback, entity, registryName, volume, pitch, distance, mono, false, true);
    }

    @Unique
    @SuppressWarnings("unchecked")
    private static
    <S extends GunSoundInstance>
    S gunsmithlib$playVanillaSoundEventIfPossible(
            Supplier<S> original, Entity entity, ResourceLocation name, float volume, float pitch, int distance, boolean mono, boolean relative, boolean trackEntity
    ) {
        if (name == null) {
            return original.get();
        }
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (player == null || level == null) {
            return original.get();
        }
        var holder = AudioChecker.holder(name);
        if (!AudioChecker.isValidSoundEvent(holder)) {
            return original.get();
        }
        var se = holder.value();
        var delegate = new SimpleSoundInstance(se, SoundSource.PLAYERS, volume, pitch, entity.level().getRandom(), entity.blockPosition());
        if (trackEntity)
            return (S) new EntityTrackingGunSoundInstance(se, SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono) {
                @Override
                public @Nonnull WeighedSoundEvents resolve(SoundManager manager) {
                    var ret = delegate.resolve(manager);
                    this.sound = delegate.getSound();
                    return ret;
                }
            };
        else {
            return (S) new GunSoundInstance(se, SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono, relative) {
                @Override
                public @Nonnull WeighedSoundEvents resolve(SoundManager manager) {
                    var ret = delegate.resolve(manager);
                    this.sound = delegate.getSound();
                    return ret;
                }
            };
        }
    }
}
