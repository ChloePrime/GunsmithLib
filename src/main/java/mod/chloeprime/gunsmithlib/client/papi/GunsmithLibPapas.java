package mod.chloeprime.gunsmithlib.client.papi;

import com.tacz.guns.client.model.papi.PapiManager;

public final class GunsmithLibPapas {

    public static void registerPapas() {
        PapiManager.addPapi(AirburstDistancePapi.NAME, AirburstDistancePapi.INSTANCE);
        PapiManager.addPapi(AmmoStockPapi.NAME, AmmoStockPapi.INSTANCE);
        PapiManager.addPapi(HeatPercentPapi.NAME, HeatPercentPapi.INSTANCE);
        PapiManager.addPapi(PlayerPosPapi.NAME, PlayerPosPapi.INSTANCE);
        PapiManager.addPapi(RangefinderPapi.NAME, RangefinderPapi.INSTANCE);
        PapiManager.addPapi(TargetNamePapi.NAME, TargetNamePapi.INSTANCE);
        PapiManager.addPapi(TargetPosPapi.NAME, TargetPosPapi.INSTANCE);
        PapiManager.addPapi(TargetSizePapi.NAME, TargetSizePapi.INSTANCE);
        LightPapi.register();
    }

    private GunsmithLibPapas() {
    }
}
