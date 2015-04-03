package uk.ac.ebi.pride.utilities.data.controller.cache;

import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;

/**
 * Interface for building the cache.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public interface CachingStrategy {

    /**
     * initialize the cache, it should clear the previous cache first,
     * then createAttributedSequence the new cache.
     */
    void cache();

    void setDataAccessController(DataAccessController dataAccessController);

    void setCache(Cache cache);
}



