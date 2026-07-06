package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

import com.mojang.authlib.GameProfile;
import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.api.item.IGun;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.ICapabilityProvider;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * @since 6.1.0
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

    @EventBusSubscriber
    public static final class CapAttacher implements ICapabilityProvider<ItemStack, @Nullable Void, IEnergyStorage> {
        @Override
        public @Nullable IEnergyStorage getCapability(@Nonnull ItemStack stack, @Nullable Void unused) {
            var config = EnergyWeaponV2Data.of(stack).orElse(null);
            if (config == null) {
                return null;
            }
            return new CapProvider(stack, config);
        }

        @SubscribeEvent
        public static void onAttachCaps(RegisterCapabilitiesEvent event) {
            for (Item item : BuiltInRegistries.ITEM) {
                if (item instanceof IGun) {
                    event.registerItem(Capabilities.EnergyStorage.ITEM, new CapAttacher(), item);
                }
            }
        }

        private CapAttacher() {
        }
    }

    public static class CapProvider implements GunEnergyStorage {
        private final ItemStack stack;
        private final EnergyWeaponV2Data config;

        public CapProvider(ItemStack stack, EnergyWeaponV2Data config) {
            this.stack = stack;
            this.config = config;
        }

        public static final Supplier<DataComponentType<Long>> TAG_ENERGY = GunsmithLib.DataComponents.ENERGY_STORED_V2;

        @Override
        public long getEnergyStoredL() {
            return stack.getOrDefault(TAG_ENERGY, 0L);
        }

        @Override
        public void setEnergyStoredL(long value) {
            stack.set(TAG_ENERGY, value);
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
