package mod.chloeprime.gunsmithlib.client.audio;

import cn.chloeprime.commons.ContextUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nonnull;
import java.util.Optional;

public final class AudioChecker {
    private static final long MAGIC_SEED = 889013900943277918L;

    public static @Nonnull Holder<SoundEvent> holder(ResourceLocation id) {
        return Optional.ofNullable(ContextUtil.getRegistryAccessSafely())
                .flatMap(registries -> registries.registry(Registries.SOUND_EVENT))
                .orElse(BuiltInRegistries.SOUND_EVENT)
                .getHolder(ResourceKey.create(Registries.SOUND_EVENT, id))
                .map(holder -> (Holder<SoundEvent>) holder)
                .orElseGet(() -> Holder.direct(SoundEvent.createVariableRangeEvent(id)));
    }

    public static boolean isValidSoundEvent(Holder<SoundEvent> holder) {
        var mc = Minecraft.getInstance();
        var pos = Optional.ofNullable(mc.player)
                .map(Entity::position)
                .orElse(Vec3.ZERO);
        var random = RandomSource.create(MAGIC_SEED);
        var instance = new SimpleSoundInstance(
                holder.value(), SoundSource.MASTER, 1, 1,
                random, pos.x(), pos.y(), pos.z());
        if (instance.resolve(mc.getSoundManager()) == SoundManager.INTENTIONALLY_EMPTY_SOUND_EVENT) {
            return false;
        }
        var sound = instance.getSound();
        return sound != SoundManager.EMPTY_SOUND && sound != SoundManager.INTENTIONALLY_EMPTY_SOUND;
    }

    private AudioChecker() {
    }
}
