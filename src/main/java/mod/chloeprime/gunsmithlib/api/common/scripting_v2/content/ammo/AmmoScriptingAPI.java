package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ammo;

import com.tacz.guns.item.ModernKineticGunScriptAPI;
import com.tacz.guns.resource.index.CommonAmmoIndex;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.content.ShooterStates;
import mod.chloeprime.gunsmithlib.api.util.AmmoInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.luaj.vm2.LuaTable;

/**
 * 弹药脚本的 API。
 *
 * @param gun           弹药所安装的枪械的 API
 * @param script_params 弹药 index 中指定的脚本参数
 * @param privateData   弹药的不面向脚本公开的数据
 * @since 6.2.0
 */
@SuppressWarnings("unused")
public record AmmoScriptingAPI(
        ModernKineticGunScriptAPI gun,
        LuaTable script_params,
        PrivateData privateData
) {
    /**
     * 获取弹药 ID。
     *
     * @return 弹药 ID
     */
    public ResourceLocation ammo_id() {
        return privateData().ammoInfo().ammoId();
    }

    /**
     * 获取弹药的 index。
     *
     * @return 弹药的 index
     */
    public CommonAmmoIndex ammo_index() {
        return privateData().ammoInfo().index();
    }

    /**
     * 获取射手的各种状态。
     * 这个方法在逻辑脚本中永远不会返回 {@code nil}。
     *
     * @return 获取射手的各种状态的接口
     * @see GunsmithLibCommonScriptExtension#shooter_states()
     */
    public ShooterStates shooter_states() {
        return ((GunScriptAPIExtension) (Object) gun()).gunsmithlib_extension().shooter_states();
    }

    // 小写驼峰风格的 API

    /**
     * 获取枪械的脚本 API。
     * {@link #gun()} 的别名。
     *
     * @return 枪械的脚本 API
     */
    public ModernKineticGunScriptAPI getGunApi() {
        return gun();
    }

    /**
     * 获取弹药 data 中指定的脚本参数。
     * {@link #script_params()} 的别名。
     *
     * @return data 文件中指定的脚本参数。
     */
    public LuaTable getScriptParams() {
        return script_params();
    }

    /**
     * 获取弹药 ID。
     * {@link #ammo_id()} 的别名。
     *
     * @return 弹药 ID
     */
    public ResourceLocation getAmmoId() {
        return ammo_id();
    }

    /**
     * 获取弹药的 index。
     * {@link #ammo_index()} 的别名。
     *
     * @return 弹药的 index
     */
    public CommonAmmoIndex getAmmoIndex() {
        return ammo_index();
    }
    
    /**
     * 获取射手的各种状态。
     * 这个方法在逻辑脚本中永远不会返回 {@code nil}。
     * {@link #shooter_states()} 的别名。
     *
     * @return 获取射手的各种状态的接口
     * @see GunsmithLibCommonScriptExtension#shooter_states()
     */
    public ShooterStates getShooterStates() {
        return shooter_states();
    }

    @ApiStatus.Internal
    public AmmoScriptingAPI(
            ModernKineticGunScriptAPI gun,
            AmmoInfo ammoInfo,
            LuaTable scriptParams
    ) {
        this(gun, scriptParams, new PrivateData(gun.getShooter(), ammoInfo));
    }

    private record PrivateData(
            LivingEntity shooter,
            AmmoInfo ammoInfo
    ) {
    }
}
