package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.ammo_variant;

import cn.chloeprime.commons.rpc.*;
import com.google.common.collect.ImmutableMap;
import com.tacz.guns.api.DefaultAssets;
import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.builder.GunItemBuilder;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.modifier.AttachmentPropertyManager;
import com.tacz.guns.resource.pojo.data.gun.GunData;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.client.gui.GunVariantSelectWheelScreen;
import mod.chloeprime.gunsmithlib.common.compat.CapabilityBasedModCompat;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.*;

public class AmmoVariantSystem {
    public static final String PDK_SELECTED_PART = GunsmithLib.loc("selected_part").toString();

    public static Optional<GunAmmoVariantSet.Part> getCurrentPart(ItemStack gun) {
        if (gun.hasTag()) {
            var tag = Objects.requireNonNull(gun.getTag());
            if (tag.contains(PDK_SELECTED_PART)) {
                var name = tag.getString(PDK_SELECTED_PART);
                return GunAmmoVariantSet.of(gun).map(set -> set.partByName().get(name));
            }
        }
        var gunInfo = Gunsmith.getGunInfo(gun).orElse(null);
        if (gunInfo == null) {
            return Optional.empty();
        }
        var gunId = gunInfo.gunId();
        return GunAmmoVariantSet.of(gun)
                .stream()
                .map(GunAmmoVariantSet::parts)
                .flatMap(Collection::stream)
                .filter(part -> part.variants().stream()
                        .map(GunAmmoVariantSet.Variant::getGunIds)
                        .flatMap(Collection::stream)
                        .anyMatch(gunId::equals))
                .findFirst()
                .or(() -> getDefaultPart(gunInfo));
    }

    public static Optional<GunAmmoVariantSet.Part> getDefaultPart(GunInfo gun) {
        return GunAmmoVariantSet.of(gun).stream()
                .filter(set -> !set.parts().isEmpty())
                .findFirst()
                .map(set -> set.parts().get(0));
    }

    public static void setCurrentPart(ItemStack gun, GunAmmoVariantSet.Part part) {
        gun.getOrCreateTag().putString(PDK_SELECTED_PART, part.name());
    }

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER)
    public static void switchToNextPart() {
        var user = RPCContext.isCalledThroughRPC() ? RPCContext.getSenderPlayer() : null;
        if (user == null) {
            return;
        }
        Gunsmith.getGunInfo(user.getMainHandItem()).ifPresent(gun -> switchToNextPart(gun, user));
    }

    public static void switchToNextPart(GunInfo gunBefore, @Nullable LivingEntity user) {
        Objects.requireNonNull(gunBefore);
        var prevAmmo = gunBefore.gunItem().getCurrentAmmoCount(gunBefore.gunStack());
        var prevBarrel = gunBefore.gunItem().hasBulletInBarrel(gunBefore.gunStack());

        if (user != null && user.level().isClientSide()) {
            return;
        }

        var variantSet = GunAmmoVariantSet.of(gunBefore).orElse(null);
        if (variantSet == null) {
            return;
        }
        var currentPart = getCurrentPart(gunBefore.gunStack()).orElse(null);
        if (currentPart == null) {
            return;
        }
        var nextPart = nextPart(variantSet, currentPart);
        @Nullable
        var nextPartData = storeAndUpdateAmmoVariantData(gunBefore, currentPart).byPartStorage().get(nextPart.name());
        var fireMode = Optional.ofNullable(nextPartData)
                .map(AmmoVariantStorage.OfSinglePart::fireMode)
                .filter(mode -> mode != FireMode.UNKNOWN)
                .orElseGet(() -> getDefaultFireMode(nextPart));
        int nextVariantIndex = nextPartData == null ? 0 : nextPartData.selectedVariant() % nextPart.variants().size();
        var nextVariant = nextPart.variants().get(nextVariantIndex);
        var nextGunId = nextVariant.getGunIdOrFallback(fireMode).orElse(null);
        if (nextGunId == null) {
            if (user != null) {
                user.sendSystemMessage(Component.literal("Failed to switch part: part %s selected an empty variant".formatted(nextPart.name())));
            }
            return;
        }

        if (setGunId(gunBefore, nextGunId, user)) {
            Gunsmith.getGunInfo(gunBefore.gunStack()).ifPresent(gunAfter -> restoreGunStateFromStorage(gunAfter, nextPartData));
            setCurrentPart(gunBefore.gunStack(), nextPart);
            if (user instanceof ServerPlayer ssp) {
                RPC.call(RPCTarget.to(ssp), AmmoVariantSystem::triggerAnimation, false, nextGunId, gunBefore.gunId(), prevAmmo, prevBarrel);
            }
        }
    }

    private static @Nonnull FireMode getDefaultFireMode(GunAmmoVariantSet.Part part) {
        var variants = part.variants();
        if (variants.isEmpty()) {
            return FireMode.UNKNOWN;
        }
        return variants.get(0).getGunIds().stream().findFirst()
                .flatMap(TimelessAPI::getCommonGunIndex)
                .flatMap(index -> index.getGunData().getFireModeSet().stream().findFirst())
                .orElse(FireMode.UNKNOWN);
    }

    private static GunAmmoVariantSet.Part nextPart(GunAmmoVariantSet set, GunAmmoVariantSet.Part current) {
        var parts = set.parts();
        int curIndex = set.partToIndex().getInt(current);
        return parts.get((curIndex + 1) % parts.size());
    }

    private static AmmoVariantStorage storeAndUpdateAmmoVariantData(GunInfo gun, GunAmmoVariantSet.Part currentPart) {
        var curVariantIndex = currentPart.gunIdToIndex().getInt(gun.gunId());
        var curStoredAmmo = gun.getTotalAmmo();
        var newPartStorage = new AmmoVariantStorage.OfSinglePart(curVariantIndex, curStoredAmmo, gun.getFireMode());

        var oldStorage = AmmoVariantStorage.of(gun.gunStack());
        var newStorage = new AmmoVariantStorage(ImmutableMap.<String, AmmoVariantStorage.OfSinglePart>builder()
                .putAll(oldStorage.byPartStorage())
                .put(currentPart.name(), newPartStorage)
                .buildKeepingLast());
        AmmoVariantStorage.set(gun.gunStack(), newStorage);
        return newStorage;
    }

    private static void restoreGunStateFromStorage(GunInfo gun, @Nullable AmmoVariantStorage.OfSinglePart partData) {
        gun.setTotalAmmo(partData == null ? 0 : partData.storedAmmo());
    }

    // 切换枪械弹种 / 模式

    /**
     * Returned list is immutable
     */
    public static List<ResourceLocation> getAvailableVariants(ItemStack gunStack) {
        var gun = Gunsmith.getGunInfo(gunStack).orElse(null);
        if (gun == null) {
            return Collections.emptyList();
        }
        var part = getCurrentPart(gunStack).orElse(null);
        if (part == null) {
            return Collections.emptyList();
        }
        var curFireMode = gun.getFireMode();
        return part.variants().stream()
                .map(variant -> variant.getGunIdOrFallback(curFireMode).orElse(DefaultAssets.EMPTY_GUN_ID))
                .toList();
    }

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER)
    public static void switchToVariant(int index) {
        var sender = RPCContext.isCalledThroughRPC() ? RPCContext.getSenderPlayer() : null;
        if (sender == null) {
            return;
        }
        Gunsmith.getGunInfo(sender.getMainHandItem()).ifPresent(gun -> switchToVariant(gun, index, sender));
    }

    public static void switchToVariant(GunInfo gunBefore, int index, @Nullable LivingEntity user) {
        Objects.requireNonNull(gunBefore);
        var prevAmmo = gunBefore.gunItem().getCurrentAmmoCount(gunBefore.gunStack());
        var prevBarrel = gunBefore.gunItem().hasBulletInBarrel(gunBefore.gunStack());

        if (user != null && user.level().isClientSide()) {
            return;
        }

        var gunStack = gunBefore.gunStack();
        var part = getCurrentPart(gunStack).orElse(null);
        if (part == null) {
            return;
        }
        var newVariant = part.variants().get(index % part.variants().size());
        var newGunId = newVariant.getGunIdOrFallback(gunBefore.getFireMode()).orElse(null);
        if (newGunId == null) {
            if (user != null) {
                user.sendSystemMessage(Component.literal("Failed to switch to variant %s: variant is empty".formatted(index)));
            }
            return;
        }

        var ammoBefore = gunBefore.index().getGunData().getAmmoId();
        var ammoAfter = TimelessAPI.getCommonGunIndex(newGunId)
                .map(CommonGunIndex::getGunData)
                .map(GunData::getAmmoId)
                .orElse(null);
        var isSameAmmo = Objects.equals(ammoBefore, ammoAfter);
        var options = isSameAmmo
                ? new ChangeGunIdOption[0]
                : new ChangeGunIdOption[]{ChangeGunIdOption.UNLOAD_BULLETS};

        if (setGunId(gunBefore, newGunId, user, options)) {
            if (user instanceof ServerPlayer ssp) {
                if (isSameAmmo) {
                    RPC.call(RPCTarget.to(ssp), AmmoVariantSystem::triggerAnimation, true, newGunId, gunBefore.gunId(), prevAmmo, prevBarrel);
                } else {
                    var newBackpackAmmoAmount = Gunsmith.getGunInfo(gunStack)
                            .map(gun -> GsHelper.scanBackpackAmmo(ssp, gun).orElse(CapabilityBasedModCompat.MAX_DISPLAYED_AMMO_SCANNED))
                            .orElse(0);
                    RPC.call(RPCTarget.to(ssp), AmmoVariantSystem::triggerClientReload, newGunId, newBackpackAmmoAmount);
                }
            }
        }
    }

    // 开火模式替换
    public static void onFireModeSelect(ItemStack gunStack, IGun gunInterface, @Nullable LivingEntity user) {
        if (user != null && user.level().isClientSide()) {
            return;
        }
        var gun = GsHelper.unpack(gunInterface, gunStack).orElse(null);
        if (gun == null) {
            return;
        }
        var curPart = getCurrentPart(gunStack).orElse(null);
        if (curPart == null || curPart.getVariantFromGunId(gun.gunId()) instanceof GunAmmoVariantSet.FireModeInvariantVariant) {
            return;
        }
        refreshVariant(gun, user);
    }

    public static void refreshVariant(GunInfo gunBefore, @Nullable LivingEntity user) {
        var curPart = getCurrentPart(gunBefore.gunStack()).orElse(null);
        if (curPart == null) {
            return;
        }
        var nextIndex = curPart.gunIdToIndex().getInt(gunBefore.gunId()) % curPart.variants().size();
        switchToVariant(gunBefore, nextIndex, user);
    }

    public static boolean setGunId(GunInfo gunBefore, ResourceLocation gunIdAfter, @Nullable LivingEntity user, ChangeGunIdOption... options) {
        Objects.requireNonNull(gunBefore);
        Objects.requireNonNull(gunIdAfter);

        if (user != null && user.level().isClientSide()) {
            return false;
        }
        // 新旧 ID 相同时不执行切换逻辑
        if (Objects.equals(gunBefore.gunId(), gunIdAfter)) {
            return false;
        }

        for (var option : options) {
            // 卸载弹匣内子弹
            if (option == ChangeGunIdOption.UNLOAD_BULLETS) {
                if (user instanceof Player player) {
                    gunBefore.dropAllAmmoIncludingBarrel(player);
                }
            }
        }

        var stack = gunBefore.gunStack();
        gunBefore.gunItem().setGunId(stack, gunIdAfter);
        var gunAfter = Gunsmith.getGunInfo(stack).orElse(null);

        // 试图切换到不存在的 id 时复原修改
        if (gunAfter == null) {
            gunBefore.gunItem().setGunId(stack, gunBefore.gunId());
            return false;
        }

        // 刷新配件缓存
        if (user != null) {
            AttachmentPropertyManager.postChangeEvent(user, stack);
            RPC.call(RPCTarget.near(user), AmmoVariantSystem::refreshCachedProperty, user, gunIdAfter);
        }

        return true;
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void triggerClientReload(ResourceLocation newGunId, int newBackpackAmmoAmount) {
        GunsmithLibClient.updateSyncedBackpackAmmoAmountImmediately(newBackpackAmmoAmount);
        GunsmithLibClient.clearAndReloadWeapon(newGunId);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void triggerAnimation(
            boolean isVariant, ResourceLocation newGunId,
            ResourceLocation prevGunId, int prevAmmo, boolean prevBarrel
    ) {
        var key = isVariant
                ? GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_VARIANT_SWITCHED
                : GunsmithLibAnimationConstant.GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED;

        GunsmithLibClient.setPreviousGunId(prevGunId);
        GunsmithLibClient.setPreviousAmmoInfo(prevAmmo, prevBarrel);

        GunsmithLibClient.setClientGunIdAndUpdateAnimationStateMachineContext(newGunId);
        GunsmithLibClient.triggerAnimation(key);

        GunsmithLibClient.setPreviousGunId(null);
        GunsmithLibClient.setPreviousAmmoInfo(-1, false);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void refreshCachedProperty(LivingEntity user, ResourceLocation gunIdAfter) {
        if (user == null) {
            return;
        }
        GunsmithLibClient.setClientGunIdAndUpdateAttachmentCache(user.getMainHandItem(), gunIdAfter, user);
    }

    @SuppressWarnings("ConditionCoveredByFurtherCondition")
    public static boolean hasVariantConnection(ItemStack a, ItemStack b) {
        var gunA = Gunsmith.getGunInfo(a).orElse(null);
        var gunB = Gunsmith.getGunInfo(b).orElse(null);
        if (gunA == null || gunB == null) {
            return false;
        }
        var setA = GunAmmoVariantSet.of(gunA).orElse(null);
        var setB = GunAmmoVariantSet.of(gunB).orElse(null);
        return setA != null && setB != null && setA == setB;
    }

    // GUI 服务端配合逻辑

    @RemoteCallable(flow = RPCFlow.CLIENT_TO_SERVER)
    public static void fetchBackpackAmmoCountFromServer(ResourceLocation gunId, int slot) {
        var sender = RPCContext.isCalledThroughRPC() ? RPCContext.getSenderPlayer() : null;
        if (sender == null) {
            return;
        }
        var gun = Gunsmith.getGunInfo(GunItemBuilder.create().setId(gunId).build()).orElse(null);
        if (gun == null) {
            return;
        }
        var count = GsHelper.scanBackpackAmmo(sender, gun).orElse(Integer.MAX_VALUE);
        RPC.call(RPCTarget.to(sender), AmmoVariantSystem::receiveBackpackAmmoCountFromServer, slot, count);
    }

    @RemoteCallable(flow = RPCFlow.SERVER_TO_CLIENT)
    private static void receiveBackpackAmmoCountFromServer(int slot, int count) {
        GunVariantSelectWheelScreen.receiveBackpackAmmoCount(slot, count);
    }

    private AmmoVariantSystem() {
    }
}
