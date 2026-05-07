package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import net.minecraft.util.Mth;
import net.minecraft.world.entity.Pose;
import org.joml.Vector2f;
import org.joml.Vector3d;

import java.util.random.RandomGenerator;

/**
 * 实体的状态
 *
 * @since 6.0.0
 */
@SuppressWarnings("unused")
public interface EntityStates {
    /**
     * 获取实体脚底的坐标。
     *
     * @return 实体脚底的坐标
     */
    Vector3d position();

    /**
     * 获取实体眼部的坐标。
     *
     * @return 实体眼部的坐标
     */
    Vector3d eye_position();

    /**
     * 获取实体碰撞箱中心的坐标。
     *
     * @return 实体碰撞箱中心的坐标
     */
    Vector3d center_position();

    /**
     * 获取实体的旋转。单位为弧度。
     *
     * @return 实体的旋转。x() 为绕 x 轴的旋转（pitch），y() 为绕 y 轴的旋转（yaw）。
     */
    default Vector2f rotation() {
        return rotation_degrees().mul(Mth.PI / 180, new Vector2f());
    }

    /**
     * 获取实体的旋转。单位为角度。
     *
     * @return 实体的旋转。x() 为绕 x 轴的旋转（pitch），y() 为绕 y 轴的旋转（yaw）。
     */
    Vector2f rotation_degrees();

    /**
     * 获取实体的速度，单位为格每刻。
     *
     * @return 实体的速度。
     */
    Vector3d velocity_per_tick();

    /**
     * 获取实体的速度，单位为格每秒。
     *
     * @return 实体的速度。
     */
    default Vector3d velocity_per_second() {
        return velocity_per_tick().mul(20, new Vector3d());
    }

    /**
     * 获取实体视线前方在世界坐标系下的值。
     *
     * @return 实体视线前方在世界坐标系下的值
     */
    Vector3d get_look_direction();

    /**
     * 获取射手的实体姿势。
     *
     * @return 射手的实体姿势
     * @see net.minecraft.world.entity.Pose
     */
    default String get_entity_pose() {
        return get_entity_pose_object().name();
    }

    /**
     * 获取射手的实体姿势对象。主要面向高级脚本用户和 Java 用户。
     *
     * @return 射手的实体姿势对象
     * @see #get_entity_pose() 简单的枪械脚本中可以用这个。
     */
    Pose get_entity_pose_object();

    /**
     * 获取实体自身的随机数生成器。
     *
     * @return 实体自身的随机数生成器
     */
    RandomGenerator get_random_generator();

    /**
     * 获取实体是否活着。
     *
     * @return 实体是否活着
     */
    boolean is_alive();

    /**
     * 获取实体是否被移除。
     *
     * @return 实体是否被移除
     */
    boolean is_removed();

    /**
     * 获取实体是否存在。
     * 死亡动画播放的过程中也属于存在。
     *
     * @return 实体是否存在
     */
    default boolean exists() {
        return !is_removed();
    }

    /**
     * 获取射手是否正在地面上移动。
     *
     * @return 射手是否正在地面上移动
     */
    boolean is_moving();

    /**
     * 获取射手是否正在疾跑。
     *
     * @return 射手是否正在疾跑
     */
    boolean is_sprinting();

    /**
     * 获取射手是否正在蹲下。
     *
     * @return 射手是否正在蹲下
     */
    boolean is_crouching();

    /**
     * 获取射手是否正在趴下。
     *
     * @return 射手是否正在趴下
     */
    boolean is_crawling();

    /**
     * 获取射手是否正在游泳。
     *
     * @return 射手是否正在游泳
     */
    boolean is_swimming();

    /**
     * 获取实体是否站在地上。
     *
     * @return 实体是否站在地上
     */
    boolean is_on_ground();

    /**
     * 获取实体是否开启了静音。
     *
     * @return 实体是否开启了静音
     */
    boolean is_silent();

    /**
     * 获取实体是否开启了静音。
     *
     * @return 实体是否开启了静音
     */
    boolean is_affected_by_gravity();

    /**
     * 获取实体是否免疫火焰。
     * 注：免疫火焰不能免疫 GunsmithLib 标准弹种库的燃烧效果。
     *
     * @return 实体是否免疫火焰
     */
    boolean is_fire_immune();

    /**
     * 获取实体是否在水方块中。
     *
     * @return 实体是否在水方块中
     */
    boolean is_in_water();

    /**
     * 获取实体是否淋雨。
     *
     * @return 实体是否淋雨
     */
    boolean is_in_rain();

    /**
     * 获取实体是否在气泡柱中。
     *
     * @return 实体是否在气泡柱中
     */
    boolean is_in_bubble();

    /**
     * 获取实体是否在水中或淋雨。
     *
     * @return 实体是否在水中或淋雨
     */
    boolean is_in_water_or_rain();

    /**
     * 获取实体是否在水中或在气泡柱中。
     *
     * @return 实体是否在水中或在气泡柱中
     */
    boolean is_in_water_or_bubble();

    /**
     * 获取实体是否在水中，淋雨或在气泡柱中。
     *
     * @return 实体是否在水中，淋雨或在气泡柱中
     */
    boolean is_in_water_or_rain_or_bubble();

    /**
     * 获取实体是否完全被水淹没。
     *
     * @return 实体是否完全被水淹没，不是所措
     */
    boolean is_under_water();

    /**
     * 获取实体是否接触熔岩。
     *
     * @return 实体是否接触熔岩
     */
    boolean is_in_lava();

    /**
     * 获取射手是否着火。
     *
     * @return 射手是否着火
     */
    boolean is_on_fire();

    /**
     * 获取射手着火的剩余时间。
     *
     * @return 射手着火的剩余时间，单位为刻
     */
    long remaining_fire_ticks();

    /**
     * 获取射手着火的剩余时间。单位为秒。
     *
     * @return 射手着火的剩余时间，单位为秒
     */
    default double remaining_fire_time_seconds() {
        return remaining_fire_ticks() / 20.0;
    }
}
