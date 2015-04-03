package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.io.file.MzMLUnmarshallerAdaptor;

import java.util.ArrayList;

/**
 * The CacheBuilder class provides the functionality to initialize the cache
 * categories for mzML files.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 3/13/12
 * Time: 2:48 PM
 */
public class MzMlCachingStrategy extends AbstractCachingStrategy {

    /**
     * For the moment, MzXmlCacheBuilder only caches spectrum ids and chromatogram ids.
     */

    @Override
    public void cache() {
        // get a direct reference to unmarshaller
        MzMLUnmarshallerAdaptor unmarshaller = ((MzMLControllerImpl) controller).getUnmarshaller();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(unmarshaller.getSpectrumIds()));

        // clear and add chromatograms ids
        cache.clear(CacheEntry.CHROMATOGRAM_ID);
        cache.storeInBatch(CacheEntry.CHROMATOGRAM_ID, new ArrayList<Comparable>(unmarshaller.getChromatogramIds()));
    }
}
