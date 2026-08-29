package mod.chloeprime.gunsmithlib;

import com.mojang.logging.LogUtils;
import mod.chloeprime.gunsmithlib.api.common.GunAttributes;
import mod.chloeprime.gunsmithlib.api.common.GunLootFunctions;
import mod.chloeprime.gunsmithlib.api.common.recipe.GunRecipeSerializers;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.entity.AreaEffectCloud3D;
import mod.chloeprime.gunsmithlib.common.entity.RangefinderMarker;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.fire_control.FireControlAttributes;
import mod.chloeprime.gunsmithlib.common.entity.MagicLaser;
import mod.chloeprime.gunsmithlib.common.util.AttackDamageMobEffect;
import mod.chloeprime.gunsmithlib.common.util.PercentBasedAttribute;
import mod.chloeprime.gunsmithlib.network.ModNetwork;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.ApiStatus;
import org.slf4j.Logger;

import java.awt.*;
import java.util.function.Consumer;

@Mod(GunsmithLib.MOD_ID)
public class GunsmithLib {

    public static final String MOD_ID = "gunsmithlib";
    public static final Logger LOGGER = LogUtils.getLogger();

    @SuppressWarnings("removal")
    public GunsmithLib() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();

        Attributes.REGISTRY.register(bus);
        FireControlAttributes.init(bus);
        MobEffects.REGISTRY.register(bus);
        SoundEvents.REGISTRY.register(bus);
        EntityTypes.DFR.register(bus);
        GunRecipeSerializers.init(bus);
        bus.addListener(this::commonSetup);
        var loadContext = ModLoadingContext.get();
        loadContext.registerConfig(ModConfig.Type.COMMON, Config.SPEC);

        if (FMLLoader.getDist().isClient()) {
            GunsmithLibClient.onClientConstruct(loadContext::registerConfig);
        }
    }

    public static ResourceLocation loc(String path) {
        return new ResourceLocation(MOD_ID, path);
    }

    public static ResourceLocation loc(String namespace, String path) {
        return new ResourceLocation(namespace, path);
    }

    public static class Attributes {
        private static final Consumer<Attribute> SET_SYNCED = attribute -> attribute.setSyncable(true);
        private static final DeferredRegister<Attribute> REGISTRY = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, MOD_ID);

        /**
         * 射击伤害，是每个单片的基础伤害
         */
        public static final RegistryObject<Attribute> BULLET_DAMAGE = create("bullet_damage", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        /**
         * 穿甲倍率
         * @since 4.6.0
         */
        public static final RegistryObject<Attribute> ARMOR_PIERCING_RATIO = createPercentBased("armor_piercing_ratio", 0, 0, 1);

        /**
         * 爆头倍率
         * @since 4.6.0
         */
        public static final RegistryObject<Attribute> HEADSHOT_MULTIPLIER = createPercentBased("headshot_multiplier", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY);

        /**
         * 子弹飞行速度
         */
        public static final RegistryObject<Attribute> BULLET_SPEED = create("bullet_speed", 0, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SET_SYNCED);

        public static final RegistryObject<Attribute> H_RECOIL = createPercentBased("horz_recoil", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SET_SYNCED);
        public static final RegistryObject<Attribute> V_RECOIL = createPercentBased("vert_recoil", 1, Double.NEGATIVE_INFINITY, Double.POSITIVE_INFINITY, SET_SYNCED);

        public static final RegistryObject<Attribute> RPM = create("rpm", 300, 1, 1200, SET_SYNCED);
        public static final RegistryObject<Attribute> AMMO_CAPACITY = create("ammo_capacity", 30, 0, Integer.MAX_VALUE);
        public static final RegistryObject<Attribute> RELOAD_SPEED = createPercentBased("reload_speed", 1, 0, Double.POSITIVE_INFINITY, SET_SYNCED);

        @ApiStatus.Internal
        public static final RegistryObject<Attribute> AMMO_IN_BACKPACK = create("internal_do_not_use_0", -1, -1, Integer.MAX_VALUE, SET_SYNCED);


        @SuppressWarnings("SameParameterValue")
        private static RegistryObject<Attribute> create(String name, double defaultValue, double min, double max) {
            return create(name, defaultValue, min, max, _a -> {});
        }

        private static RegistryObject<Attribute> create(String name, double defaultValue, double min, double max, Consumer<Attribute> customizer) {
            return REGISTRY.register(name, () -> {
                var attribute = new RangedAttribute(createLangKey(name), defaultValue, min, max);
                customizer.accept(attribute);
                return attribute;
            });
        }

        @SuppressWarnings("SameParameterValue")
        private static RegistryObject<Attribute> createPercentBased(String name, double defaultValue, double min, double max) {
            return createPercentBased(name, defaultValue, min, max, attribute -> {});
        }

        private static RegistryObject<Attribute> createPercentBased(String name, double defaultValue, double min, double max, Consumer<Attribute> customizer) {
            return REGISTRY.register(name, () -> {
                var attribute = new PercentBasedAttribute(createLangKey(name), defaultValue, min, max);
                customizer.accept(attribute);
                return attribute;
            });
        }

        private static String createLangKey(String name) {
            return "attribute.name.%s.%s".formatted(MOD_ID, name);
        }
    }

    public static class MobEffects {
        private static final DeferredRegister<MobEffect> REGISTRY = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, MOD_ID);
        public static final RegistryObject<MobEffect> GUN_DAMAGE = REGISTRY.register("crossfire", () -> new AttackDamageMobEffect(MobEffectCategory.BENEFICIAL, Color.LIGHT_GRAY, Config.CROSSFIRE_BUFF_POWER::get)
                .addAttributeModifier(GunAttributes.BULLET_DAMAGE.get(), "57de873d-44fe-4d65-b1e7-371143916e9e", 0, AttributeModifier.Operation.MULTIPLY_TOTAL));
    }

    public static class SoundEvents {
        private static final DeferredRegister<SoundEvent> REGISTRY = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, MOD_ID);
        public static final RegistryObject<SoundEvent> SHIELD_BLOCKS_BULLET = REGISTRY.register("shield_blocks_bullet", () -> SoundEvent.createVariableRangeEvent(loc( "shield_blocks_bullet")));
        public static final RegistryObject<SoundEvent> BALLISTIC_COMPUTER = REGISTRY.register("ballistic_computer", () -> SoundEvent.createVariableRangeEvent(loc( "ballistic_computer")));
    }

    public static class EntityTypes {
        private static final DeferredRegister<EntityType<?>> DFR = DeferredRegister.create(Registries.ENTITY_TYPE, MOD_ID);
        public static final RegistryObject<EntityType<MagicLaser>> MAGIC_LASER = DFR.register("magic_laser", () -> MagicLaser.TYPE);
        public static final RegistryObject<EntityType<RangefinderMarker>> RANGEFINDER_MARKER = DFR.register("rangefinder_marker", () -> RangefinderMarker.TYPE);
        public static final RegistryObject<EntityType<AreaEffectCloud3D>> AREA_EFFECT_CLOUD_3D = DFR.register("area_effect_cloud_3d", () -> AreaEffectCloud3D.TYPE);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        checkKnownIncompatibilities();
        event.enqueueWork(() -> {
            ModNetwork.init();
            GunLootFunctions.init();
        });
    }

    private void checkKnownIncompatibilities() {
        if (ModList.get().isLoaded("tacz_fire_control_extension")) {
            throw new UnsupportedOperationException("""
                    
                    This version of GunsmithLib contains the same functionality and is incompatible with TaCZ Fire Control Extension.
                    Please remove TaCZ Fire Control Extension, this will not break your game functionality.
                    
                    此版本的 GunsmithLib 已包括 TaCZ Fire Control Extension 的内容，且与该模组不兼容。
                    请删除 TaCZ Fire Control Extension, 放心，这不会让火控功能失效。""");
        }
    }
}
