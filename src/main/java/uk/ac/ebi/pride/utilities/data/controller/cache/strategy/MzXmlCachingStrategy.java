package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzXmlControllerImpl;
import uk.ac.ebi.pride.utilities.data.io.file.MzXmlUnmarshallerAdaptor;

import java.util.ArrayList;

/**
 * MzMlAccessCacheBuilder initialize the cache for mzML reading.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class MzXmlCachingStrategy extends AbstractCachingStrategy {

    /**
     * For the moment, MzXmlCacheBuilder only caches spectrum ids and chromatogram ids.
     */
    @Override
    public void cache() {
        // get a direct reference to unmarshaller
        MzXmlUnmarshallerAdaptor unmarshaller = ((MzXmlControllerImpl) controller).getUnmarshaller();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(unmarshaller.getSpectrumIds()));
    }
}



