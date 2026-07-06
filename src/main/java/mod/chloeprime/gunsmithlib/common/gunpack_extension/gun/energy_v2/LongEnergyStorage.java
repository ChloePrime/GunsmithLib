package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

import net.minecraftforge.energy.IEnergyStorage;

public interface LongEnergyStorage extends IEnergyStorage {
    /**
     * Adds energy to the storage. Returns quantity of energy that was accepted.
     *
     * @param maxReceive
     *            Maximum amount of energy to be inserted.
     * @param simulate
     *            If TRUE, the insertion will only be simulated.
     * @return Amount of energy that was (or would have been, if simulated) accepted by the storage.
     */
    long receiveEnergyL(long maxReceive, boolean simulate);

    /**
     * Removes energy from the storage. Returns quantity of energy that was removed.
     *
     * @param maxExtract
     *            Maximum amount of energy to be extracted.
     * @param simulate
     *            If TRUE, the extraction will only be simulated.
     * @return Amount of energy that was (or would have been, if simulated) extracted from the storage.
     */
    long extractEnergyL(long maxExtract, boolean simulate);

    /**
     * Returns the amount of energy currently stored.
     */
    long getEnergyStoredL();

    /**
     * Returns the maximum amount of energy that can be stored.
     */
    long getMaxEnergyStoredL();

    @Override
    @Deprecated
    default int receiveEnergy(int maxReceive, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, receiveEnergyL(maxReceive, simulate));
    }

    @Deprecated
    @Override
    default int extractEnergy(int maxExtract, boolean simulate) {
        return (int) Math.min(Integer.MAX_VALUE, extractEnergyL(maxExtract, simulate));
    }

    @Deprecated
    @Override
    default int getEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getEnergyStoredL());
    }

    @Deprecated
    @Override
    default int getMaxEnergyStored() {
        return (int) Math.min(Integer.MAX_VALUE, getMaxEnergyStoredL());
    }
}
