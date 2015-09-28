package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.NetCDFControllerImpl;

import uk.ac.ebi.pride.utilities.data.io.file.NetCDFUnmarshallerAdaptor;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Yasset Perez-Riverol (ypriverol@gmail.com)
 * @date 28/09/15
 */
public class NetCDFCachingStrategy extends AbstractCachingStrategy {

    @Override
    public void cache() {
        // get a direct reference to unmarshaller
        NetCDFUnmarshallerAdaptor unmarshaller = ((NetCDFControllerImpl) controller).getUnmarshaller();

        // clear and add spectrum ids
        cache.clear(CacheEntry.SPECTRUM_ID);
        cache.storeInBatch(CacheEntry.SPECTRUM_ID, new ArrayList<Comparable>(unmarshaller.getSpectrumIds()));
    }
}
