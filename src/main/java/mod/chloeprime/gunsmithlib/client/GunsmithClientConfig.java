package mod.chloeprime.gunsmithlib.client;

import net.neoforged.neoforge.common.ModConfigSpec;

public class GunsmithClientConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * @since 5.9.3
     */
    public static final ModConfigSpec.BooleanValue IMPROVE_TRACER_START_POSITION_WHEN_AIMING = BUILDER
            .comment("""
                    If true, when shoot during aiming and has a scope installed,
                    make tracer start under the scope, instead of from the center of the scope.
                    
                    Added in version 5.9.3""")
            .define("improve_tracer_start_position_when_aiming", true);

    /**
     * @since 6.2.0
     */
    public static final ModConfigSpec.BooleanValue HINT_ARCANA_INSTALLATION = BUILDER
            .comment("""
                    If true, hint users to install TaCZ Arcana for weapons that use its ammo type system.""")
            .define("hint_arcana_installation", true);

    public enum AmmoTypeHudMode {
        DISABLE,
        ENABLE,
        STYLED,
    }

    /**
     * @since 6.2.0
     */
    public static final ModConfigSpec.EnumValue<AmmoTypeHudMode> HUD_AMMO_TYPE_NAME_OPTION = BUILDER
            .comment("""
                    How to display the current ammo type's name.
                    - DISABLE: Show TaCZ's version (vanilla behavior)
                    - ENABLE:  Show the ammo type's name with original color
                    - STYLED:  Show the ammo type's name with unified color
                    
                    Added in version 6.2.0""")
            .defineEnum("hud_ammo_type_name_option", AmmoTypeHudMode.STYLED);

    /**
     * @since 6.2.0
     */
    public static final ModConfigSpec.EnumValue<AmmoTypeHudMode> HUD_AMMO_TYPE_ICON_OPTION = BUILDER
            .comment("""
                    How to display the current ammo type's icon.
                    - DISABLE: Don't display
                    - ENABLE:  Show the ammo type's slot texture
                    - STYLED:  Show the ammo type's slot texture with a shader that makes it more suitable with other parts of the gun HUD
                    
                    Warning: Incompatible with TaCZ Presence
                    Added in version 6.2.0""")
            .defineEnum("hud_ammo_type_icon_option", AmmoTypeHudMode.STYLED);


    static final ModConfigSpec SPEC = BUILDER.build();
}
