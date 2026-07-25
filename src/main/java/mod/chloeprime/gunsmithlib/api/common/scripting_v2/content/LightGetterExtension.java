package mod.chloeprime.gunsmithlib.api.common.scripting_v2.content;

import org.joml.Vector3d;
import org.joml.Vector3i;

/**
 * 获取光照的 API
 *
 * @since 6.3
 */
@SuppressWarnings("unused")
public interface LightGetterExtension {
    /**
     * 使用整数坐标获取实时光照。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_level_light_i(Vector3i pos);

    /**
     * 使用整数坐标获取实时光照。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_level_light_i(int x, int y, int z);

    /**
     * 使用浮点数坐标获取实时光照。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_level_light_d(Vector3d pos);

    /**
     * 使用浮点数坐标获取实时光照。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_level_light_d(double x, double y, double z);


    /**
     * 使用整数坐标获取方块光照。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的方块光照。如果所选区域未加载则返回 {@code -1}
     */
    int get_block_light_i(Vector3i pos);

    /**
     * 使用整数坐标获取方块光照。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的方块光照。如果所选区域未加载则返回 {@code -1}
     */
    int get_block_light_i(int x, int y, int z);

    /**
     * 使用浮点数坐标获取方块光照。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的方块光照。如果所选区域未加载则返回 {@code -1}
     */
    int get_block_light_d(Vector3d pos);

    /**
     * 使用浮点数坐标获取方块光照。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的方块光照。如果所选区域未加载则返回 {@code -1}
     */
    int get_block_light_d(double x, double y, double z);


    /**
     * 使用整数坐标获取天空光照。
     * <p>
     * *不*受昼夜循环影响。典型用例：使用星辰之力，能感应到天空即可充能的武器。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的天空光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_sky_light_i(Vector3i pos);

    /**
     * 使用整数坐标获取天空光照。
     * <p>
     * *不*受昼夜循环影响。典型用例：使用星辰之力，能感应到天空即可充能的武器。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的天空光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_sky_light_i(int x, int y, int z);

    /**
     * 使用浮点数坐标获取天空光照。
     * <p>
     * *不*受昼夜循环影响。典型用例：使用星辰之力，能感应到天空即可充能的武器。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的天空光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_sky_light_d(Vector3d pos);

    /**
     * 使用浮点数坐标获取天空光照。
     * <p>
     * *不*受昼夜循环影响。典型用例：使用星辰之力，能感应到天空即可充能的武器。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的天空光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_sky_light_d(double x, double y, double z);


    /**
     * 使用整数坐标获取实时天空光照。
     * <p>
     * 受昼夜循环影响。典型用例：太阳能供电或使用太阳神之力的武器。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的实时天空光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_realtime_sky_light_i(Vector3i pos);

    /**
     * 使用整数坐标获取实时天空光照。
     * <p>
     * 受昼夜循环影响。典型用例：太阳能供电或使用太阳神之力的武器。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的实时天空光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_realtime_sky_light_i(int x, int y, int z);

    /**
     * 使用浮点数坐标获取实时天空光照。
     * <p>
     * 受昼夜循环影响。典型用例：太阳能供电或使用太阳神之力的武器。
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的实时天空光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_realtime_sky_light_d(Vector3d pos);

    /**
     * 使用浮点数坐标获取实时天空光照。
     * <p>
     * 受昼夜循环影响。典型用例：太阳能供电或使用太阳神之力的武器。
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的实时天空光照，受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_realtime_sky_light_d(double x, double y, double z);


    /**
     * 使用整数坐标获取理论光照。
     * <p>
     * F3 界面中显示的光照，除了用于调试以外应该没有其他用处了。
     * 不过为了 API 完整性还是在此提供了 :P
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的理论光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_theoretical_light_i(Vector3i pos);

    /**
     * 使用整数坐标获取理论光照。
     * <p>
     * F3 界面中显示的光照，除了用于调试以外应该没有其他用处了。
     * 不过为了 API 完整性还是在此提供了 :P
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的理论光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_theoretical_light_i(int x, int y, int z);

    /**
     * 使用浮点数坐标获取理论光照。
     * <p>
     * F3 界面中显示的光照，除了用于调试以外应该没有其他用处了。
     * 不过为了 API 完整性还是在此提供了 :P
     *
     * @param pos 需要获取光照的位置
     * @return 该位置的理论光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_theoretical_light_d(Vector3d pos);

    /**
     * 使用浮点数坐标获取理论光照。
     * <p>
     * F3 界面中显示的光照，除了用于调试以外应该没有其他用处了。
     * 不过为了 API 完整性还是在此提供了 :P
     *
     * @param x 需要获取光照的 X 坐标
     * @param y 需要获取光照的 Y 坐标
     * @param z 需要获取光照的 Z 坐标
     * @return 该位置的理论光照，*不*受昼夜循环影响。如果所选区域未加载则返回 {@code -1}
     */
    int get_theoretical_light_d(double x, double y, double z);
}
