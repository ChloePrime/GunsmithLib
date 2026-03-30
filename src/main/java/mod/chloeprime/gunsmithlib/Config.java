package mod.chloeprime.gunsmithlib;

import net.neoforged.neoforge.common.ModConfigSpec;

public class Config
{
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.DoubleValue CROSSFIRE_BUFF_POWER = BUILDER
            .comment("Amount of damage scale boost per level of Crossfire buff gives")
            .defineInRange("crossfire_buff_power", 0.3, Double.MIN_NORMAL, Double.MAX_VALUE);

    public static final ModConfigSpec.BooleanValue ENABLE_SPECIAL_HURT = BUILDER
            .comment("Enable calling hurt function with invokespecial on entities with a specific tag")
            .define("enable_special_hurt", true);

    public static final ModConfigSpec.BooleanValue ENABLE_REMOVE_INTERCEPTION = BUILDER
            .comment("If true, intercept suspicious attempts to remove bullet entities.")
            .define("enable_remove_interception", true);

    /**
     * @since 3.2.1
     */
    public static final ModConfigSpec.BooleanValue USE_ATTACK_DAMAGE = BUILDER
            .comment("If true, gun damage will be affect by `minecraft:generic.attack_damage` attribute, then use the result as base value for `gunsmithlib:bullet_damage` attribute")
            .define("use_attack_damage", true);

    /**
     * @since 4.4.2
     */
    public static final ModConfigSpec.BooleanValue ALTERNATIVE_ARMOR_PIERCING_FORMULA = BUILDER
            .comment("""
                    If true, guns' armor piercing mechanic will have only one hit with target armor reduced, instead of dealing two hits.
                    This can fix the bug that Apotheosis's magic armor can reduce armor-piercing damage,
                    and make Cataclysm's dynamic damage reduction work properly for guns.
                    
                    Added in version 4.4.2""")
            .define("alt_ap_formula", true);
    /**
     * @since 4.4.3
     */
    public static final ModConfigSpec.BooleanValue INTERACT_KEY_INFERENCING = BUILDER
            .comment("""
                    Auto inference whether a block or entity is interactable by its method overriding status.
                    This is one of the core technologies in my mod TacInteractKey (https://www.curseforge.com/minecraft/mc-mods/tac-interact-key),
                    which is missing in the interact key functionality of TaCZ itself.
                    
                    Added in version 4.4.3""")
            .define("interact_key_inferencing", true);

    public static final ModConfigSpec.BooleanValue IMPROVE_TRACER_ROTATION_STABILITY = BUILDER
            .comment("""
                    Disable server side position correction for bullets.
                    This fixes tracer effect being randomly rotated after initial tick.
                    If unsure, keep this true.
                    
                    Added in version 5.9.3""")
            .define("improve_tracer_rotation_stability", true);

    static final ModConfigSpec SPEC = BUILDER.build();
}
