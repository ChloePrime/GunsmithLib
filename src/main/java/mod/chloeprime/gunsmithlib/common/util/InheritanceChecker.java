package mod.chloeprime.gunsmithlib.common.util;

import com.mojang.logging.LogUtils;
import org.slf4j.Logger;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class InheritanceChecker<T> {
    public InheritanceChecker(Class<T> baseType, String methodName, Class<?>... paramTypes) {
        this.givenBaseType = baseType;
        this.methodName = methodName;
        this.paramTypes = paramTypes;
        this.realBaseType = getDeclareClass(baseType, methodName, paramTypes);
    }

    public Class<T> getBaseType() {
        return givenBaseType;
    }

    public boolean isInherited(Class<? extends T> typeToCheck) {
        return CACHE.get(typeToCheck);
    }

    private static final Logger LOGGER = LogUtils.getLogger();
    private final Class<T> givenBaseType;
    private final String methodName;
    private final Class<?>[] paramTypes;
    private final Class<? super T> realBaseType;
    private final Set<Class<?>> badClasses = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ClassValue<Boolean> CACHE = new ClassValue<>() {
        @Override
        @SuppressWarnings("unchecked")
        protected Boolean computeValue(@Nonnull Class<?> type) {
            if (badClasses.contains(type)) {
                return false;
            }
            try {
                return !realBaseType.equals(getDeclareClass((Class<T>) type, methodName, paramTypes));
            } catch (OutOfMemoryError oom) {
                throw oom;
            } catch (Throwable ex) {
                LOGGER.warn("{} failed to inference method inheritance:", InheritanceChecker.this.getClass().getSimpleName(), ex);
                badClasses.add(type);
                return false;
            }
        }
    };

    @SuppressWarnings("unchecked")
    private Class<? super T> getDeclareClass(Class<T> type, String methodName, Class<?>[] paramTypes) {
        Method method;
        try {
            method = getDeclaredMethod(type, methodName, paramTypes);
        } catch (NoSuchMethodException ex) {
            // While MobEntity.mobInteract(...) is a protected method,
            // throwing NoSuchMethodException means the method has not
            // been overridden in given class
            method = getDeclaredMethodFromSuperClass(type, methodName, paramTypes);
        }
        return (Class<? super T>) method.getDeclaringClass();
    }

    // Walk over super classes to check override status
    private Method getDeclaredMethodFromSuperClass(Class<?> type, String methodName, Class<?>... paramTypes) {
        Class<?> temp = type.getSuperclass();

        while (temp != this.realBaseType && temp != Object.class && temp != null) {
            Method method;
            try {
                method = temp.getMethod(methodName, paramTypes);
                return method;
            }
            catch (NoSuchMethodException ignored) {
                try {
                    method = temp.getDeclaredMethod(methodName, paramTypes);
                    if (!method.trySetAccessible()) {
                        throw new IllegalArgumentException("Inaccessible Method");
                    }
                    return method;
                }
                catch (NoSuchMethodException pass) {
                    temp = temp.getSuperclass();
                }
            }
        }

        try {
            return Objects.requireNonNull(temp).getDeclaredMethod(methodName, paramTypes);
        }
        catch (NoSuchMethodException e) {
            // No way
            throw new RuntimeException(e);
        }
    }

    private static Method getDeclaredMethod(Class<?> type, String methodName, Class<?>... paramTypes) throws NoSuchMethodException {
        Method method;
        try {
            method = type.getMethod(methodName, paramTypes);
        } catch (NoSuchMethodException ignored) {
            method = type.getDeclaredMethod(methodName, paramTypes);
            if (!method.trySetAccessible()) {
                throw new IllegalArgumentException("Inaccessible Method");
            }
        }
        return method;
    }
}
