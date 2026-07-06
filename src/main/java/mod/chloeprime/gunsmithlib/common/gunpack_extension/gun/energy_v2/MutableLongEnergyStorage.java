package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

public interface MutableLongEnergyStorage extends LongEnergyStorage {
    /**
     * Updates the amount of energy currently stored.
     */
    void setEnergyStoredL(long value);
}
