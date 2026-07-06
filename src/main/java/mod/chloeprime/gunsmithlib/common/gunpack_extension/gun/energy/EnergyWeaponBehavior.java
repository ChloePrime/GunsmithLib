package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy;

import com.tacz.guns.api.TimelessAPI;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.event.common.AttachmentPropertyEvent;
import com.tacz.guns.api.event.common.GunReloadEvent;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.GunReloadFeedEvent;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.EnhancedGunData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@Deprecated(since = "6.1.0")
@SuppressWarnings("DeprecatedIsStillUsed")
@Mod.EventBusSubscriber
public class EnergyWeaponBehavior {
    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    public static boolean isEnergyWeapon(ItemStack stack) {
        return EnergyWeaponData.runtime(stack).isPresent();
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void energyWeaponCannotReloadUnlessInCreative(GunReloadEvent event) {
        var isCreative = !IGunOperator.fromLivingEntity(event.getEntity()).needCheckAmmo();
        if (isCreative) {
            return;
        }
        var disableReload = EnergyWeaponData.runtime(event.getGunItemStack())
                .filter(data -> !canEnergyWeaponReload(data))
                .isPresent();
        if (disableReload) {
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public static void preventAttachmentBatteryOverflow(AttachmentPropertyEvent event) {
        EnergyWeaponData.runtime(event.getGunItem()).ifPresent(runtime -> {
            var gun = runtime.gun();
            int bulletInMag = gun.getTotalAmmo();
            var bullet = bulletInMag + gun.getDummyAmmoAmount();
            var max = gun.getTotalMagazineSize();

            if (bullet > max) {
                setTotalAmmo(gun, max);
            }
        });
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void clearHeatIfThatIsConfiguredToRequiresReload(GunReloadFeedEvent.Post event) {
        var data = EnergyWeaponData.runtime(event.getGunInfo()).orElse(null);
        if (data == null || !data.energy().needsReloadOnFullHeat()) {
            return;
        }
        data.gun().gunItem().setOverheatLocked(data.gun().gunStack(), false);
        data.gun().gunItem().setHeatAmount(data.gun().gunStack(), 0);
    }

    public static void setTotalAmmo(GunInfo gun, int value) {
        gun.gunItem().setBulletInBarrel(gun.gunStack(), false);
        gun.gunItem().setCurrentAmmoCount(gun.gunStack(), 0);
        gun.setDummyAmmoAmount(0);

        if (value > 0) {
            gun.gunItem().setBulletInBarrel(gun.gunStack(), true);
            value -= 1;
        }

        if (value > 0) {
            var magSize = AttachmentDataUtils.getAmmoCountWithAttachment(gun.gunStack(), gun.index().getGunData());
            var inMag = Math.max(value, magSize);
            gun.gunItem().setCurrentAmmoCount(gun.gunStack(), inMag);
        }
    }

    @SubscribeEvent
    public static void preventCreativeBatteryOverflow(GunReloadFeedEvent.Post event) {
        if (!isEnergyWeapon(event.getGunInfo().gunStack())) {
            return;
        }
        // 防止创造模式下弹药溢出
        if (!IGunOperator.fromLivingEntity(event.getEntity()).needCheckAmmo()) {
            event.getGunInfo().setDummyAmmoAmount(0);
        }
    }

    public static boolean canEnergyWeaponReload(EnergyWeaponData.Runtime data) {
        return data.energy().needsReloadOnFullHeat() && data.gun().gunItem().isOverheatLocked(data.gun().gunStack());
    }

    @SubscribeEvent
    public static void syncAndLoadAmmo(TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            return;
        }
        var rgi = EnergyWeaponData.runtime(event.player.getMainHandItem()).orElse(null);
        if (rgi == null) {
            return;
        }
        var gun = rgi.gun();

        var cap = gun.gunStack().getCapability(ForgeCapabilities.ENERGY).resolve().orElse(null);
        if (cap == null) {
            return;
        }

        var isClient = event.player.level().isClientSide;
        if (!isClient && !gun.gunItem().useDummyAmmo(gun.gunStack())) {
            gun.setDummyAmmoAmount(0);
        }

        if (!isClient) {
            var needsReload = canEnergyWeaponReload(rgi);
            if (needsReload) {
                // 需要换散热器时，将弹匣转移至备弹
                var ammo = gun.getTotalAmmo();
                if (ammo > 0) {
                    gun.gunItem().setCurrentAmmoCount(gun.gunStack(), 0);
                    gun.gunItem().setBulletInBarrel(gun.gunStack(), false);
                    gun.addDummyAmmoAmount(ammo);
                }
            } else {
                // 能开火时，将备弹转移至弹匣
                if (gun.getTotalAmmo() != gun.getTotalMagazineSize()) {
                    var ammo = gun.getDummyAmmoAmount();
                    Gunsmith.magicReload(event.player, gun.gunStack(), ammo);
                }
            }
        }
    }

    @Mod.EventBusSubscriber
    public static class CapAttacher {
        public static final ResourceLocation CAP_ID = GunsmithLib.loc("energy_weapon_cap");

        @SubscribeEvent
        public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event) {
            if (!isEnergyWeapon(event.getObject())) {
                return;
            }
            Gunsmith
                    .getGunInfo(event.getObject())
                    .ifPresent(gunInfo -> event.addCapability(CAP_ID, new CapProvider(gunInfo)));
        }
    }

    public static class CapProvider implements ICapabilityProvider, IEnergyStorage {
        private final ItemStack stack;
        private final ResourceLocation gunId;

        public CapProvider(GunInfo gun) {
            this.stack = gun.gunStack();
            this.gunId = gun.gunId();
        }

        public static final Capability<IEnergyStorage> ENERGY_CAP = ForgeCapabilities.ENERGY;
        public static final String TAG_ENERGY = GunsmithLib.loc("energy_stored").toString();
        private final LazyOptional<IEnergyStorage> CAP_INSTANCE = LazyOptional.of(() -> this);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ENERGY_CAP.orEmpty(cap, CAP_INSTANCE);
        }

        public void setEnergyStored(int value) {
            var data = EnergyWeaponData.runtime(stack).orElse(null);
            if (data == null) {
                stack.getOrCreateTag().putInt(TAG_ENERGY, value);
                return;
            }
            var totalAmmoAmount = value / data.energy().energyPerShot();
            var rem = value - totalAmmoAmount * data.energy().energyPerShot();

            var needsReload = canEnergyWeaponReload(data);
            if (needsReload || totalAmmoAmount == 0) {
                data.gun().gunItem().setCurrentAmmoCount(data.gun().gunStack(), 0);
                data.gun().gunItem().setBulletInBarrel(data.gun().gunStack(), false);
                data.gun().setDummyAmmoAmount(totalAmmoAmount);
            } else {
                var hasBarrel = data.gun().index().getGunData().getBolt() != Bolt.OPEN_BOLT;
                if (hasBarrel) {
                    data.gun().gunItem().setBulletInBarrel(data.gun().gunStack(), true);
                }
                var totalBulletExcludeBarrel = totalAmmoAmount - (hasBarrel ? 1 : 0);
                var gunpackMagSize = AttachmentDataUtils.getAmmoCountWithAttachment(data.gun().gunStack(), data.gun().index().getGunData());
                var magAmmoAmount = Math.min(totalBulletExcludeBarrel, gunpackMagSize);
                var batAmmoAmount = Math.max(totalBulletExcludeBarrel - magAmmoAmount, 0);

                data.gun().gunItem().setCurrentAmmoCount(data.gun().gunStack(), magAmmoAmount);
                data.gun().setDummyAmmoAmount(batAmmoAmount);
            }

            stack.getOrCreateTag().putInt(TAG_ENERGY, rem);
        }

        public int getMaxReceive() {
            return canReceive() ? getChargePower() : 0;
        }

        public int getMaxExtract() {
            return canExtract() ? getChargePower() : 0;
        }

        private int getChargePower() {
            return TimelessAPI.getCommonGunIndex(gunId)
                    .map(CommonGunIndex::getGunData)
                    .flatMap(gd -> ((EnhancedGunData) gd).gunsmith$getGunsmithLibExtension())
                    .map(GunsmithLibGunDataExtension::battery)
                    .map(EnergyWeaponData::chargePower)
                    .orElse(0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (!canReceive())
                return 0;

            int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), Math.min(getMaxReceive(), maxReceive));
            if (!simulate)
                setEnergyStored(getEnergyStored() + energyReceived);
            return energyReceived;
        }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            if (!canExtract())
                return 0;

            int energyExtracted = Math.min(getEnergyStored(), Math.min(getMaxExtract(), maxExtract));
            if (!simulate)
                setEnergyStored(getEnergyStored() - energyExtracted);
            return energyExtracted;
        }

        @Override
        public int getEnergyStored() {
            var frontend = getEnergyInFrontend();
            var backend = getEnergyInBackend();
            var rem = stack.hasTag() ? stack.getOrCreateTag().getInt(TAG_ENERGY) : 0;
            return frontend + backend + rem;
        }

        private int getEnergyInBackend() {
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * data.gun().gunItem().getDummyAmmoAmount(data.gun().gunStack()))
                    .orElse(0);
        }

        private int getEnergyInFrontend() {
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * data.gun().getTotalAmmo())
                    .orElse(0);
        }

        @Override
        public int getMaxEnergyStored() {
            return EnergyWeaponData.runtime(stack)
                    .map(data -> data.energy().energyPerShot() * data.gun().getTotalMagazineSize())
                    .orElse(0);
        }

        @Override
        public boolean canExtract() {
            return false;
        }

        @Override
        public boolean canReceive() {
            return true;
        }
    }
}
