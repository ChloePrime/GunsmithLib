package mod.chloeprime.gunsmithlib.api.util;

import com.google.common.base.Suppliers;
import com.tacz.guns.api.item.IGun;
import com.tacz.guns.api.item.gun.FireMode;
import com.tacz.guns.resource.index.CommonGunIndex;
import com.tacz.guns.resource.pojo.data.gun.Bolt;
import com.tacz.guns.util.AttachmentDataUtils;
import mod.chloeprime.gunsmithlib.common.util.GsScriptingUtil;
import mod.chloeprime.gunsmithlib.common.util.GunPartIterator;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaError;
import org.luaj.vm2.LuaFunction;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import javax.annotation.Nullable;
import java.util.*;
import java.util.stream.Stream;

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

    public <T> Optional<T> runScript(LivingEntity shooter, String method, Class<T> retType, Object... args) {
        return Optional.ofNullable(this.index.getScript())
                .map(script -> checkFunction(script.get(method)))
                .map(func -> func.invoke(GsScriptingUtil.varargWithApi(GsScriptingUtil.api(shooter, gunStack), args)).arg1())
                .filter(result -> !result.isnil())
                .map(result -> CoerceLuaToJava.coerce(result, retType))
                .map(retType::cast);
    }

    /**
     * 按枪械，弹药，配件的顺序运行脚本。
     * 调用此方法时不会立即执行脚本，在返回的 Stream 背后的 iterator 迭代时才会运行对应的脚本。
     *
     * @since 6.5
     */
    public <T> Stream<T> runAllScript(LivingEntity shooter, String method, Class<T> retType, Object... args) {
        var api = Suppliers.memoize(() -> GsScriptingUtil.api(shooter, this.gunStack));
        return GunPartIterator.iterate(this, gi -> Optional.ofNullable(gi.index().getScript())
                        .map(script -> checkFunction(script.get(method)))
                        .map(func -> func.invoke(GsScriptingUtil.varargWithApi(api.get(), args)).arg1())
                        .filter(result -> !result.isnil())
                        .map(result -> CoerceLuaToJava.coerce(result, retType))
                        .map(retType::cast),
                ami -> ami.runScript(api.get(), method, retType, args),
                ati -> ati.runScript(api.get(), method, retType, args));
    }

    private static LuaFunction checkFunction(LuaValue luaValue) {
        if (luaValue.isfunction()) {
            return (LuaFunction) luaValue;
        } else if (luaValue.isnil()) {
            return null;
        } else {
            throw new LuaError("bad argument: function or nil expected, got " + luaValue.typename());
        }
    }
}
