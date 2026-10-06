package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import com.tacz.guns.entity.EntityKineticBullet;
import mod.chloeprime.gunsmithlib.common.impl.scripting_v2.content.BulletEntityImpl;
import org.joml.Vector3d;
import org.luaj.vm2.LuaTable;

import javax.annotation.Nonnull;

/**
 * @since 7.0
 */
@SuppressWarnings("unused")
public interface BulletEntityAPI extends EntityStates {
    static BulletEntityAPI of(EntityKineticBullet bullet) {
        return new BulletEntityImpl(bullet);
    }

    /**
     * 获取一个可以改变子弹实体状态的控制面板对象。
     *
     * @return 子弹的控制面板对象
     */
    ControlPanel control_panel();

    /**
     * 获得子弹实体在当前上下文中的位置，例如命中位置。
     * 由于子弹实体运动速度过快，上下文位置可能和子弹实体当前的位置相差甚远。
     *
     * @return 子弹实体的上下文位置
     */
    Vector3d get_context_position();

    interface ControlPanel {
        /**
         * 在子弹位置处生成破片。
         */
        void create_fragments();

        /**
         * 在指定位置处生成破片。
         *
         * @param pos 爆心坐标
         */
        void create_fragments(@Nonnull Vector3d pos);

        /**
         * 在指定位置处生成破片。
         *
         * @param pos 爆心坐标
         * @param config 破片设置
         * @param triggers_script_modification 如果为 {@code true}，那么 {@code config} 生效的值将被 modify property 修改。
         */
        void create_fragments(@Nonnull Vector3d pos, @Nonnull LuaTable config, boolean triggers_script_modification);

        /**
         * 让子弹爆炸，
         * 不会销毁子弹实体。
         */
        void explode();

        /**
         * 让子弹在指定的位置爆炸，
         * 不会销毁子弹实体。
         *
         * @param pos 爆心坐标
         */
        void explode(@Nonnull Vector3d pos);

        /**
         * 让子弹在指定的位置爆炸，
         * 不会销毁子弹实体。
         *
         * @param pos 爆心坐标
         * @param config 爆炸配置
         */
        void explode(@Nonnull Vector3d pos, @Nonnull LuaTable config, boolean triggers_script_modification);

        /**
         * 销毁子弹实体
         */
        void discard();
    }
}
