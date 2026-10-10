package mod.chloeprime.gunsmithlib.client.papi;

import com.tacz.guns.api.client.gameplay.IClientPlayerGunOperator;
import com.tacz.guns.resource.pojo.data.gun.ChargeData;
import mod.chloeprime.gunsmithlib.GunsmithLib;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.client.papi.framework.Papi;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

/**
 * 蓄力进度。Requested by 杂狼2403
 * @since 7.0
 */
public enum ChargeProgressPapi implements Papi {
    INSTANCE;

    public static final String NAME = GunsmithLib.loc("charge_progress").toString();

    @Override
    public String apply(ItemStack stack) {
        float maxProgress = Gunsmith.getGunInfo(stack)
                .map(gi -> gi.index().getGunData().getChargeData(gi.getFireMode()))
                .map(ChargeData::getMaxCharge)
                .orElse(Float.NaN);
        if (Float.isNaN(maxProgress) || maxProgress == 0) {
            return FALLBACK_TEXT;
        }

        return Optional.ofNullable(Minecraft.getInstance().player)
                .map(IClientPlayerGunOperator::fromLocalPlayer)
                .map(IClientPlayerGunOperator::getChargeProgress)
                .map(progress -> computeDisplayedProgress(progress, maxProgress))
                .orElse(FALLBACK_TEXT);
    }

    private static String computeDisplayedProgress(float progress, float maxProgress) {
        if (maxProgress == 0) {
            return FALLBACK_TEXT;
        }
        int percent = Mth.clamp(Math.abs(Math.round(progress / maxProgress)), 0, 100);
        return String.valueOf(percent);
    }
}
