package uk.ac.ebi.pride.utilities.data.controller.cache;

import java.util.Collection;
import java.util.Map;

/**
 * Cache interface defines methods for storing data into cache
 * and retrieving data from cache.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 * Date: 07-Sep-2010
 * Time: 11:14:53
 */
public interface Cache {

    /**
     * Store data into cache by key-value pairs
     *
     * @param type  cache type
     * @param key   key
     * @param value value
     */
    void store(CacheEntry type, Object key, Object value);

    /**
     * Store a map of data into the cache
     *
     * @param type   cache type
     * @param values a map of values
     */
    void storeInBatch(CacheEntry type, Map values);

    /**
     * Store data into cache by key only
     *
     * @param type cache type
     * @param key  key
     */
    void store(CacheEntry type, Object key);

    /**
     * Store a collection into the cache
     *
     * @param type   cache category
     * @param values a collection of data
     */
    void storeInBatch(CacheEntry type, Collection values);

    /**
     * Get data from cache using CacheCategory and key
     *
     * @param type cache category
     * @param key  key
     * @return Object value
     */
    Object get(CacheEntry type, Object key);

    /**
     * Get data from cache using CacheCategory and a collections of keys
     *
     * @param type cache category
     * @param keys keys
     * @return Object value
     */
    Collection getInBatch(CacheEntry type, Collection keys);

    /**
     * Check whether the cache already has the cache category
     *
     * @param type CacheCategory
     * @return boolean  true if the category exists
     */
    boolean hasCacheEntry(CacheEntry type);

    /**
     * Get data from cache using CacheCategory
     *
     * @param type cache category
     * @return Object   key
     */
    Object get(CacheEntry type);

    /**
     * Clear all data assigned to CacheCategory
     *
     * @param type cache category
     */
    void clear(CacheEntry type);

    /**
     * Clear all data
     */
    void clear();
}



