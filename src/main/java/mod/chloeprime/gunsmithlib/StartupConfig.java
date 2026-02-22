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
            Fully disable TaCZ Pack Upgrader.
            Note: This will also disable TaCZ Pack Upgrader that was installed independently.
            You should use another Minecraft client to run the pack upgrader to get upgraded packs.
            
            1.21.1 Only
            Added in version 5.1.7""")
            .define("disable_pack_upgrader", false);

    static final ModConfigSpec SPEC = BUILDER.build();
}
