package net.glasslauncher.glassbrigadier.api.permission;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.util.Objects;
import java.util.function.Function;

public record PermissionNode<T>(
        String path,
        IsValuePositivePredicate<T> positivePredicate,
        Function<Object, T> valueLoadFunction, Function<T, Object> valueSaveFunction,
        Function<String, T> valueFromArgumentFunction,
        boolean arbitrary
) {
    private static final @NotNull Cache<@NotNull String, @NotNull PermissionNode<?>> CACHE = Caffeine.newBuilder().softValues().build();
    private static final @NotNull Cache<@NotNull String, @NotNull PermissionNode<Boolean>> ARBITRARY_CACHE = Caffeine.newBuilder().softValues().build();

    public static IsValuePositivePredicate<Boolean> BOOLEAN = notNullPositive();
    public static Function<Object, Boolean> BOOLEAN_LOAD = blindLoad();
    public static Function<Boolean, Object> BOOLEAN_SAVE = blindSave();
    public static Function<String, Boolean> BOOLEAN_PARSE = Boolean::valueOf;

    public static IsValuePositivePredicate<Integer> INTEGER = notNullPositive();
    public static Function<Object, Integer> INTEGER_LOAD = blindLoad();
    public static Function<Integer, Object> INTEGER_SAVE = blindSave();
    public static Function<String, Integer> INTEGER_PARSE = value -> {
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ignored){}
        return null;
    };

    public static <T> Function<Object, T> blindLoad() {
        //noinspection unchecked
        return val -> (T) val;
    }

    public static <T> Function<T, Object> blindSave() {
        return val -> val;
    }

    public static <T> IsValuePositivePredicate<T> notNullPositive() {
        return thisNode -> thisNode.getValue() != null;
    }

    /**
     * Get the permission node object relevant to the provided string.
     */
    public static <T> @Nullable PermissionNode<T> ofExisting(String path) {
        //noinspection unchecked Ahahaha fuck type erasure fuck type erasure fuck type erasure fucking why
        return (PermissionNode<T>) CACHE.getIfPresent(path);
    }

    /**
     * Get the permission node object relevant to the provided string.
     */
    public static <T> @NotNull PermissionNode<T> ofExistingOrArbitrary(String path) {
        // This is pretty fucking cursed. I should've just hardcoded int and boolean.
        PermissionNode<?> node = CACHE.getIfPresent(path);
        if (node == null) {
            //noinspection unchecked
            return (PermissionNode<T>) arbitraryBooleanNode(path);
        }
        //noinspection unchecked
        return (PermissionNode<T>) node;
    }

    /**
     * Get an arbitrary boolean node.
     */
    public static PermissionNode<Boolean> arbitraryBooleanNode(String path) {
        return ARBITRARY_CACHE.get(path, p -> new PermissionNode<>(p, BOOLEAN, BOOLEAN_LOAD, BOOLEAN_SAVE, BOOLEAN_PARSE, true));
    }

    /**
     * Register the permission node for use.
     */
    public static <T> PermissionNode<T> register(String path, IsValuePositivePredicate<T> positivePredicate, Function<Object, T> valueLoadFunction, Function<T, Object> valueSaveFunction, Function<String, T> valueFromArgumentFunction) {
        //noinspection unchecked Ahahaha fuck type erasure fuck type erasure fuck type erasure fucking why
        return (PermissionNode<T>) CACHE.get(path, path_ -> new PermissionNode<>(path_, positivePredicate, valueLoadFunction, valueSaveFunction, valueFromArgumentFunction, false));
    }

    /**
     * Register the permission node for use.
     */
    public static <T> PermissionNode<T> registerBoolean(String path) {
        //noinspection unchecked Ahahaha fuck type erasure fuck type erasure fuck type erasure fucking why
        return (PermissionNode<T>) CACHE.get(path, path_ -> new PermissionNode<>(path_, BOOLEAN, BOOLEAN_LOAD, BOOLEAN_SAVE, BOOLEAN_PARSE, false));
    }

    /**
     * Register the permission node for use.
     */
    public static <T> PermissionNode<T> registerInteger(String path) {
        //noinspection unchecked Ahahaha fuck type erasure fuck type erasure fuck type erasure fucking why
        return (PermissionNode<T>) CACHE.get(path, path_ -> new PermissionNode<>(path_, INTEGER, INTEGER_LOAD, INTEGER_SAVE, INTEGER_PARSE, false));
    }

    public boolean matches(PermissionNode<?> nodeToCheck) {
        // This is kinda terrible.
        if (nodeToCheck.path().equals(path())) { // Short circuit the relatively expensive node parsing.
            return true;
        }
        if (nodeToCheck.valueFromArgumentFunction() != BOOLEAN_PARSE) { // Don't try to wildcard non-booleans.
            return false;
        }
        String[] pathElements = path().split("\\.");
        String[] checkPathElements = nodeToCheck.path().split("\\.");
        final int length = Math.max(pathElements.length, checkPathElements.length);

        for (int i = 0; i < length; i++) {
            final String node1 = i < pathElements.length ? pathElements[i] : "*";
            final String node2 = i < checkPathElements.length ? checkPathElements[i] : null;
            if (node2 == null || !nodeSatisfiesNode(node1, node2))
                return false;
        }
        return true;
    }

    public static boolean nodeSatisfiesNode(@Nonnull String node1, String node2) {
        if (node1.equals("*"))
            return true;
        return node1.equals(node2);
    }

    @Override
    public @NotNull String toString() {
        return path;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        PermissionNode<?> that = (PermissionNode<?>) o;
        return path.equals(that.path);
    }

    @Override
    public int hashCode() {
        return Objects.hash(path);
    }

    public interface IsValuePositivePredicate<T> {
        boolean isPositive(PermissionNodeInstance<T> thisNode);
    }

}
