package mod.chloeprime.gunsmithlib.common.util;

import java.lang.annotation.*;

/**
 * 有这个注解的字段是可以由枪包数据文件控制的哦
 *
 * @since 4.9.0
 */
@Documented
@Retention(RetentionPolicy.CLASS)
@Target(ElementType.FIELD)
public @interface GunpackProperty {
}
