package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.FastMzIdentMLController;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.SimpleToJmzIdentMLTransformer;
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

    private static final int INIT_BIG_HASH = 10000;

    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {
        FastMzIdentMLUnmarshallerAdaptor unmarshaller = ((FastMzIdentMLController) controller).getUnmarshaller();
        //            cacheSpectrumIds(unmarshaller);

        // cache spectra data
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
                uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraDataValueJmzidml = SimpleToJmzIdentMLTransformer.convertSpectraDataToJmzidml(spectraDataValue);
                uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData = MzIdentMLTransformer.transformToSpectraData(spectraDataValueJmzidml, listSpectraData.contains(spectraDataValueJmzidml.getId()));
                spectraDataMapResult.put((Comparable) mapEntry.getKey(), spectraData);
            }
            cache.clear(CacheEntry.SPECTRA_DATA);
            cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataMapResult);
        }
    }

//    private void cacheSpectrumIds(FastMzIdentMLUnmarshallerAdaptor unmarshaller) throws ConfigurationException, JAXBException {
//
//        /**
//         * In some special cases the search engines when they have reference to spectra files use instead of the index for mgf, the Title in the mgf spectra.
//         * It meas that if the reference is index=1 instead of using the first spectra in the mgf we will use the CVTErm
//         * (cvRef="MS" accession="MS:1000796" name="spectrum title" value="Locus:1.1.1.942.2 File:&quot;24P 0_1ug 30min exit1 8.wiff&quot;") to find in the mgf
//         * the referenced spectrum.
//         * In these cases the process will be slow because we need to retrieve for each SpectrumIdentificationResult the title from a CVparam. We will start to support this cases
//         * only for ProteinPilot software, but it can be also the case in the future for other search engines.
//         */
//
//        boolean mgfTitleReference = false;
//
//        // Spectra details extracted from MzIdentML -> DataCollection -> Inputs
//        // eg:  <SpectraData location="file:///Carbamoyl-phosphate synthase small chain-47029-41-G2-4-biotools.mgf" id="SD_1"></SpectraData>
//        Map<Comparable, uk.ac.ebi.pride.utilities.data.mzid.lightModel.SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();
//
//        Map<Comparable, Tuple<String, String>> identSpectrumMap = new HashMap<Comparable, Tuple<String, String>>(INIT_BIG_HASH);
//        List<Tuple<String, String>> spectrumIdentified = new ArrayList<>(INIT_BIG_HASH);
//
//        Map<Comparable, List<Comparable>> spectraDataMap = new HashMap<Comparable, List<Comparable>>(spectraDataIds.size());
//
//        // get all the Spectrum Identification Lists from the mzIdentML object
//        List<SpectrumIdentificationList> psm = unmarshaller.getMzIdentML().getDataCollection().getAnalysisData().getSpectrumIdentificationList();
//
//        // Run through each SpectrumIdentificationList
//        for (SpectrumIdentificationList spectrumIdentificationList : psm) {
//
//            // Run through each SpectrumIdentificationResult
//            // eg: <SpectrumIdentificationResult id="SIR_12" spectrumID="index=35" spectraData_ref="SD_1">...</SpectrumIdentificationResult>
//            for (uk.ac.ebi.pride.utilities.data.mzid.lightModel.SpectrumIdentificationResult spectrumIdentificationResult : spectrumIdentificationList.getSpectrumIdentificationResult()) {
//
//                // id="SIR_12"
//                String spectrumIdentResultId = spectrumIdentificationResult.getId();
//
//                // eg: spectraData_ref="SD_1"
//                String spectrumDataReference = spectrumIdentificationResult.getSpectraDataRef();
//
//                // eg: spectrumID="index=35"
//                String spectrumID = spectrumIdentificationResult.getSpectrumID();
//
//                uk.ac.ebi.pride.utilities.data.mzid.lightModel.SpectraData spectraData = spectraDataIds.get(spectrumDataReference);
//
//                // fill the SpectraDataMap
//                // for the currently referenced spectra file, retrieve the List (if it exists already) that is to store all the spectra IDs
//                List<Comparable> spectrumIds = spectraDataMap.get(spectrumDataReference);
//
//                // if there is no spectra ID list for the spectrum file yet, then create one and add it to the map
//                if (spectrumIds == null) {
//                    spectrumIds = new ArrayList<Comparable>();
//                    spectraDataMap.put(spectrumDataReference, spectrumIds);
//                }
//                // add the spectrum ID to the list of spectrum IDs for the current spectrum file
//                spectrumIds.add(spectrumID);
//
//                for (SpectrumIdentificationItem spectrumIdentificationItem : spectrumIdentificationResult.getSpectrumIdentificationItem()) {
//
//                    String spectrumIdentItemId = spectrumIdentificationItem.getId();
//                    Tuple<String, String> spectrumFeatures = null;
//
//                    if (mgfTitleReference) {
//                        // TODO: need to cpmplete this
////                    if (mgfTitleReference && possibleMGMTitleReferenced.contains(spectrumDataReference)) {
////                        Comparable title = unmarshaller.getMGFTitleReference(spectrumIdentResultId);
////                        if(title != null){
////                            spectrumFeatures = new Tuple<String, String>(title.toString(), spectrumDataReference);
////                            identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);
////                            mgfTitleReferenceMap.put(spectrumFeatures, unmarshaller.getMGFTitleReference(spectrumIdentResultId));
////                            if(!spectraDataToMGF.contains(spectrumDataReference))
////                                spectraDataToMGF.add(spectrumDataReference);
////                        }
//// ;
//                    } else {
//                        // extract the spectrum ID from the provided identifier
//
//                        String formattedSpectrumID = MzIdentMLUtils.getSpectrumId(SimpleToJmzIdentMLTransformer.convertSpectraDataToJmzidml(spectraData), spectrumID);
////                        System.out.println("---------------- formated ID: " + formattedSpectrumID);
//
//                        spectrumFeatures = new Tuple<String, String>(formattedSpectrumID, spectrumDataReference);
//                        identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);
//                    }
//                    if (spectrumFeatures != null)
//                        spectrumIdentified.add(spectrumFeatures);
//                }
//            }
//        }
//
//        cache.clear(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
//        cache.storeInBatch(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS, spectraDataMap);
//
//        cache.clear(CacheEntry.PEPTIDE_TO_SPECTRUM);
//        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);
//
//        cache.storeInBatch(CacheEntry.SPECTRUM_IDENTIFIED, spectrumIdentified);
//
//        // TODO: need to cpmplete this
////        if(mgfTitleReference && mgfTitleReferenceMap.size() > 0){
////            cache.clear(CacheEntry.MGF_INDEX_TITLE);
////            cache.storeInBatch(CacheEntry.MGF_INDEX_TITLE, mgfTitleReferenceMap);
////
////            cache.clear(CacheEntry.SPECTRA_DATA_MGF_TITLE);
////            cache.storeInBatch(CacheEntry.SPECTRA_DATA_MGF_TITLE, spectraDataToMGF);
////        }
//
//    }
}