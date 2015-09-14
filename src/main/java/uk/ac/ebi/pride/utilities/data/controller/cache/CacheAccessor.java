package uk.ac.ebi.pride.utilities.data.controller.cache;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.*;

/**
 * CacheAccessor is the implementation of cache.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class CacheAccessor implements Cache {
    private static final Logger logger = LoggerFactory.getLogger(CacheAccessor.class);

    /**
     * All data are stored in here.
     */
    private final Map<CacheEntry, Object> contents;

    /**
     * CacheAccessor constructor
     */
    public CacheAccessor() {
        contents = new HashMap<CacheEntry, Object>();
    }

    /**
     * Store key into cache, type should be a collection type.
     *
     * @param type cache type
     * @param key  key
     */
    @Override
    public void store(CacheEntry type, Object key) {
        store(type, key, null);
    }

    /**
     * Store a key-value pair into cache, type should be a map type.
     *
     * @param type  cache type
     * @param key   key
     * @param value value
     */
    @Override
    @SuppressWarnings(value = "unchecked")
    public void store(CacheEntry type, Object key, Object value) {
        Object content = createIfNotExist(type);

        // put into a map if underlying data structure is map
        if (content instanceof Map) {

            // must do this check here
            if (key == null) {
                String errMsg = "Key and value cannot be null (key: " + null + ", value: " + value + ")";

                logger.error(errMsg);

                throw new IllegalArgumentException(errMsg);
            }

            ((Map) content).put(key, value);
        } else if (content instanceof Collection) {
            ((Collection) content).add(key);
        } else {
            String errMsg = "Cannot store key-value pair to a data structure other than map";

            logger.error(errMsg);

            throw new IllegalArgumentException(errMsg);
        }
    }

    /**
     * Store a map of values
     *
     * @param type   cache type
     * @param values a map of values
     */
    @Override
    @SuppressWarnings("unchecked")
    public void storeInBatch(CacheEntry type, Map values) {
        if (values == null) {
            String errMsg = "Map values cannot be null";

            logger.error(errMsg);

            throw new IllegalArgumentException(errMsg);
        }

        if (!values.isEmpty()) {
            Object content = createIfNotExist(type);

            if (content instanceof Map) {
                ((Map) content).putAll(values);
            }
        }
    }

    /**
     * Store a collection of values
     *
     * @param type   cache category
     * @param values a collection of data
     */
    @Override
    @SuppressWarnings("unchecked")
    public void storeInBatch(CacheEntry type, Collection values) {
        if (values == null) {
            String errMsg = "Collection values cannot be null";

            logger.error(errMsg);

            throw new IllegalArgumentException(errMsg);
        }

        if (!values.isEmpty()) {
            Object content = createIfNotExist(type);

            if (content instanceof Collection) {
                ((Collection) content).addAll(values);
            }
        }
    }

    /**
     * Get the stored data using a key.
     *
     * @param type cache category
     * @param key  key
     * @return Object data
     */
    @Override
    public Object get(CacheEntry type, Object key) {
        return retrieveContent(type, key);
    }

    /**
     * Get a collection of values using a collection of keys.
     * Note: keys with no matching value will automatically be ignored.
     *
     * @param type cache category
     * @param keys keys
     * @return Collection   value collection
     */
    @Override
    public Collection getInBatch(CacheEntry type, Collection keys) {
        Collection<Object> results = new ArrayList<Object>();

        for (Object key : keys) {
            Object val = retrieveContent(type, key);

            if (val != null) {
                results.add(val);
            }
        }

        return results;
    }

    /**
     * Get the stored data set using a type.
     *
     * @param type cache category
     * @return Object   data
     */
    @Override
    public Object get(CacheEntry type) {
        return retrieveContent(type, null);
    }

    /**
     * Check the cachecategory availability
     *
     * @param type CacheCategory
     * @return boolean  true if exists.
     */
    @Override
    public boolean hasCacheEntry(CacheEntry type) {
        return contents.containsKey(type);
    }

    /**
     * Clear a cache category
     *
     * @param type cache category
     */
    @Override
    public void clear(CacheEntry type) {
        contents.remove(type);
    }

    /**
     * Clear all cache
     */
    @Override
    public void clear() {
        contents.clear();
    }

    @SuppressWarnings("unchecked")
    private Object createIfNotExist(CacheEntry type) {

        Object content = contents.get(type);

        if (content == null) {
            Class className = type.getDataStructType();
            Integer size = type.getSize();

            // create a new data structure with the specified size
            try {
                if (size != null) {
                    Constructor constructor = className.getDeclaredConstructor(int.class);

                    content = constructor.newInstance(size);
                } else {

                    // create a new instance of the data structure
                    content = type.getDataStructType().newInstance();
                }
            } catch (Exception e) {
                logger.error("Failed to initialize data structure for caching", e);
            }

            // synchronized wrapping
            if (content != null && content instanceof Map) {
                content = Collections.synchronizedMap((Map) content);
            } else if (content instanceof Collection) {
                content = Collections.synchronizedCollection((Collection) content);
            }

            // store data
            if (content != null) {
                contents.put(type, content);
            }
        }

        return content;
    }

    @SuppressWarnings("unchecked")
    private Object retrieveContent(CacheEntry type, Object key) {
        Object result = null;
        Object content = contents.get(type);

        if (content != null) {

            // get the value
            if (key == null) {
                result = content;
            } else {
                if (content instanceof Map) {
                    result = ((Map) content).get(key);
                } else {
                    result = content;
                }
            }
        }

        // return a new data structure which contains all the elements
        if (result != null && result instanceof Map) {
//            Map temp = new LinkedHashMap();
//
//            temp.putAll((Map) result);
//            result = temp;
            return result;
        } else if (result instanceof Collection) {
//            List temp = new ArrayList();
//
//            temp.addAll((Collection) result);
//            result = temp;
            return result;
        }

        return result;
    }
}



