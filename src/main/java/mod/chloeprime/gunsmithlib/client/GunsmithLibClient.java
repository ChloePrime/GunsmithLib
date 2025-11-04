package mod.chloeprime.gunsmithlib.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.client.model.papi.PapiManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.sound.SoundPlayManager;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.papi.RangefinderPapi;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import javax.annotation.Nullable;
import java.util.Objects;

@EventBusSubscriber(Dist.CLIENT)
public class GunsmithLibClient {
    public static void initClient() {
        PapiManager.addPapi(RangefinderPapi.NAME, RangefinderPapi.INSTANCE);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(GunsmithLibClient::initClient);
    }

    @SubscribeEvent
    public static void registerEntityRenderersExcludingLaser(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GunsmithLib.EntityTypes.RANGEFINDER_MARKER.get(), NoopRenderer::new);
        event.registerEntityRenderer(GunsmithLib.EntityTypes.AREA_EFFECT_CLOUD_3D.get(), NoopRenderer::new);
    }

    public static void playComputerButtonSound() {
        playUnspatialSound(GunsmithLib.SoundEvents.BALLISTIC_COMPUTER.getId());
    }

    public static void playFireSelectSound(ItemStack gun) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        TimelessAPI
                .getGunDisplay(gun)
                .ifPresent(display -> SoundPlayManager.playFireSelectSound(player, display));
    }

    public static void playUnspatialSound(ResourceLocation id) {
        Objects.requireNonNull(id, "Registry not initialized");
        var sound = new SimpleSoundInstance(
                id, SoundSource.PLAYERS, 1, 1,
                SoundInstance.createUnseededRandom(), false, 0, SoundInstance.Attenuation.NONE,
                0, 0, 0, true);
        Minecraft.getInstance().getSoundManager().play(sound);
    }

    public static void triggerAnimation(@Nullable LivingEntity user, String key) {
        if (user == null) {
            return;
        }
        var gun = user.getMainHandItem();
        if (Gunsmith.getGunInfo(gun).isEmpty()) {
            return;
        }
        triggerAnimation(gun, key);
    }

    public static void triggerAnimation(ItemStack gun, String key) {
        TimelessAPI.getGunDisplay(gun)
                .map(GunDisplayInstance::getAnimationStateMachine)
                .ifPresent(sm -> sm.trigger(key));
    }
}
