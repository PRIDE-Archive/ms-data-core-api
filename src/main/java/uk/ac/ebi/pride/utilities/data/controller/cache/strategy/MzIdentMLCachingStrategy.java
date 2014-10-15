package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationResult;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzIdentMLTransformer;
import uk.ac.ebi.pride.utilities.data.core.CVLookup;
import uk.ac.ebi.pride.utilities.data.core.IdentifiableParamGroup;
import uk.ac.ebi.pride.utilities.data.io.file.MzIdentMLUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;

import javax.naming.ConfigurationException;
import java.util.*;

/**
 * The MzIdentMLCacheBuilder initialize the cache for mzidentml file  reading.
 * <p/>
 * @author ypriverol
 * @author rwang
 */
public class MzIdentMLCachingStrategy extends AbstractCachingStrategy {

    private static final Logger logger = LoggerFactory.getLogger(MzIdentMLCachingStrategy.class);


    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {
        MzIdentMLUnmarshallerAdaptor unmarshaller = ((MzIdentMLControllerImpl) controller).getUnmarshaller();
        boolean proteinGroupPresent = hasProteinGroup(unmarshaller);

        // cache spectra data
        cacheSpectraData(unmarshaller);

        // fragmentation table
        cacheFragmentationTable(unmarshaller);

        // cvlookup map
        cacheCvlookupMap(unmarshaller);

        /* Get a preScan of the File, the PreCan of the mzidentml File gets the information
         * about all the spectrums, protein identifications, and peptide-spectrum matchs with the
         * same structure that currently follow the mzidentml library.
         * */
        try {
            if (proteinGroupPresent) {
                cacheSpectrumIds(unmarshaller);
                cacheProteinGroups(unmarshaller);
            } else {
                cachePrescanIdMaps(unmarshaller);
            }
        } catch (ConfigurationException e) {
            throw new DataAccessException("Failed to prescan id maps for mzIdentML file", e);
        }
    }

    private boolean hasProteinGroup(MzIdentMLUnmarshallerAdaptor unmarshaller) {
        boolean proteinGroupPresent;
        try {
            proteinGroupPresent = unmarshaller.hasProteinGroup();
        } catch (ConfigurationException e) {
            String msg = "Failed while checking whether protein groups are present";
            logger.error(msg, e);
            throw new DataAccessException(msg, e);
        }
        return proteinGroupPresent;
    }

    protected void cacheCvlookupMap(MzIdentMLUnmarshallerAdaptor unmarshaller) {
        List<CVLookup> cvLookupList = MzIdentMLTransformer.transformCVList(unmarshaller.getCvList());
        if (cvLookupList != null && !cvLookupList.isEmpty()) {
            Map<String, CVLookup> cvLookupMap = new HashMap<String, CVLookup>();
            for (CVLookup cvLookup : cvLookupList) {
                cvLookupMap.put(cvLookup.getCvLabel(), cvLookup);
            }
            cache.clear(CacheEntry.CV_LOOKUP);
            cache.storeInBatch(CacheEntry.CV_LOOKUP, cvLookupMap);
        }
    }

    protected void cacheFragmentationTable(MzIdentMLUnmarshallerAdaptor unmarshaller) {
        uk.ac.ebi.jmzidml.model.mzidml.FragmentationTable oldFragmentationTable = unmarshaller.getFragmentationTable();
        if (oldFragmentationTable != null) {
            Map<String, IdentifiableParamGroup> fragmentationTable = MzIdentMLTransformer.transformToFragmentationTable(oldFragmentationTable);
            cache.clear(CacheEntry.FRAGMENTATION_TABLE);
            cache.storeInBatch(CacheEntry.FRAGMENTATION_TABLE, fragmentationTable);
        }
    }

    protected void cacheSpectraData(MzIdentMLUnmarshallerAdaptor unmarshaller) {
        Map<Comparable, uk.ac.ebi.jmzidml.model.mzidml.SpectraData> oldSpectraDataMap = unmarshaller.getSpectraDataMap();

        if (oldSpectraDataMap != null && !oldSpectraDataMap.isEmpty()) {
            Map<Comparable, uk.ac.ebi.pride.utilities.data.core.SpectraData> spectraDataMapResult = new HashMap<Comparable, uk.ac.ebi.pride.utilities.data.core.SpectraData>();

            Iterator iterator = oldSpectraDataMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData = MzIdentMLTransformer.transformToSpectraData((uk.ac.ebi.jmzidml.model.mzidml.SpectraData) mapEntry.getValue());
                //if (isSpectraDataSupported(spectraData)) {
                spectraDataMapResult.put((Comparable) mapEntry.getKey(), spectraData);
                //}
            }
            cache.clear(CacheEntry.SPECTRA_DATA);
            cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataMapResult);
        }
    }

    private boolean isSpectraDataSupported(uk.ac.ebi.pride.utilities.data.core.SpectraData spectraData) {
        return (!(MzIdentMLUtils.getSpectraDataIdFormat(spectraData) == Constants.SpecIdFormat.NONE ||
                Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.NONE));
    }


    private void cacheProteinGroups(MzIdentMLUnmarshallerAdaptor unmarshaller) throws ConfigurationException {

        Set<String> proteinAmbiguityGroupIds = unmarshaller.getIDsForElement(MzIdentMLElement.ProteinAmbiguityGroup);

        if (proteinAmbiguityGroupIds != null && !proteinAmbiguityGroupIds.isEmpty()) {

            cache.clear(CacheEntry.PROTEIN_GROUP_ID);
            cache.storeInBatch(CacheEntry.PROTEIN_GROUP_ID, new ArrayList<Comparable>(proteinAmbiguityGroupIds));

            List<Comparable> proteinHIds = new ArrayList<Comparable>(unmarshaller.getIDsForElement(MzIdentMLElement.ProteinDetectionHypothesis));

            if (!proteinHIds.isEmpty()) {
                cache.clear(CacheEntry.PROTEIN_ID);
                cache.storeInBatch(CacheEntry.PROTEIN_ID, proteinHIds);
            }
        }
    }

    private void cacheSpectrumIds(MzIdentMLUnmarshallerAdaptor unmarshaller) throws ConfigurationException {

        Map<Comparable, String[]> identSpectrumMap = new HashMap<Comparable, String[]>();


        Set<String> spectrumIdentResultIds = unmarshaller.getIDsForElement(MzIdentMLElement.SpectrumIdentificationResult);

        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();

        Map<Comparable, List<Comparable>> spectraDataMap = new HashMap<Comparable, List<Comparable>>(spectraDataIds.size());

        for (String spectrumIdentResultId : spectrumIdentResultIds) {

            Map<String, String> spectrumIdentificationResultAttributes = unmarshaller.getElementAttributes(spectrumIdentResultId, SpectrumIdentificationResult.class);
            String spectrumDataReference = spectrumIdentificationResultAttributes.get("spectraData_ref");
            String spectrumID = spectrumIdentificationResultAttributes.get("spectrumID");
            SpectraData spectraData = spectraDataIds.get(spectrumDataReference);

            // fill the SpectraDataMap
            // for the currently referenced spectra file, retrieve the List (if it exists already) that is to store all the spectra IDs
            List<Comparable> spectrumIds = spectraDataMap.get(spectrumDataReference);
            // if there is no spectra ID list for the spectrum file yet, then create one and add it to the map
            if (spectrumIds == null) {
                spectrumIds = new ArrayList<Comparable>();
                spectraDataMap.put(spectrumDataReference, spectrumIds);
            }
            // add the spectrum ID to the list of spectrum IDs for the current spectrum file
            spectrumIds.add(spectrumID);

            // proceed to populate the identSpectrumMap
            Set<String> spectrumIdentItemIds = unmarshaller.getSpectrumIdentificationItemIds(spectrumIdentResultId);
            for (String spectrumIdentItemId : spectrumIdentItemIds) {

                // extract the spectrum ID from the provided identifier
                String formattedSpectrumID = MzIdentMLUtils.getSpectrumId(spectraData, spectrumID);
                String[] spectrumFeatures = {formattedSpectrumID, spectrumDataReference};

                identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);
            }
        }

        cache.clear(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        cache.storeInBatch(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS, spectraDataMap);

        cache.clear(CacheEntry.PEPTIDE_TO_SPECTRUM);
        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);
    }

    /**
     * This function try to Map in memory ids mapping and relation for an mzidentml file. The structure of the
     * mzidentml files is from spectrum->peptide->protein, but most for the end users is more interesting to
     * have an information structure from protein->peptide->spectrum. The function take the information from
     * spectrumItems and read the Peptide Evidences and the Proteins related with these peptideEvidence. Finally
     * the function construct a map in from proteins to spectrums named identProteinsMap.
     *
     * @throws javax.naming.ConfigurationException
     *
     */
    private void cachePrescanIdMaps(MzIdentMLUnmarshallerAdaptor unmarshaller) throws ConfigurationException {

        /**
         * Map of IDs to SpectraData, e.g. IDs to spectra files
         */
        Map<Comparable, SpectraData> spectraDataIds = unmarshaller.getSpectraDataMap();


        /**
         * First Map is the Relation between an Spectrum file and all the Spectrums ids in the file
         * This information is useful to retrieve the for each spectrum File with spectrums are really
         * SpectrumIdentificationItems. For PRIDE Inspector is important for one of the windows that
         * shows the number of missing spectrum for an mzidentml file.
         * Map of SpectraData IDs to List of spectrum IDs, e.g. which spectra come from which file
         */
        Map<Comparable, List<Comparable>> spectraDataMap = new HashMap<Comparable, List<Comparable>>(spectraDataIds.size());

        /**
         * The relation between the peptide evidence and the spectrumIdentificationItem.
         * This map allow the access to the peptide evidence and spectrum information
         * without the Protein information.
         * ???? PeptideEvidence ????
         *
         * Map of SII IDs to a String[2] of spectrum ID and spectrum file ID
         */
        Map<Comparable, String[]> identSpectrumMap = new HashMap<Comparable, String[]>();


        /**
         * List of PSMs, e.g. SpectrumIdentificationResult IDs
         */
        Set<String> spectrumIdentResultIds = unmarshaller.getIDsForElement(MzIdentMLElement.SpectrumIdentificationResult);

        /**
         * This Protein Map represents the Protein identification in the DBSequence Section that contains SpectrumIdentification Items
         * Each key is the Protein Id, the Map related with each key is a Peptide Evidence Map. Each Peptide Evidence Map contains a key
         * of the for a PeptideEvidence and a list of SpectrumIdentificationItems. Each Sepctrum Identification Item is that contains
         * the original id of the spectrum in the Spectrum file and the id of the spectrum file.
         *
         */
        Map<Comparable, List<Comparable>> identProteinsMap = new HashMap<Comparable, List<Comparable>>();

        for (String spectrumIdentResultId : spectrumIdentResultIds) {

            Map<String, String> spectrumIdentificationResultAttributes = unmarshaller.getElementAttributes(spectrumIdentResultId, SpectrumIdentificationResult.class);
            String spectrumDataReference = spectrumIdentificationResultAttributes.get("spectraData_ref");
            String spectrumID = spectrumIdentificationResultAttributes.get("spectrumID");

            // fill the SpectraDataMap
            // for the currently referenced spectra file, retrieve the List (if it exists already) that is to store all the spectra IDs
            List<Comparable> spectrumIds = spectraDataMap.get(spectrumDataReference);
            // if there is no spectra ID list for the spectrum file yet, then create one and add it to the map
            if (spectrumIds == null) {
                spectrumIds = new ArrayList<Comparable>();
                spectraDataMap.put(spectrumDataReference, spectrumIds);
            }
            // add the spectrum ID to the list of spectrum IDs for the current spectrum file
            spectrumIds.add(spectrumID);

            // proceed to populate the identSpectrumMap
            Set<String> spectrumIdentItemIds = unmarshaller.getSpectrumIdentificationItemIds(spectrumIdentResultId);
            for (String spectrumIdentItemId : spectrumIdentItemIds) {

                // fill the SpectrumIdentification and the Spectrum information
                SpectraData spectraData = spectraDataIds.get(spectrumDataReference);

                // extract the spectrum ID from the provided identifier
                String formattedSpectrumID = MzIdentMLUtils.getSpectrumId(spectraData, spectrumID);
                String[] spectrumFeatures = {formattedSpectrumID, spectrumDataReference};

                identSpectrumMap.put(spectrumIdentItemId, spectrumFeatures);

                Set<Comparable> idProteins = new HashSet<Comparable>();
                Set<String> peptideEvidenceReferences = unmarshaller.getPeptideEvidenceReferences(spectrumIdentResultId, spectrumIdentItemId);
                for (String peptideEvidenceReference : peptideEvidenceReferences) {
                    Map<String, String> attributes = unmarshaller.getElementAttributes(peptideEvidenceReference, PeptideEvidence.class);
                    idProteins.add(attributes.get("dBSequence_ref"));
                }

                for (Comparable idProtein : idProteins) {
                    List<Comparable> spectrumIdentifications = identProteinsMap.get(idProtein);
                    if (spectrumIdentifications == null) {
                        spectrumIdentifications = new ArrayList<Comparable>();
                        identProteinsMap.put(idProtein, spectrumIdentifications);
                    }
                    spectrumIdentifications.add(spectrumIdentItemId);
                }
            }
        }

        // Protein To to Peptides Evidences, It retrieve the peptides per Proteins
        cache.clear(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES);
        cache.storeInBatch(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES, identProteinsMap);

        cache.clear(CacheEntry.PROTEIN_ID);
        cache.storeInBatch(CacheEntry.PROTEIN_ID, new ArrayList<Comparable>(identProteinsMap.keySet()));

        cache.clear(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        cache.storeInBatch(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS, spectraDataMap);

        cache.clear(CacheEntry.PEPTIDE_TO_SPECTRUM);
        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);
    }
}



