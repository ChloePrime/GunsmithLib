package mod.chloeprime.gunsmithlib.api.client.scripting_v2;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 有这个注解的 property 在 modify display property 时，
 * 传入的 context 可能会无法执行动画相关的方法。
 *
 * @since 6.2
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.CLASS)
public @interface MaybeInanimatable {
}
