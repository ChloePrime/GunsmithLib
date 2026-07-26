package mod.chloeprime.gunsmithlib.client;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.client.animation.statemachine.AnimationStateMachine;
import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.entity.ReloadState;
import com.tacz.guns.client.gameplay.LocalPlayerReload;
import com.tacz.guns.client.model.papi.PapiManager;
import com.tacz.guns.client.resource.GunDisplayInstance;
import com.tacz.guns.client.resource.index.ClientGunIndex;
import com.tacz.guns.client.sound.SoundPlayManager;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.tooltip.DescriptionalGunAffix;
import mod.chloeprime.gunsmithlib.client.gunpack_extension.EnhancedGunDisplayInstance;
import mod.chloeprime.gunsmithlib.client.papi.AirburstDistancePapi;
import mod.chloeprime.gunsmithlib.client.papi.RangefinderPapi;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.compat.ModInstallationStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
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
import net.neoforged.fml.config.IConfigSpec;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import org.jetbrains.annotations.ApiStatus;

import javax.annotation.Nullable;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Supplier;

@EventBusSubscriber(Dist.CLIENT)
public class GunsmithLibClient {
    private static @Nullable String prevGunId = null;
    private static int prevAmmoAmount = -1;
    private static boolean prevAmmoInBarrel = false;

    public static void onClientConstruct(BiConsumer<ModConfig.Type, IConfigSpec> registerConfigFunc) {
        registerConfigFunc.accept(ModConfig.Type.CLIENT, GunsmithClientConfig.SPEC);
        DescriptionalGunAffix.init();
        if (ModInstallationStatus.TACZ_PRESENCE_INSTALLED) {
            GunsmithLib.LOGGER.warn("GunsmithLib found a partially incompatible mod: {}", ModInstallationStatus.TACZ_PRESENCE_ID);
            GunsmithLib.LOGGER.warn("Incompatible functionalities will be disabled when these incompatible mods are installed.");
        }
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        event.enqueueWork(GunsmithLibClient::initClient);
    }

    public static void initClient() {
        PapiManager.addPapi(RangefinderPapi.NAME, RangefinderPapi.INSTANCE);
        PapiManager.addPapi(AirburstDistancePapi.NAME, AirburstDistancePapi.INSTANCE);
    }

    @SubscribeEvent
    public static void registerEntityRenderersExcludingLaser(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(GunsmithLib.EntityTypes.RANGEFINDER_MARKER.get(), NoopRenderer::new);
        event.registerEntityRenderer(GunsmithLib.EntityTypes.AREA_EFFECT_CLOUD_3D.get(), NoopRenderer::new);
    }

    public static Optional<GunInfo> currentGunInfo() {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .flatMap(Gunsmith::getGunInfo);
    }

    public static Optional<GunDisplayInstance> currentGunDisplay() {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(LivingEntity::getMainHandItem)
                .flatMap(TimelessAPI::getGunDisplay);
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

    public static Optional<String> getPreviousGunId() {
        return Optional.ofNullable(prevGunId);
    }

    public static int getPreviousAmmoAmount() {
        return prevAmmoAmount;
    }

    public static boolean prevHasAmmoInBarrel() {
        return prevAmmoInBarrel;
    }

    public static void setPreviousGunId(@Nullable ResourceLocation id) {
        prevGunId = Optional.ofNullable(id).map(ResourceLocation::toString).orElse(null);
    }

    public static void setPreviousAmmoInfo(int ammoAmount, boolean hasAmmoInnBarrel) {
        prevAmmoAmount = ammoAmount;
        prevAmmoInBarrel = hasAmmoInnBarrel;
    }

    public static void triggerAnimation(String key) {
        triggerAnimation(Minecraft.getInstance().player, key);
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
        updateStateMachineContext(gun);
        TimelessAPI.getGunDisplay(gun)
                .map(GunDisplayInstance::getAnimationStateMachine)
                .ifPresent(sm -> sm.trigger(key));
    }

    public static void updateSyncedBackpackAmmoAmountImmediately(int amount) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        CapabilityBasedModCompat.setClientSyncedAmmoCountInBackpack(player, amount);
    }

    @ApiStatus.Internal
    public static void clearAndReloadWeapon(ResourceLocation newGunId) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        // 刷新枪械 id，防止根据旧备弹数量判定是否能换弹
        var weapon = player.getMainHandItem();
        setClientGunIdAndUpdateAnimationStateMachineContext(weapon, newGunId);
        Gunsmith.getGunInfo(weapon).ifPresent(gun -> gun.setTotalAmmo(0));
        // reload
        IClientPlayerGunOperator.fromLocalPlayer(player).reload();
    }

    public static void setClientGunIdAndUpdateAnimationStateMachineContext(ResourceLocation newGunId) {
        var player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        setClientGunIdAndUpdateAnimationStateMachineContext(player.getMainHandItem(), newGunId);
    }

    public static void setClientGunIdAndUpdateAnimationStateMachineContext(ItemStack weapon, ResourceLocation newGunId) {
        Gunsmith.getGunInfo(weapon).ifPresent(gun -> {
            gun.gunItem().setGunId(weapon, newGunId);
            updateStateMachineContext(weapon);
        });
    }

    public static void setClientGunIdAndUpdateAttachmentCache(ItemStack weapon, ResourceLocation newGunId, LivingEntity user) {
        if (user == null) {
            return;
        }
        Gunsmith.getGunInfo(weapon).ifPresent(gun -> {
            gun.gunItem().setGunId(weapon, newGunId);
            AttachmentPropertyManager.postChangeEvent(user, weapon);
        });
    }

    private static void updateStateMachineContext(ItemStack gun) {
        TimelessAPI.getGunDisplay(gun)
                .map(GunDisplayInstance::getAnimationStateMachine)
                .map(AnimationStateMachine::getContext)
                .ifPresent(gsm -> gsm.setCurrentGunItem(gun));
    }

    private static Map<ResourceLocation, List<ResourceLocation>> lastRedirectionData;

    public static void applyDisplayRedirectionData() {
        if (lastRedirectionData != null) {
            applyDisplayRedirectionData(lastRedirectionData);
        }
    }

    public static void applyDisplayRedirectionData(Map<ResourceLocation, List<ResourceLocation>> redirections) {
        lastRedirectionData = redirections;
        // 清空之前的 override 数据防止残留
        for (var entry : TimelessAPI.getAllClientGunIndex()) {
            if (entry.getValue().getDefaultDisplay() instanceof EnhancedGunDisplayInstance display) {
                display.gunsmith$acceptOverride(null);
            }
        }
        // 装载新的 redirect 数据
        redirections.forEach(GunsmithLibClient::applyDisplayRedirection);
    }

    private static void applyDisplayRedirection(ResourceLocation masterId, List<ResourceLocation> ids) {
        var master = TimelessAPI
                .getClientGunIndex(masterId)
                .map(ClientGunIndex::getDefaultDisplay)
                .orElse(null);
        if (master == null) {
            return;
        }
        ids.stream()
                .flatMap(id -> TimelessAPI.getClientGunIndex(id).stream())
                .map(ClientGunIndex::getDefaultDisplay)
                .filter(display -> display != master)
                .filter(Objects::nonNull)
                .forEach(display -> ((EnhancedGunDisplayInstance) display).gunsmith$acceptOverride(master));
    }

    /**
     * @since 6.3
     */
    public static boolean isReloading() {
        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(IGunOperator::fromLivingEntity)
                .map(IGunOperator::getSynReloadState)
                .map(ReloadState::getStateType)
                .filter(ReloadState.StateType::isReloading)
                .isPresent();
    }

    /**
     * @since 6.3
     */
    public static void cancelReload() {
        Optional.ofNullable(Minecraft.getInstance().player)
                .map(GunsmithLibClient::getPlayerReloader)
                .ifPresent(LocalPlayerReload::cancelReload);
    }

    /**
     * @since 6.3
     */
    private static final VarHandle LOCAL_PLAYER_RELOAD_FIELD = ((Supplier<VarHandle>) () -> {
        try {
            var lookup = MethodHandles.privateLookupIn(LocalPlayer.class, MethodHandles.lookup());
            // noinspection JavaLangInvokeHandleSignature
            return lookup.findVarHandle(LocalPlayer.class, "tac$reload", LocalPlayerReload.class);
        } catch (Exception ex) {
            throw sneak(ex);
        }
    }).get();

    /**
     * @since 6.3
     */
    public static LocalPlayerReload getPlayerReloader(LocalPlayer player) {
        return (LocalPlayerReload) LOCAL_PLAYER_RELOAD_FIELD.get(player);
    }

    @SuppressWarnings("unchecked")
    private static <T extends Throwable> RuntimeException sneak(Throwable exception) throws T {
        throw (T) exception;
    }

    private GunsmithLibClient() {
    }
}
