package dev.robocode.tankroyale.botapi.util;

import java.util.*;

/**
 * Collection utility class.
 */
public final class CollectionUtil {

    // Hides constructor
    private CollectionUtil() {
    }

    /**
     * Creates a mutable list that is a copy of another list.
     *
     * @param list is the list to copy, where {@code null} results in returning an empty mutable list.
     * @return a mutable list that is a copy of the input list.
     */
    public static <T> List<T> toMutableList(List<T> list) {
        return list == null ? new ArrayList<>() : new ArrayList<>(list);
    }

    /**
     * Creates a mutable set that copies all items from a collection, but is removing duplicates.
     *
     * @param collection is the collection to copy, where {@code null} results in returning an empty mutable set.
     * @return a mutable set that is a copy of the input collection.
     */
    public static <T> Set<T> toMutableSet(Collection<T> collection) {
        return collection == null ? new HashSet<>() : new HashSet<>(collection);
    }
}
