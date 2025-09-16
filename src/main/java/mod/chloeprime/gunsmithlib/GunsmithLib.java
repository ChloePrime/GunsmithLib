package mod.chloeprime.gunsmithlib;

import com.mojang.logging.LogUtils;
import com.mojang.serialization.Codec;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import mod.chloeprime.gunsmithlib.common.entity.AreaEffectCloud3D;
import mod.chloeprime.gunsmithlib.common.entity.RangefinderMarker;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlAttributes;
import mod.chloeprime.gunsmithlib.common.entity.MagicLaser;
import mod.chloeprime.gunsmithlib.common.util.AttackDamageMobEffect;
import mod.chloeprime.gunsmithlib.common.util.LesRaisinsCrashFix;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.common.PercentageAttribute;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;
import org.slf4j.Logger;

import java.awt.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Mod(GunsmithLib.MOD_ID)
public class GunsmithLib {
    public static final String MOD_ID = "gunsmithlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    public GunsmithLib(IEventBus bus, ModContainer container) {
        Attributes.REGISTRY.register(bus);
        FireControlAttributes.init(bus);
        MobEffects.REGISTRY.register(bus);
        SoundEvents.REGISTRY.register(bus);
        EntityTypes.DFR.register(bus);
        bus.addListener(this::commonSetup);
        container.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        // MC1.21+ Only
        DataComponents.DFR.register(bus);
        DataAttachments.DFR.register(bus);
        GunLootFunctions.DFR_FUNC.register(bus);
        GunLootFunctions.DFR_COND.register(bus);
        LesRaisinsCrashFix.init(bus);
    }

    public static ResourceLocation loc(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public static class Attributes {
        private static final Consumer<Attribute> SET_SYNCED = attribute -> attribute.setSyncable(true);
        private static final Consumer<Attribute> SET_NEGATIVE = attribute -> attribute.setSentiment(Attribute.Sentiment.NEGATIVE);
        private static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(BuiltInRegistries.ATTRIBUTE, MOD_ID);

        /**
         * 射击伤害，是每个单片的基础伤害
         */
        public static final DeferredHolder<Attribute, Attribute> BULLET_DAMAGE = create("bullet_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        /**
         * 穿甲倍率
         * @since 4.6.0
         */
        public static final DeferredHolder<Attribute, Attribute> ARMOR_PIERCING_RATIO = createPercentBased("armor_piercing_ratio", 0, 0, 1);

        /**
         * 爆头倍率
         * @since 4.6.0
         */
        public static final DeferredHolder<Attribute, Attribute> HEADSHOT_MULTIPLIER = createPercentBased("headshot_multiplier", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        /**
         * 子弹飞行速度
         */
        public static final DeferredHolder<Attribute, Attribute> BULLET_SPEED = create("bullet_speed", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        public static final DeferredHolder<Attribute, Attribute> H_RECOIL = createPercentBased("horz_recoil", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SET_SYNCED.andThen(SET_NEGATIVE));
        public static final DeferredHolder<Attribute, Attribute> V_RECOIL = createPercentBased("vert_recoil", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SET_SYNCED.andThen(SET_NEGATIVE));

        public static final DeferredHolder<Attribute, Attribute> RPM = create("rpm", 300, 1, 1200, SET_SYNCED);
        public static final DeferredHolder<Attribute, Attribute> AMMO_CAPACITY = create("ammo_capacity", 30, 0, Integer.MAX_VALUE);
        public static final DeferredHolder<Attribute, Attribute> RELOAD_SPEED = createPercentBased("reload_speed", 1, 0, Double.POSITIVE_INFINITY, SET_SYNCED);


        @SuppressWarnings("SameParameterValue")
        private static DeferredHolder<Attribute, Attribute> create(String name, double defaultValue, double min, double max) {
            return create(name, defaultValue, min, max, _a -> {});
        }

        private static DeferredHolder<Attribute, Attribute> create(String name, double defaultValue, double min, double max, Consumer<Attribute> customizer) {
            return REGISTRY.register(name, () -> {
                var attribute = new RangedAttribute(createLangKey(name), defaultValue, min, max);
                customizer.accept(attribute);
                return attribute;
            });
        }

        @SuppressWarnings("SameParameterValue")
        private static DeferredHolder<Attribute, Attribute> createPercentBased(String name, double defaultValue, double min, double max) {
            return createPercentBased(name, defaultValue, min, max, attribute -> {});
        }

        private static DeferredHolder<Attribute, Attribute> createPercentBased(String name, double defaultValue, double min, double max, Consumer<Attribute> customizer) {
            return REGISTRY.register(name, () -> {
                var attribute = new PercentageAttribute(createLangKey(name), defaultValue, min, max);
                customizer.accept(attribute);
                return attribute;
            });
        }

        private static String createLangKey(String name) {
            return "attribute.name.%s.%s".formatted(MOD_ID, name);
        }
    }

    public static class MobEffects {
        private static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(BuiltInRegistries.MOB_EFFECT, MOD_ID);
        public static final DeferredHolder<MobEffect, MobEffect> GUN_DAMAGE = REGISTRY.register("crossfire", () -> new AttackDamageMobEffect(MobEffectCategory.BENEFICIAL, Color.LIGHT_GRAY, Config.CROSSFIRE_BUFF_POWER::get)
                .addAttributeModifier(GunAttributes.BULLET_DAMAGE, loc("crossfire_buff"), 0, AttributeModifier.Operation.ADD_MULTIPLIED_TOTAL));
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, MOD_ID);
        public static final DeferredHolder<SoundEvent, SoundEvent> SHIELD_BLOCKS_BULLET = REGISTRY.register("shield_blocks_bullet", () -> SoundEvent.createVariableRangeEvent(loc( "shield_blocks_bullet")));
    }

    public static class EntityTypes {
        private static final DeferredRegister<EntityType<?>> DFR = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
        public static final Supplier<EntityType<MagicLaser>> MAGIC_LASER = DFR.register("magic_laser", () -> MagicLaser.TYPE);
        public static final Supplier<EntityType<RangefinderMarker>> RANGEFINDER_MARKER = DFR.register("rangefinder_marker", () -> RangefinderMarker.TYPE);
        public static final Supplier<EntityType<AreaEffectCloud3D>> AREA_EFFECT_CLOUD_3D = DFR.register("area_effect_cloud_3d", () -> AreaEffectCloud3D.TYPE);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        checkKnownIncompatibilities();
        event.enqueueWork(GunLootFunctions::init);
    }

    private void checkKnownIncompatibilities() {
        // There will be no more TaCZ Fire Control Extension on MC1.21
    }

    // MC1.21+ Only
    public static class DataComponents {
        private static final DeferredRegister<DataComponentType<?>> DFR = DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, MOD_ID);
        public static final Supplier<DataComponentType<Integer>> ENERGY_STORED = DFR.register("energy_stored", () -> DataComponentType.<Integer>builder()
                .persistent(Codec.INT)
                .networkSynchronized(ByteBufCodecs.INT)
                .build());
        public static final Supplier<DataComponentType<ItemAttributeModifiers>> ATTACHMENT_ATTRIBUTES = DFR.register("attachment_attribute_modifiers", () -> DataComponentType.<ItemAttributeModifiers>builder()
                .persistent(ItemAttributeModifiers.CODEC)
                .networkSynchronized(ItemAttributeModifiers.STREAM_CODEC)
                .build());
    }

    public static class DataAttachments {
        private static final DeferredRegister<AttachmentType<?>> DFR = DeferredRegister.create(NeoForgeRegistries.ATTACHMENT_TYPES, MOD_ID);
        public static final Supplier<AttachmentType<Integer>> AMMO_IN_BACKPACK = DFR.register("ammo_in_backpack", () -> AttachmentType
                .builder(() -> -1)
                .serialize(Codec.INT)
                .sync(ByteBufCodecs.INT)
                .build());
    }
}
