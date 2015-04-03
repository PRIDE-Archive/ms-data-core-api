package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.mgf_parser.MgfFile;
import uk.ac.ebi.pride.tools.mgf_parser.model.Ms2Query;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.PeakControllerImpl;
import uk.ac.ebi.pride.utilities.data.io.file.PeakUnmarshallerAdaptor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * PeakCacheBuilder provides the methods to initialize the Cache Categories
 * for pure peaks list file formats.
 * <p/>
 * @author Yasset Perez-Riverol
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
        if(unmarshaller.isUseTitle() && unmarshaller.getUnmarshaller() instanceof MgfFile){
            Map<Comparable, Comparable> titleToIndex = new HashMap<Comparable, Comparable>();
            for(Comparable idSpec: unmarshaller.getSpectrumIds()){
                try {
                   Ms2Query spectrum = (Ms2Query) unmarshaller.getSpectrumById(idSpec.toString());
                   titleToIndex.put(spectrum.getTitle(), idSpec);
                } catch (JMzReaderException e) {
                    throw new DataAccessException("Failed to cache the mgf peak list file ", e);
                }
            }
            cache.clear(CacheEntry.TITLE_MGF_INDEX);
            cache.storeInBatch(CacheEntry.TITLE_MGF_INDEX, titleToIndex);
        }

    }
}