package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

import com.mojang.authlib.GameProfile;
import com.tacz.guns.api.entity.IGunOperator;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.FakePlayerFactory;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.event.AttachCapabilitiesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

/**
 * @since 6.2
 */
public final class EnergyWeaponV2System {
    public static boolean isEnergyWeapon(ItemStack candidate) {
        return EnergyWeaponV2Data.of(candidate).isPresent();
    }

    public static boolean isEnergyWeapon(GunInfo candidate) {
        return EnergyWeaponV2Data.of(candidate).isPresent();
    }

    public static final class DynamicSpeedFactory {
        private static final GameProfile BATTERY_ACCOUNTANT = new GameProfile(UUID.fromString("6f1e2e8d-006c-4d95-9791-f2142eed7e5f"), "[Battery Accountant]");

        public static long getModifiedValue(@Nonnull ItemStack gunStack, @Nullable LivingEntity contextEntity, @Nonnull String propName, long original) {
            Objects.requireNonNull(gunStack);
            Objects.requireNonNull(propName);

            var level = contextEntity != null
                    ? contextEntity.level()
                    : Optional.ofNullable(ServerLifecycleHooks.getCurrentServer()).map(MinecraftServer::overworld).orElse(null);
            if (!(level instanceof ServerLevel srvLevel)) {
                return 0;
            }
            var gunInfo = Gunsmith.getGunInfo(gunStack).orElse(null);
            if (gunInfo == null) {
                return 0;
            }
            var shooter = contextEntity != null
                    ? contextEntity
                    : FakePlayerFactory.get(srvLevel, BATTERY_ACCOUNTANT);
            var dataHolder = Objects.requireNonNull(IGunOperator.fromLivingEntity(shooter).getDataHolder());
            return gunInfo.gunItem().modifyProperty(dataHolder, gunInfo.gunStack(), shooter, propName, Long.class, original);
        }

        private DynamicSpeedFactory() {
        }
    }

    @Mod.EventBusSubscriber
    public static final class CapAttacher {
        public static final ResourceLocation CAP_ID = GunsmithLib.loc("energy_weapon_v2_cap");

        @SubscribeEvent
        public static void onAttachCaps(AttachCapabilitiesEvent<ItemStack> event) {
            var stack = event.getObject();
            var config = EnergyWeaponV2Data.of(stack).orElse(null);
            if (config == null) {
                return;
            }
            event.addCapability(CAP_ID, new CapProvider(stack, config));
        }

        private CapAttacher() {
        }
    }

    public static class CapProvider implements ICapabilityProvider, GunEnergyStorage {
        private final ItemStack stack;
        private final EnergyWeaponV2Data config;

        public CapProvider(ItemStack stack, EnergyWeaponV2Data config) {
            this.stack = stack;
            this.config = config;
        }

        public static final Capability<IEnergyStorage> ENERGY_CAP = ForgeCapabilities.ENERGY;
        public static final String TAG_ENERGY = GunsmithLib.loc("energy_v2_stored").toString();
        private final LazyOptional<IEnergyStorage> capInstance = LazyOptional.of(() -> this);

        @Override
        public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
            return ENERGY_CAP.orEmpty(cap, capInstance);
        }

        @Override
        public long getEnergyStoredL() {
            return Optional.ofNullable(stack.getTag())
                    .map(tag -> tag.getLong(TAG_ENERGY))
                    .orElse(0L);
        }

        @Override
        public void setEnergyStoredL(long value) {
            stack.getOrCreateTag().putLong(TAG_ENERGY, value);
        }

        @Override
        public long getMaxEnergyStoredL() {
            return config.getCapacity(this.stack, null);
        }

        public long getMaxReceive() {
            return config.getMaxInputSpeed(this.stack, null);
        }

        public long getMaxExtract() {
            return config.getMaxOutputSpeed(this.stack, null);
        }

        @Override
        public long extractEnergyL(long maxExtract, boolean simulate) {
            return extractEnergyL(maxExtract, simulate, false);
        }

        @Override
        public long receiveEnergyL(long maxReceive, boolean simulate) {
            return receiveEnergyL(maxReceive, simulate, false);
        }

        @Override
        public long privilegedExtractEnergyL(long maxExtract, boolean simulate) {
            return extractEnergyL(maxExtract, simulate, true);
        }

        @Override
        public long privilegedReceiveEnergyL(long maxReceive, boolean simulate) {
            return receiveEnergyL(maxReceive, simulate, true);
        }

        private long extractEnergyL(long wantedAmount, boolean simulate, boolean privileged) {
            long maxExtract = privileged ? Long.MAX_VALUE : getMaxExtract();
            if (maxExtract <= 0) {
                return 0;
            }

            long energy = getEnergyStoredL();
            long energyExtracted = Math.min(energy, Math.min(maxExtract, wantedAmount));
            if (!simulate) {
                setEnergyStoredL(energy - energyExtracted);
            }
            return energyExtracted;
        }

        private long receiveEnergyL(long givenAmount, boolean simulate, boolean privileged) {
            long maxReceive = privileged ? Long.MAX_VALUE : getMaxReceive();
            if (maxReceive <= 0) {
                return 0;
            }

            long capacity = config.getCapacity(this.stack, null);
            long energy = getEnergyStoredL();
            long energyReceived = Math.min(capacity - energy, Math.min(maxReceive, givenAmount));
            if (!simulate) {
                setEnergyStoredL(energy + energyReceived);
            }
            return energyReceived;
        }

        @Override
        public boolean canExtract() {
            return getMaxExtract() > 0;
        }

        @Override
        public boolean canReceive() {
            return getMaxReceive() > 0;
        }
    }

    private EnergyWeaponV2System() {
    }
}
