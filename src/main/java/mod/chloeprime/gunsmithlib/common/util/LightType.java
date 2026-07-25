package mod.chloeprime.gunsmithlib.common.util;

public enum LightType {
    /**
     * F3 界面中显示的光照
     */
    THEORETICAL,

    /**
     * 方块光照
     */
    BLOCK,

    /**
     * 天空光照，*不*受昼夜循环影响
     */
    SKY,

    /**
     * 实时光照，受昼夜循环影响
     */
    REALTIME,

    /**
     * 实时天空光照，受昼夜循环影响
     */
    REALTIME_SKY
}
