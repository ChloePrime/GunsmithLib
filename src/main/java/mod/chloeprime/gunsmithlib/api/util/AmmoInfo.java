package mod.chloeprime.gunsmithlib.api.util;

import com.tacz.guns.api.item.IAmmo;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ammo.AmmoScriptingAPI;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.ammo.GunsmithLibAmmoDataExtension;
import mod.chloeprime.gunsmithlib.common.util.GsScriptingUtil;
import mod.chloeprime.gunsmithlib.common.util.GsHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.lib.jse.CoerceLuaToJava;

import java.util.Optional;

public record AmmoInfo(
        ItemStack ammoStack,
        IAmmo ammoItem,
        ResourceLocation ammoId,
        CommonAmmoIndex index
) {
    /**
     * @since 4.12.0
     */
    public static Optional<AmmoInfo> of(ItemStack gun) {
        return Gunsmith.getAmmoInfo(gun);
    }

    /**
     * @since 6.5
     */
    @SuppressWarnings("unused")
    public <T> Optional<T> runScript(LivingEntity shooter, GunInfo gun, String method, Class<T> retType, Object... args) {
        return runScript(GsScriptingUtil.api(shooter, gun.gunStack()), method, retType, args);
    }

    /**
     * @since 6.5
     */
    public <T> Optional<T> runScript(ModernKineticGunScriptAPI gunApi, String method, Class<T> retType, Object... args) {
        var ext = GunsmithLibAmmoDataExtension.of(this).orElse(null);
        if (ext == null) {
            return Optional.empty();
        }

        return ext.getScript()
                .map(script -> GsHelper.checkFunction(script.get(method)))
                .map(func -> {
                    var api = new AmmoScriptingAPI(gunApi, this, ext.getScriptParams());
                    return func.invoke(GsScriptingUtil.varargWithApi(api, args)).arg1();
                })
                .filter(result -> !result.isnil())
                .map(result -> CoerceLuaToJava.coerce(result, retType))
                .map(retType::cast);
    }
}
