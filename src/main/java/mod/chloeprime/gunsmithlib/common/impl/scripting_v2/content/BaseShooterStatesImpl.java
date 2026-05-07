package mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content;

import com.tacz.guns.api.entity.IGunOperator;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.PotionEffectInstanceView;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ShooterStates;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.Optional;

/**
 * @since 6.0.0
 */
public class BaseShooterStatesImpl extends EntityStatesImpl implements ShooterStates {
    protected final LivingEntity shooter;
    protected final IGunOperator operator;

    public BaseShooterStatesImpl(LivingEntity shooter) {
        super(shooter);
        this.shooter = shooter;
        this.operator = IGunOperator.fromLivingEntity(shooter);
    }

    // Vanilla

    public Vector2f get_head_rotation() {
        return new Vector2f(shooter.getXRot(), shooter.getYHeadRot());
    }

    @Override
    public Vector2f get_body_rotation() {
        return new Vector2f(0, shooter.yBodyRot);
    }

    @Override
    public double get_attribute_value(String attributeName) {
        return getRegistryObjectChecked(Registries.ATTRIBUTE, attributeName, "attribute")
                .map(shooter::getAttributeValue)
                .orElse(0.0);
    }

    @Override
    public double get_attribute_base_value(String attributeName) {
        return getRegistryObjectChecked(Registries.ATTRIBUTE, attributeName, "attribute")
                .map(shooter::getAttributeBaseValue)
                .orElse(0.0);
    }

    @Override
    public @Nullable PotionEffectInstanceView get_potion_effect(String effectName) {
        return getRegistryObjectChecked(Registries.MOB_EFFECT, effectName, "potion effect")
                .map(shooter::getEffect)
                .map(MobEffectInstanceWrapper::new)
                .orElse(null);
    }

    private <T> Optional<Holder<T>> getRegistryObjectChecked(ResourceKey<Registry<T>> registry, String name, String registryDebugLabel) {
        var id = ResourceLocation.tryParse(name);
        if (id == null) {
            GunsmithLib.LOGGER.error("Incorrect {} name \"{}\", should be a valid resource location", registryDebugLabel, name);
            return Optional.empty();
        }
        var key = ResourceKey.create(registry, id);
        var attribute = shooter.level().registryAccess()
                .registry(registry)
                .flatMap(regInstance -> regInstance.getHolder(key))
                .orElse(null);
        if (attribute == null) {
            GunsmithLib.LOGGER.error("Unknown {} \"{}\"", registryDebugLabel, name);
            return Optional.empty();
        }
        return Optional.of(attribute);
    }

    @Override
    public float get_health() {
        return shooter.getHealth();
    }

    @Override
    public float get_max_health() {
        return shooter.getMaxHealth();
    }

    @Override
    public double get_armor() {
        return shooter.getAttributeValue(Attributes.ARMOR);
    }

    @Override
    public double get_armor_toughness() {
        return shooter.getAttributeValue(Attributes.ARMOR_TOUGHNESS);
    }

    @Override
    public double get_movement_speed() {
        return shooter.getSpeed();
    }

    @Override
    public boolean is_on_climbable() {
        return shooter.onClimbable();
    }

    @Override
    public boolean is_baby() {
        return shooter.isBaby();
    }

    @Override
    public float get_scale() {
        return shooter.getScale();
    }

    @Override
    public boolean is_elytra_flying() {
        return shooter.isFallFlying();
    }

    @Override
    public int get_elytra_flying_ticks() {
        return shooter.getFallFlyingTicks();
    }

    // TaCZ

    @Override
    public boolean is_bolting() {
        return operator.getSynIsBolting();
    }

    @Override
    public String reload_state() {
        return operator.getSynReloadState().getStateType().name();
    }

    @Override
    public long reload_countdown_millis() {
        return operator.getSynReloadState().getCountDown();
    }

    @Override
    public boolean is_aiming() {
        return operator.getSynIsAiming();
    }

    @Override
    public float aiming_progress() {
        return operator.getSynAimingProgress();
    }

    @Override
    public float sprint_time() {
        return operator.getSynSprintTime();
    }

    @Override
    public long shoot_cooldown_millis() {
        return operator.getSynShootCoolDown();
    }

    @Override
    public long melee_cooldown_millis() {
        return operator.getSynMeleeCoolDown();
    }

    @Override
    public long draw_cooldown_millis() {
        return operator.getSynDrawCoolDown();
    }

    @Override
    public Vector3d get_movement_input() {
        if (shooter instanceof ServerPlayer) {
            var msg = "get_movement_input() is not functional for server players. Use is_moving() instead.";
            GunsmithLib.LOGGER.warn(msg);
        }
        return new Vector3d(shooter.xxa, shooter.yya, shooter.zza);
    }
}
