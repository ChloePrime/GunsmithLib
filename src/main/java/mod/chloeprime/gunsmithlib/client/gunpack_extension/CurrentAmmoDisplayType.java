package mod.chloeprime.gunsmithlib.client.gunpack_extension;

import com.google.gson.annotations.SerializedName;

public enum CurrentAmmoDisplayType {
    @SerializedName("default") DEFAULT,
    @SerializedName("battery") BATTERY,
    @SerializedName("counter") COUNTER,
}
