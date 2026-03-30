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

    static final ModConfigSpec SPEC = BUILDER.build();
}
