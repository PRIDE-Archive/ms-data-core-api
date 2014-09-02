package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PeakControllerImpl;
import uk.ac.ebi.pride.utilities.data.io.file.PeakUnmarshallerAdaptor;

import java.util.ArrayList;

/**
 * PeakCacheBuilder provides the methods to initialize the Cache Categories
 * for pure peaks list file formats.
 * <p/>
 * @author ypriverol
 * Date: 3/15/12
 * Time: 10:45 PM
 */
public class PeakCachingStrategy extends AbstractCachingStrategy {

    /**
     * For the moment, MzXmlCacheBuilder only caches spectrum ids and chromatogram ids.
     */
    @Override
    public void cache() {
        // get a direct reference to unmarshaller
        PeakUnmarshallerAdaptor unmarshaller = ((PeakControllerImpl) controller).getUnmarshaller();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(unmarshaller.getSpectrumIds()));
    }
}