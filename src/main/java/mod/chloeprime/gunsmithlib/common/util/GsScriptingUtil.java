package mod.chloeprime.gunsmithlib.common.util;

import com.tacz.guns.api.entity.IGunOperator;
import com.tacz.guns.entity.shooter.ShooterDataHolder;
import com.tacz.guns.item.ModernKineticGunScriptAPI;
import net.minecraft.Util;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.luaj.vm2.LuaValue;
import org.luaj.vm2.lib.jse.CoerceJavaToLua;

public final class GsScriptingUtil {
    public static ModernKineticGunScriptAPI api(LivingEntity shooter, ItemStack gunItem) {
        return api(shooter, IGunOperator.fromLivingEntity(shooter).getDataHolder(), gunItem);
    }

    public static ModernKineticGunScriptAPI api(LivingEntity shooter, ShooterDataHolder dataHolder, ItemStack gunItem) {
        return Util.make(new ModernKineticGunScriptAPI(), api -> {
            api.setShooter(shooter);
            api.setDataHolder(dataHolder);
            api.setItemStack(gunItem);
        });
    }

    public static LuaValue[] varargWithApi(Object api, Object[] args) {
        var merged = new LuaValue[args.length + 1];
        merged[0] = CoerceJavaToLua.coerce(api);
        for (int i = 0; i < args.length; i++) {
            merged[i + 1] = CoerceJavaToLua.coerce(args[i]);
        }
        return merged;
    }

    private GsScriptingUtil() {
    }
}
