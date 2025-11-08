package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * @see Gunsmith#getGunInfo(ItemStack)
 */
public record GunInfo(
        ItemStack gunStack,
        IGun gunItem,
        ResourceLocation gunId,
        CommonGunIndex index
) {
    /**
     * @since 4.12.0
     */
    public static Optional<GunInfo> of(ItemStack gun) {
        return Gunsmith.getGunInfo(gun);
    }

    public int getTotalAmmo() {
        int mag = gunItem().getCurrentAmmoCount(gunStack());
        int barrel = index().getGunData().getBolt() == Bolt.OPEN_BOLT
                ? 0
                : gunItem().hasBulletInBarrel(gunStack()) ? 1 : 0;
        return mag + barrel;
    }

    public int getTotalMagazineSize() {
        int mag = AttachmentDataUtils.getAmmoCountWithAttachment(gunStack(), index().getGunData());
        int barrel = index().getGunData().getBolt() == Bolt.OPEN_BOLT
                ? 0
                : 1;
        return mag + barrel;
    }

    /**
     * @since 3.2.0
     */
    public void setTotalAmmo(int value) {
        if (index().getGunData().getBolt() == Bolt.OPEN_BOLT) {
            gunItem.setCurrentAmmoCount(gunStack, value);
        } else {
            if (value > 0) {
                gunItem.setBulletInBarrel(gunStack, true);
                gunItem.setCurrentAmmoCount(gunStack, value - 1);
            } else {
                gunItem.setBulletInBarrel(gunStack, false);
                gunItem.setCurrentAmmoCount(gunStack, 0);
            }
        }
    }

    /**
     * 卸载所有子弹，包括膛内的 +1 发
     */
    public void dropAllAmmoIncludingBarrel(Player user) {
        if (gunItem().hasBulletInBarrel(gunStack())) {
            gunItem().setBulletInBarrel(gunStack(), false);
            gunItem().setCurrentAmmoCount(gunStack(), gunItem().getCurrentAmmoCount(gunStack()) + 1);
        }
        gunItem().dropAllAmmo(user, gunStack());
        setTotalAmmo(0);
    }

    public int getDummyAmmoAmount() {
        return gunItem().getDummyAmmoAmount(gunStack());
    }

    public void setDummyAmmoAmount(int amount) {
        gunItem().setDummyAmmoAmount(gunStack(), amount);
    }

    public void addDummyAmmoAmount(int amount) {
        gunItem().addDummyAmmoAmount(gunStack(), amount);
    }

    public FireMode getFireMode() {
        return gunItem().getFireMode(gunStack());
    }

    public void setFireMode(@Nullable FireMode fireMode) {
        gunItem().setFireMode(gunStack(), fireMode);
    }
}
