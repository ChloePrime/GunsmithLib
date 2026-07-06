package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

import com.tacz.guns.api.ValueModifiableAtRuntime;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.GunInfo;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.GunsmithLibGunDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.Optional;

/**
 * @since 6.1.0
 */
public class EnergyWeaponV2Data {
    @GunpackProperty
    private long capacity;

    /**
     * 最大充电速度，为 0 时将不能充电。
     * 单位：FE/t。
     */
    @GunpackProperty
    private long max_input_speed;

    /**
     * 最大放电速度，为 0 时将不能放电。
     * 单位：FE/t。
     */
    @GunpackProperty
    private long max_output_speed;

    /**
     * 如果为 true，那么电池容量可以被 modify property 修改。
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @GunpackProperty
    private boolean dynamic_capacity = false;

    /**
     * 如果为 true，那么充电速度可以被 modify property 修改。
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @GunpackProperty
    private boolean dynamic_max_input_speed = false;

    /**
     * 如果为 true，那么放电速度可以被 modify property 修改。
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @GunpackProperty
    private boolean dynamic_max_output_speed = false;

    /**
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @ValueModifiableAtRuntime(Long.class)
    public static final String PROP_NAME_MAX_CAPACITY = "%s_energy_capacity".formatted(GunsmithLib.MOD_ID);

    /**
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @ValueModifiableAtRuntime(Long.class)
    public static final String PROP_NAME_MAX_INPUT_SPEED = "%s_max_energy_input_speed".formatted(GunsmithLib.MOD_ID);

    /**
     * 注：对电池相关属性 modify property 时 api 中的 shooter 可能为假玩家。
     */
    @ValueModifiableAtRuntime(Long.class)
    public static final String PROP_NAME_MAX_OUTPUT_SPEED = "%s_max_energy_output_speed".formatted(GunsmithLib.MOD_ID);

    // 下面是面向 Java 的部分

    public static Optional<EnergyWeaponV2Data> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun).flatMap(EnergyWeaponV2Data::of);
    }

    public static Optional<EnergyWeaponV2Data> of(GunInfo gun) {
        return GunsmithLibGunDataExtension.of(gun).flatMap(GunsmithLibGunDataExtension::getVersion2BatteryData);
    }

    public long getStaticCapacity() {
        return capacity;
    }

    public long getStaticMaxInputSpeed() {
        return max_input_speed;
    }

    public long getStaticMaxOutputSpeed() {
        return max_output_speed;
    }

    public boolean isCapacityDynamic() {
        return dynamic_capacity;
    }

    public boolean isInputSpeedDynamic() {
        return dynamic_max_input_speed;
    }

    public boolean isOutputSpeedDynamic() {
        return dynamic_max_output_speed;
    }

    public long getCapacity(@Nonnull ItemStack gunStack, @Nullable LivingEntity shooter) {
        Objects.requireNonNull(gunStack);
        if (!isCapacityDynamic()) {
            return getStaticCapacity();
        }
        return EnergyWeaponV2System.DynamicSpeedFactory.getModifiedValue(gunStack, shooter, PROP_NAME_MAX_CAPACITY, getStaticCapacity());
    }

    public long getMaxInputSpeed(@Nonnull ItemStack gunStack, @Nullable LivingEntity shooter) {
        Objects.requireNonNull(gunStack);
        if (!isInputSpeedDynamic()) {
            return getStaticMaxInputSpeed();
        }
        return EnergyWeaponV2System.DynamicSpeedFactory.getModifiedValue(gunStack, shooter, PROP_NAME_MAX_INPUT_SPEED, getStaticMaxInputSpeed());
    }

    public long getMaxOutputSpeed(@Nonnull ItemStack gunStack, @Nullable LivingEntity shooter) {
        Objects.requireNonNull(gunStack);
        if (!isOutputSpeedDynamic()) {
            return getStaticMaxOutputSpeed();
        }
        return EnergyWeaponV2System.DynamicSpeedFactory.getModifiedValue(gunStack, shooter, PROP_NAME_MAX_OUTPUT_SPEED, getStaticMaxOutputSpeed());
    }
}
