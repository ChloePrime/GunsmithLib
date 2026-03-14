package mod.chloeprime.gunsmithlib.api.common;

import mod.chloeprime.gunsmithlib.api.common.scripting_v2.GunsmithLibLogicScriptExtension;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle.HitParticleData;
import mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.potion_effect.PotionEffectData;
import mod.chloeprime.gunsmithlib.common.util.TableSchema;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3d;
import org.luaj.vm2.LuaValue;

/**
 * @since 3.3.0
 */
@SuppressWarnings("unused")
public interface GunScriptAPIExtension extends CommonScriptingExtension {
    @Override
    GunsmithLibLogicScriptExtension gunsmithlib_extension();

    /**
     * 播放 GunsmithLib 内置的过热音效
     */
    void gunsmith_playOverheatSound();

    /**
     * 通知客户端触发状态机脚本状态转移（transition）
     *
     * @param input 状态机 transition 函数中的 input 参数
     * @since 5.4.0
     */
    void gunsmith_triggerAnimationStateTransition(String input);

    /**
     * 给发射者添加一个药水效果
     *
     * @param effect 药水效果，写法类似 data 文件中的 {@link PotionEffectData}
     */
    void gunsmith_addEffect(@TableSchema(PotionEffectData.class) LuaValue effect);

    /**
     * 给指定目标添加一个药水效果
     *
     * @param target 要添加药水效果哦的目标
     * @param effect 药水效果，写法类似 data 文件中的 {@link PotionEffectData}
     */
    void gunsmith_addEffectTo(LivingEntity target, @TableSchema(PotionEffectData.class) LuaValue effect);

    void gunsmith_spawnParticle(Vector3d position, @TableSchema(HitParticleData[].class) LuaValue[] effect);
}
