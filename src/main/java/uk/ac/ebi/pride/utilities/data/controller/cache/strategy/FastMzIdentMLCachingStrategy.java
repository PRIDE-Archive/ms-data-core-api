package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.FastMzIdentMLController;
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

    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {
        FastMzIdentMLUnmarshallerAdaptor unmarshaller = ((FastMzIdentMLController) controller).getUnmarshaller();
        cacheSpectraData(unmarshaller);
    }

    protected void cacheSpectraData(FastMzIdentMLUnmarshallerAdaptor unmarshaller) {
        // Map<"SD_1", SpectraData>
        Map<Comparable, SpectraData> oldSpectraDataMap = unmarshaller.getSpectraDataMap();

        if (oldSpectraDataMap != null && !oldSpectraDataMap.isEmpty()) {
            Map<Comparable, uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraDataMapResult = new HashMap<>();
            Iterator iterator = oldSpectraDataMap.entrySet().iterator();

            List<Comparable> listSpectraData = (List<Comparable>) cache.get(CacheEntry.SPECTRA_DATA_MGF_TITLE);
            if (listSpectraData == null)
                listSpectraData = new ArrayList<>();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                SpectraData spectraDataValue = (SpectraData) mapEntry.getValue();
                uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData = LightModelsTransformer.transformToSpectraData(spectraDataValue, listSpectraData.contains(spectraDataValue.getId()));
                spectraDataMapResult.put((Comparable) mapEntry.getKey(), spectraData);
            }
            cache.clear(CacheEntry.SPECTRA_DATA);
            cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataMapResult);
        }
    }
}