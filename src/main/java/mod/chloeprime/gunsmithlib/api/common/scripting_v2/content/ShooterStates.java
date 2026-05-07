package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import net.minecraft.world.entity.ai.attributes.Attribute;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Vector2f;
import org.joml.Vector3d;

import javax.annotation.Nullable;

/**
 * 射手状态
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface ShooterStates extends EntityStates {

    // Vanilla

    /**
     * 获取射手的头部旋转。
     * 脖子右拧
     *
     * @return 射手的头部旋转
     */
    Vector2f get_head_rotation();

    /**
     * 获取射手的身体旋转。
     *
     * @return 射手的身体旋转
     */
    Vector2f get_body_rotation();

    /**
     * 获取一个实体的某个 {@link Attribute} 的值。
     *
     * @param attribute_id attribute 的注册名
     * @return 射手对应 attribute 的值
     */
    double get_attribute_value(String attribute_id);

    /**
     * 获取一个实体的某个 {@link Attribute} 的基础值。
     *
     * @param attribute_id attribute 的注册名
     * @return 射手对应 attribute 的基础值
     */
    double get_attribute_base_value(String attribute_id);

    /**
     * 获取一个实体的药水效果实例。
     *
     * @return 射手对应药水效果的实例，如果实体没有该药水效果则返回 {@code nil}
     */
    @Nullable PotionEffectInstanceView get_potion_effect(String effect_id);

    /**
     * 获取射手当前的生命值
     *
     * @return 当前生命值
     */
    float get_health();

    /**
     * 获取射手的最大生命值
     *
     * @return 射手的最大生命值
     */
    float get_max_health();

    /**
     * 获取射手的护甲值
     *
     * @return 射手的护甲值
     */
    double get_armor();

    /**
     * 获取射手的移速属性。
     *
     * @return 射手的移速属性
     */
    double get_movement_speed();

    /**
     * 获取射手的盔甲韧性
     *
     * @return 射手的盔甲韧性
     */
    double get_armor_toughness();

    /**
     * 判断射手是否是幼年状态。
     * 拿起你的 JR-15.
     *
     * @return 射手是否是幼年状态
     */
    boolean is_baby();

    /**
     * 获取射手当前是否正爬在可攀爬物上（主要是梯子）。
     *
     * @return 射手当前是否正爬在可攀爬物上
     */
    boolean is_on_climbable();

    /**
     * 获取射手的缩放比例。
     * 这个值在原版只用于缩放幼年形态的实体，
     * 但是可能被各种 mod 修改。
     *
     * @return 射手的缩放比例
     */
    float get_scale();

    /**
     * 获取射手当前是否正在鞘翅飞行。
     *
     * @return 射手当前是否正在鞘翅飞行
     */
    boolean is_elytra_flying();

    /**
     * 获取射手当次鞘翅飞行的时间，单位为刻。
     *
     * @return 射手当次鞘翅飞行的时间，单位为刻
     */
    int get_elytra_flying_ticks();

    /**
     * 获取射手当次鞘翅飞行的时间，单位为秒。
     *
     * @return 射手当次鞘翅飞行的时间，单位为秒
     */
    default double get_elytra_flying_time_seconds() {
        return get_elytra_flying_ticks() / 20.0;
    }

    /**
     * 获取实体的移动输入。
     * 警告：这个方法对服务端玩家无效。
     *
     * @return 实体本地坐标系下的移动输入向量
     */
    @ApiStatus.Experimental
    Vector3d get_movement_input();

    // TaCZ

    /**
     * 获取射手是否在拉大栓。
     *
     * @return 射手是否在拉大栓
     */
    boolean is_bolting();

    /**
     * 获取射手的换弹状态。
     *
     * @return 换弹状态
     * @see com.tacz.guns.api.entity.ReloadState.StateType
     */
    String reload_state();

    /**
     * 获取射手的换弹冷却，单位为毫秒。
     */
    long reload_countdown_millis();

    /**
     * 获取用户是否有效地按下了瞄准键。
     *
     * @return 用户是否有效地按下了瞄准键
     */
    boolean is_aiming();

    /**
     * 获取瞄准进度。范围 0-1。
     *
     * @return 瞄准进度，范围 0-1
     */
    float aiming_progress();

    /**
     * 获取玩家持枪奔跑的时长。
     * 最大不会大于枪械数据中设置的 sprintTime，最小不会小于 0。
     *
     * @return 玩家持枪奔跑的时长
     */
    float sprint_time();

    /**
     * 获取射手的射击冷却，单位为毫秒。
     *
     * @return 射手的射击冷却，单位为毫秒
     */
    long shoot_cooldown_millis();

    /**
     * 获取射手的近战冷却，单位为毫秒。
     *
     * @return 射手的近战冷却，单位为毫秒
     */
    long melee_cooldown_millis();

    /**
     * 获取射手的拔枪冷却，单位为毫秒。
     *
     * @return 射手的拔枪冷却，单位为毫秒
     */
    long draw_cooldown_millis();
}
