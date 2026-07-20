package mod.chloeprime.gunsmithlib.compat;

import net.minecraftforge.fml.ModList;

import java.util.Objects;

public final class ModInstallationStatus {
    public static final String ARCANA_ID = "taczexpands";
    public static final String AAA_PARTICLES_ID = "aaa_particles";
    public static final String TACZ_PRESENCE_ID = "tacz_presence";
    public static final boolean ARCANA_INSTALLED = isLoaded(ARCANA_ID);
    public static final boolean AAA_PARTICLES_INSTALLED = isLoaded(AAA_PARTICLES_ID);
    public static final boolean TACZ_PRESENCE_INSTALLED = isLoaded(TACZ_PRESENCE_ID);

    private static boolean isLoaded(String id) {
        return Objects.requireNonNull(ModList.get()).isLoaded(id);
    }

    private ModInstallationStatus() {
    }
}
