package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import com.tacz.guns.client.animation.statemachine.GunAnimationStateContext;
import mod.chloeprime.gunsmithlib.api.client.GunsmithLibAnimationConstant;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibCommonScriptExtension;
import mod.chloeprime.gunsmithlib.client.AbstractGunAnimationStateContextExtension;
import mod.chloeprime.gunsmithlib.client.GunsmithLibClient;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import org.jetbrains.annotations.ApiStatus;

/**
 * 示例用法：{@code api:gunsmithlib_extension():play_overheat_sound()}
 *
 * @see GunScriptAPIExtension#gunsmithlib_extension()
 * @since 5.6.0
 */
@SuppressWarnings({"unused", "FieldCanBeLocal"})
public class GunsmithLibAnimationStateMachineScriptExtension extends GunsmithLibCommonScriptExtension {
    /**
     * 检测是否能安全调用 {@link #get_previous_gun_id()}。
     *
     * @return 如果为 true，则调用 {@link #get_previous_gun_id()} 不会抛出异常。
     */
    public boolean has_previous_gun_id() {
        return GunsmithLibClient.getPreviousGunId().isPresent();
    }

    /**
     * 获取弹种切换前的枪械 id。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会抛出异常。
     *
     * @return 弹种切换时旧枪械 index 的 id
     */
    public String get_previous_gun_id() {
        var ret = (String) GunsmithLibClient.getPreviousGunId().orElse(null);
        if (ret == null) {
            throw new IllegalStateException("get_previous_gun_id can only be called during gun variant switching.");
        }
        return ret;
    }

    /**
     * 获取弹种切换前枪械弹匣内的子弹数量。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code -1}。
     *
     * @return 弹种切换前枪械弹匣内的子弹数量
     */
    public int get_previous_ammo_amount() {
        return GunsmithLibClient.getPreviousAmmoAmount();
    }

    /**
     * 获取弹种切换前枪膛内是否有子弹。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code false}。
     *
     * @return 弹种切换前枪膛内是否有子弹
     */
    public boolean get_previous_has_bullet_in_barrel() {
        return GunsmithLibClient.prevHasAmmoInBarrel();
    }

    /**
     * 获取弹种切换前枪械内累计子弹数量（弹匣 + 枪膛）。
     * <p>
     * 警告：只能在
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_VARIANT_SWITCHED} 和
     * {@link GunsmithLibAnimationConstant#GUNSMITHLIB_INPUT_CURRENT_PART_SWITCHED}
     * 时调用，
     * 其他时候调用会返回 {@code -1}。
     *
     * @return 弹种切换前枪械内累计子弹数量（弹匣 + 枪膛）
     */
    public int get_previous_total_ammo_amount() {
        return get_previous_ammo_amount() + (get_previous_has_bullet_in_barrel() ? 1 : 0);
    }


    // 下面是内部 API

    private final GunAnimationStateContext api;
    private final AbstractGunAnimationStateContextExtension v1;

    @ApiStatus.Internal
    public GunsmithLibAnimationStateMachineScriptExtension(GunAnimationStateContext ctx) {
        super((AbstractCommonScriptingExtension) ctx);
        this.api = ctx;
        this.v1 = (AbstractGunAnimationStateContextExtension) ctx;
    }
}
