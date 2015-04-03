package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.cache.Cache;
import uk.ac.ebi.pride.utilities.data.controller.cache.CachingStrategy;

/**
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public abstract class AbstractCachingStrategy implements CachingStrategy {

    protected Cache cache;

    protected DataAccessController controller;

    public void setCache(Cache cache) {
        this.cache = cache;
    }

    public void setDataAccessController(DataAccessController controller) {
        this.controller = controller;
    }

    public Cache getCache() {
        return cache;
    }

    public DataAccessController getController() {
        return controller;
    }

    public void setController(DataAccessController controller) {
        this.controller = controller;
    }
}
