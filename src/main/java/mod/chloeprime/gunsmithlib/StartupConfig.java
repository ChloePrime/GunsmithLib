package mod.chloeprime.gunsmithlib;

import net.neoforged.neoforge.common.ModConfigSpec;

/**
 * @since 5.1.7
 */
public class StartupConfig {
    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    /**
     * @since 5.1.7
     */
    public static final ModConfigSpec.BooleanValue DISABLE_PACK_UPGRADER = BUILDER
            .comment("""
            Fully disable GunsmithLib's automatic gun pack upgrading.
            Packs are upgraded with TaCZ's built-in universal converter, the same implementation behind /tacz universal.
            You can also convert packs manually with /tacz universal <pack> export and install the result yourself.
            
            1.21.1 Only
            Added in version 5.1.7
            Switched to TaCZ's built-in universal converter in version 7.0.0""")
            .define("disable_pack_upgrader", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
