package uk.ac.ebi.pride.utilities.data.controller.cache;

//~--- JDK imports ------------------------------------------------------------

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * CachedMap is LRU map for caching.
 * <p/>
 * Note: this cache map is not thread safe,
 * one must use CacheFactory to create a thread safe version.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public class CachedMap<K, V> extends LinkedHashMap<K, V> {
    public static final int DEFAULT_CAPACITY = 10;

    private final int maxCapacity;

    public CachedMap() {
        this(DEFAULT_CAPACITY);
    }

    public CachedMap(int maxCapacity) {

        // 0.75f indicates the capacity threshold for resizing.
        // true indicates the access order
        super(maxCapacity + 1, .75F, true);
        this.maxCapacity = maxCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry eldest) {
        return size() > maxCapacity;
    }
}



