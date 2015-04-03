package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzDataControllerImpl;
import uk.ac.ebi.pride.utilities.data.io.file.MzDataUnmarshallerAdaptor;

import java.util.ArrayList;

/**
 * Class to Create the Cache for MzData Files.
 * @author Yasset Perez-Riverol
 * Date: 3/15/12
 * Time: 8:00 AM
 */
public class MzDataCachingStrategy extends AbstractCachingStrategy {

    /**
     * For the moment, MzXmlCacheBuilder only caches spectrum ids and chromatogram ids.
     */
    @Override
    public void cache() {

        // get a direct reference to unmarshaller
        MzDataUnmarshallerAdaptor unmarshaller = ((MzDataControllerImpl) controller).getUnmarshaller();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(unmarshaller.getSpectrumIds()));
    }
}