package mod.chloeprime.gunsmithlib.common.util;

import java.util.Objects;
import java.util.function.Function;

/**
 * @since 6.0.0
 */
public record EqualityComparator<T, C>(T content, C criterion) {
    @SuppressWarnings("unused")
    public EqualityComparator(T content, Function<T, C> criterion) {
        this(content, criterion.apply(content));
    }

    public T unwrap() {
        return content();
    }

    public static <T, C> Function<T, EqualityComparator<T, C>> by(Function<T, C> criterion) {
        return obj -> new EqualityComparator<>(obj, criterion.apply(obj));
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        var that = (EqualityComparator<?, ?>) o;
        return Objects.equals(criterion, that.criterion);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(criterion);
    }
}
