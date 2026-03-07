package mod.chloeprime.gunsmithlib.common.gunpack_extension.shared.hit_particle;

import it.unimi.dsi.fastutil.floats.FloatList;
import it.unimi.dsi.fastutil.ints.IntList;
import mod.chloeprime.gunsmithlib.common.util.GunpackProperty;

public class AAAParticleData {
    /**
     * 粒子的全局缩放大小。
     */
    @GunpackProperty
    private float scale = 1;

    /**
     * 传给对应 Effekseer 粒子的动态输入。
     * [0] = @In0,
     * [1] = @In1,
     * [2] = @In2,
     * [3] = @In3,
     * <a href=https://effekseer.github.io/Help_Tool/en/ToolReference/dynamicParameter.html>Effekseer 官方文档中的动态输入章节</a>
     */
    @GunpackProperty
    private float[] parameters = EMPTY_FLOAT_ARRAY;

    /**
     * 粒子生成时触发的 Effekseer 粒子触发器。
     * <a href=https://effekseer.github.io/Help_Tool/en/ToolTutorial/15.html>Effekseer 官方文档中的触发器章节</a>
     */
    @GunpackProperty
    private int[] triggers = EMPTY_INT_ARRAY;

    public float getScale() {
        return scale;
    }

    public FloatList getParameters() {
        return FloatList.of(parameters);
    }

    public IntList getTriggers() {
        return IntList.of(triggers);
    }

    private static final float[] EMPTY_FLOAT_ARRAY = {};
    private static final int[] EMPTY_INT_ARRAY = {};
}
