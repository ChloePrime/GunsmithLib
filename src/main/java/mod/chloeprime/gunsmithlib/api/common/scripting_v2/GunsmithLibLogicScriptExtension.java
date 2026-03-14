package mod.chloeprime.gunsmithlib.api.common.scripting_v2;

import com.tacz.guns.item.ModernKineticGunScriptAPI;
import mod.chloeprime.gunsmithlib.api.common.GunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.api.util.Gunsmith;
import mod.chloeprime.gunsmithlib.common.AbstractCommonScriptingExtension;
import mod.chloeprime.gunsmithlib.common.AbstractGunScriptAPIExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.util.LinearAlgebraTypes;
import mod.chloeprime.gunsmithlib.common.util.TableSchema;
import net.minecraft.world.entity.LivingEntity;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector3d;
import org.luaj.vm2.LuaValue;

/**
 * 示例用法：{@code api:gunsmithlib_extension():play_overheat_sound()}
 *
 * @see GunScriptAPIExtension#gunsmithlib_extension()
 * @since 5.6.0
 */
@SuppressWarnings("unused")
public class GunsmithLibLogicScriptExtension extends GunsmithLibCommonScriptExtension {
    public final Vector3d get_shooter_position() {
        var shooter = api.getShooter();
        if (shooter == null) {
            return ZERO;
        }
        return LinearAlgebraTypes.moj2joml(shooter.position());
    }

    public final Vector3d get_muzzle_position() {
        var shooter = api.getShooter();
        if (shooter == null) {
            return ZERO;
        }
        return LinearAlgebraTypes.moj2joml(Gunsmith.getProximityMuzzlePos(shooter));
    }

    public final Vector3d get_front_vector() {
        var shooter = api.getShooter();
        if (shooter == null) {
            return ZERO;
        }
        return LinearAlgebraTypes.moj2joml(shooter.getLookAngle());
    }

    // 旧 API 转发

    /**
     * 播放 GunsmithLib 内置的过热音效
     */
    public final void play_overheat_sound() {
        v1.gunsmith_playOverheatSound();
    }

    /**
     * 通知客户端触发状态机脚本状态转移（transition）
     *
     * @param input 状态机 transition 函数中的 input 参数
     * @since 5.4.0
     */
    public final void trigger_animation_state_transition(String input) {
        v1.gunsmith_triggerAnimationStateTransition(input);
    }

    /**
     * 给发射者添加一个药水效果
     *
     * @param effect 药水效果，写法类似 data 文件中的 {@link PotionEffectData}
     */
    public final void add_potion_effect(@TableSchema(PotionEffectData.class) LuaValue effect) {
        v1.gunsmith_addEffect(effect);
    }

    /**
     * 给指定目标添加一个药水效果
     *
     * @param target 要添加药水效果哦的目标
     * @param effect 药水效果，写法类似 data 文件中的 {@link PotionEffectData}
     */
    public final void add_potion_effect_to(LivingEntity target, @TableSchema(PotionEffectData.class) LuaValue effect) {
        v1.gunsmith_addEffectTo(target, effect);
    }

    public final void spawn_particle(Vector3d position, @TableSchema(HitParticleData[].class) LuaValue[] effect) {
        v1.gunsmith_spawnParticle(position, effect);
    }

    // 下面是内部 API

    private static final Vector3d ZERO = new Vector3d(0, 0, 0);
    private final ModernKineticGunScriptAPI api;
    private final AbstractGunScriptAPIExtension v1;

    @ApiStatus.Internal
    public GunsmithLibLogicScriptExtension(ModernKineticGunScriptAPI api) {
        super((AbstractCommonScriptingExtension) api);
        this.api = api;
        this.v1 = (AbstractGunScriptAPIExtension) api;
    }
}
