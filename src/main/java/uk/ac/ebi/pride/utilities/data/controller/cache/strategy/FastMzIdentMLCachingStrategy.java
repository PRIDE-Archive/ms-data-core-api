package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.FastMzIdentMLController;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.LightModelsTransformer;
import uk.ac.ebi.pride.utilities.data.io.file.FastMzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.lightModel.SpectraData;

import java.util.*;

/**
 * The FastMzIdentMLCacheBuilder initialize the cache for mzIdentML file reading.
 *
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLCachingStrategy extends AbstractCachingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(FastMzIdentMLCachingStrategy.class);

    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {
        FastMzIdentMLUnmarshallerAdaptor unmarshaller = ((FastMzIdentMLController) controller).getUnmarshaller();
        cacheSpectraData(unmarshaller);
    }

    protected void cacheSpectraData(FastMzIdentMLUnmarshallerAdaptor unmarshaller) {
        Map<Comparable, SpectraData> oldSpectraDataMap = unmarshaller.getSpectraDataMap();

        if (oldSpectraDataMap != null && !oldSpectraDataMap.isEmpty()) {
            Map<Comparable, uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraDataMapResult = new HashMap<Comparable, uk.ac.ebi.pride.utilities.data.core.SpectraData>();
            Iterator iterator = oldSpectraDataMap.entrySet().iterator();

            List<Comparable> listSpectraData = (List<Comparable>) cache.get(CacheEntry.SPECTRA_DATA_MGF_TITLE);
            if (listSpectraData == null)
                listSpectraData = new ArrayList<Comparable>();

            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                SpectraData spectraDataValue = (SpectraData) mapEntry.getValue();
                uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraDataValueJmzidml = LightModelsTransformer.transformSpectraDataToJmzidml(spectraDataValue);
                uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData = MzIdentMLTransformer.transformToSpectraData(spectraDataValueJmzidml, listSpectraData.contains(spectraDataValueJmzidml.getId()));
                spectraDataMapResult.put((Comparable) mapEntry.getKey(), spectraData);
            }
            cache.clear(CacheEntry.SPECTRA_DATA);
            cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataMapResult);
        }
    }
}