package uk.ac.ebi.pride.utilities.data.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Rui Wang
 */
public final class MapUtils {

    /**
     * Private Constructor
     */
    private MapUtils() {

    }

    public static <T, V> Map<T, V> createMapFromMap(Map<T, V> map) {
        Map<T, V> newMap = new HashMap<T, V>();

        if (map != null) {
            newMap.putAll(map);
        }

        return newMap;
    }

    public static <T, V> void replaceValuesInMap(Map<T, V> from, Map<T, V> to) {
        to.clear();
        if (from != null) {
            to.putAll(from);
        }
    }
}
