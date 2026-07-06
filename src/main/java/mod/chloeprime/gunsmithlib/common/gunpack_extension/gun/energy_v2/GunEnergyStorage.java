package mod.chloeprime.gunsmithlib.common.gunpack_extension.gun.energy_v2;

public interface GunEnergyStorage extends MutableLongEnergyStorage {
    long privilegedReceiveEnergyL(long maxReceive, boolean simulate);
    long privilegedExtractEnergyL(long maxExtract, boolean simulate);
}
