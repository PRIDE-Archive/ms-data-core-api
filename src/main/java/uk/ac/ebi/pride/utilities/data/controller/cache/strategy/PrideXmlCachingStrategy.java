package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PrideXmlControllerImpl;
import uk.ac.ebi.pride.jaxb.xml.PrideXmlReader;

import java.util.ArrayList;

/**
 * PrideXmlAccessCacheBuilder initialize the cache for pride xml reading.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */
public class PrideXmlCachingStrategy extends AbstractCachingStrategy {

    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {
        // get a reference to xml reader
        PrideXmlReader reader = ((PrideXmlControllerImpl) controller).getReader();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(reader.getSpectrumIds()));

        // clear and add peptide ids
        cache.clear(CacheEntry.PROTEIN_ID);
        cache.storeInBatch(CacheEntry.PROTEIN_ID, new ArrayList<Comparable>(reader.getIdentIds()));
    }
}



