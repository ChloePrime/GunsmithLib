package mod.chloeprime.gunsmithlib.mixin.client;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.mojang.blaze3d.audio.SoundBuffer;
import com.tacz.guns.client.sound.GunSoundInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.init.ModSounds;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.EntityBoundSoundInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.levelgen.RandomSupport;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;

import java.util.function.Supplier;

@Mixin(value = SoundPlayManager.class, remap = false)
public class MixinSoundPlayManager {
    @WrapMethod(method = "playClientSound(Lnet/minecraft/world/entity/Entity;Lnet/minecraft/resources/ResourceLocation;FFIZ)Lcom/tacz/guns/client/sound/GunSoundInstance;")
    private static GunSoundInstance playVanillaSoundEventIfPossible(
            Entity entity, @Nullable ResourceLocation name, float volume, float pitch, int distance, boolean mono, Operation<GunSoundInstance> original
    ) {
        var orig = (Supplier<GunSoundInstance>) () -> original.call(entity, name, volume, pitch, distance, mono);
        if (name == null) {
            return orig.get();
        }
        var mc = Minecraft.getInstance();
        var level = mc.level;
        var player = mc.player;
        if (player == null || level == null) {
            return orig.get();
        }
        var se = BuiltInRegistries.SOUND_EVENT.get(name);
        if (se == null) {
            return orig.get();
        }
        var inst = new EntityBoundSoundInstance(se, SoundSource.PLAYERS, volume, pitch, entity, RandomSupport.generateUniqueSeed()) {
            public void setStop() {
                super.stop();
            }
        };
        mc.getSoundManager().play(inst);

        return new GunSoundInstance(ModSounds.GUN.get(), SoundSource.PLAYERS, volume, pitch, entity, distance, name, mono) {
            @Override
            public @Nullable SoundBuffer getSoundBuffer() {
                return null;
            }

            @Override
            public void setStop() {
                super.setStop();
                inst.setStop();
            }
        };
    }
}
