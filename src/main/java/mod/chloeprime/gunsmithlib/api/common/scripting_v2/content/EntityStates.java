package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import mod.chloeprime.gunsmithlib.api.util.AABB;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.EntityStatesImpl;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
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
    static EntityStates of(Entity entity) {
        if (entity instanceof LivingEntity shooter) {
            return ShooterStates.of(shooter);
        } else {
            return new EntityStatesImpl(entity);
        }
    }

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
     * 获取实体的碰撞盒大小。
     *
     * @return 实体的碰撞盒大小。
     * @since 6.3
     */
    default Vector3d size() {
        return bounding_box().size();
    }

    /**
     * 获取实体的参考大小。
     *
     * @return 实体的参考大小（三个轴上大小的平均值）
     * @since 6.3
     */
    default double referential_size() {
        return bounding_box().referential_size();
    }

    /**
     * 获取实体的碰撞盒。
     * 返回的对象并不是原版的 AABB，所以可以用可读的名称访问其方法哦~
     *
     * @return 实体的碰撞盒。
     * @since 6.3
     */
    AABB bounding_box();

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

    /**
     * 获取实体脚底位置的实时光照。
     * 注：实体站在非完整方块上时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体脚底的实时光照
     * @since 6.3
     */
    int light_level_at_body();

    /**
     * 获取实体头部位置的实时光照。
     * 注：实体头部位于非完整方块中时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体眼部的实时光照
     * @since 6.3
     */
    int light_level_at_head();

    /**
     * 获取实体头部和脚底位置的实时光照的最大值。
     * 相比前面几个方法来说，能更好地避免玩家头部 / 脚底位于非完整方块中时获取到的光照为 0 的问题。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体受到的实时光照
     * @since 6.3
     */
    default int light_level() {
        return Math.max(light_level_at_body(), light_level_at_head());
    }

    /**
     * 获取实体脚底位置的方块光照。
     * 注：实体站在非完整方块上时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体脚底的方块光照
     * @since 6.3
     */
    int block_light_level_at_body();

    /**
     * 获取实体头部位置的方块光照。
     * 注：实体头部位于非完整方块中时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体眼部的方块光照
     * @since 6.3
     */
    int block_light_level_at_head();

    /**
     * 获取实体头部和脚底位置的方块光照的最大值。
     * 相比前面几个方法来说，能更好地避免玩家头部 / 脚底位于非完整方块中时获取到的光照为 0 的问题。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体受到的方块光照
     * @since 6.3
     */
    default int block_light_level() {
        return Math.max(block_light_level_at_body(), block_light_level_at_head());
    }

    /**
     * 获取实体脚底位置的天空光照。
     * 注：实体站在非完整天空上时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体脚底的天空光照
     * @since 6.3
     */
    int sky_light_level_at_body();

    /**
     * 获取实体头部位置的天空光照。
     * 注：实体头部位于非完整天空中时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体眼部的天空光照
     * @since 6.3
     */
    int sky_light_level_at_head();

    /**
     * 获取实体头部和脚底位置的天空光照的最大值。
     * 相比前面几个方法来说，能更好地避免玩家头部 / 脚底位于非完整方块中时获取到的光照为 0 的问题。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体受到的天空光照
     * @since 6.3
     */
    default int sky_light_level() {
        return Math.max(sky_light_level_at_body(), sky_light_level_at_head());
    }

    /**
     * 获取实体脚底位置的实时天空光照。
     * 注：实体站在非完整天空上时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体脚底的实时天空光照
     * @since 6.3
     */
    int realtime_sky_light_level_at_body();

    /**
     * 获取实体头部位置的实时天空光照。
     * 注：实体头部位于非完整天空中时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体眼部的实时天空光照
     * @since 6.3
     */
    int realtime_sky_light_level_at_head();

    /**
     * 获取实体头部和脚底位置的实时天空光照的最大值。
     * 相比前面几个方法来说，能更好地避免玩家头部 / 脚底位于非完整方块中时获取到的光照为 0 的问题。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体受到的实时天空光照
     * @since 6.3
     */
    default int realtime_sky_light_level() {
        return Math.max(realtime_sky_light_level_at_body(), realtime_sky_light_level_at_head());
    }

    /**
     * 获取实体脚底位置的理论光照。
     * 注：实体站在非完整天空上时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体脚底的理论光照
     * @since 6.3
     */
    int theoretical_light_level_at_body();

    /**
     * 获取实体头部位置的理论光照。
     * 注：实体头部位于非完整天空中时，获取的光照可能为 0。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体眼部的理论光照
     * @since 6.3
     */
    int theoretical_light_level_at_head();

    /**
     * 获取实体头部和脚底位置的理论光照的最大值。
     * 相比前面几个方法来说，能更好地避免玩家头部 / 脚底位于非完整方块中时获取到的光照为 0 的问题。
     * <p>
     * 和 ModernKineticScriptAPI 扩展的方法不同，本方法在遇到错误时返回 0 而不是 -1。
     *
     * @return 实体受到的理论光照
     * @since 6.3
     */
    default int theoretical_light_level() {
        return Math.max(theoretical_light_level_at_body(), theoretical_light_level_at_head());
    }
}
