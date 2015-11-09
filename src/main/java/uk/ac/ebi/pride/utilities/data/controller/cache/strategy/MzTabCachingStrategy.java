package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzTabTransformer;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.io.file.MzTabUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import uk.ac.ebi.pride.utilities.util.Tuple;

import java.util.*;

/**
 * Cache the mzTab structures such as proteins, peptide evidences and spectrum references
 * //Todo: No way to match PSMs and Peptides which means that the current model is only for Protein + PMSs for identifications and Protein + Peptides for Quantitation.
 *
 *
 * @author Yasset Perez-Riverol
 */
public class MzTabCachingStrategy extends AbstractCachingStrategy {

    private static final int INIT_BIG_HASH = 10000;

    private static final Logger logger = LoggerFactory.getLogger(MzTabCachingStrategy.class);


    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {

        MzTabUnmarshallerAdaptor unmarshaller = ((MzTabControllerImpl) controller).getReader();

        /*
         * Cache Spectrum the PSM and Proteins in the experiment and retrieve the list of proteins in the experiment to be cached by the
         * Quatitative Section.
         */

        Map<String, String> proteinAccession = cacheSpectrumIds(unmarshaller);

        if(unmarshaller.hasQuantitationData())
            cacheQuantPeptideIds(unmarshaller, proteinAccession);

        if (!getCache().hasCacheEntry(CacheEntry.PROTEIN_ID)) {
            cacheProteins(unmarshaller);
        }

        if (hasProteinGroup(unmarshaller)) {
            cacheProteinGroups(unmarshaller);
        }
    }
    /**
     * Check if the MzTab File contrains Protein Group Information
     * @param unmarshaller The MzTab Unmarshaller
     * @return True if the mzTab contains protein group information.
     */
    private boolean hasProteinGroup(MzTabUnmarshallerAdaptor unmarshaller) {
        return unmarshaller.hasProteinGroup();
    }

    /**
     * Cache all the protein Groups
     * @param unmarshaller The MzTab Unmarshaller.
     */
    private void cacheProteinGroups(MzTabUnmarshallerAdaptor unmarshaller){

        List<String> proteinAmbiguityGroupIds = unmarshaller.getProteinGroupIds();

        if (proteinAmbiguityGroupIds != null && !proteinAmbiguityGroupIds.isEmpty()) {

            cache.clear(CacheEntry.PROTEIN_GROUP_ID);
            cache.storeInBatch(CacheEntry.PROTEIN_GROUP_ID, new ArrayList<Comparable>(proteinAmbiguityGroupIds));

            Tuple<Map<String, List<String>>, Map<String, List<String>>> proteinHIds = unmarshaller.getAllProteinAccessions();

            if (!proteinHIds.getValue().isEmpty()) {

                List<String> ids = new ArrayList<String>(proteinHIds.getValue().keySet());
                cache.clear(CacheEntry.PROTEIN_ID);
                cache.storeInBatch(CacheEntry.PROTEIN_ID, ids);

                cache.clear(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES);
                cache.storeInBatch(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES, proteinHIds.getValue());
            }

            if (!proteinHIds.getKey().isEmpty()) {
                cache.clear(CacheEntry.PROTEIN_TO_QUANTPEPTIDES);
                cache.storeInBatch(CacheEntry.PROTEIN_TO_QUANTPEPTIDES, proteinHIds.getKey());
            }
        }
    }

    /**
     * Cache all spectra and Spectrum Identification Items. It also returns the set of Proteins with accessions that will be use to cache the quantitative peptides
     * if the information is provided.
     *
     * @param unmarshaller the Mztab Unmarshaller
     */
    private Map<String, String> cacheSpectrumIds(MzTabUnmarshallerAdaptor unmarshaller){

        Map<Comparable, Tuple<String, String>> identSpectrumMap    = new HashMap<Comparable, Tuple<String, String>>(INIT_BIG_HASH);

        Map<Comparable, List<String>> spectraDataMap  = new HashMap<Comparable, List<String>>();

        Map<String, List<String>> proteinPSMIds = new HashMap<String, List<String>>(INIT_BIG_HASH);

        Map<String, String> proteinAccessions    = new HashMap<String, String>(INIT_BIG_HASH);

        List<Tuple<String, String>> spectrumIdentified = new ArrayList<Tuple<String, String>>();

        Map<Comparable, SpectraData> spectraDataIds   = MzTabTransformer.transformMsRunMap(unmarshaller.getMRunMap());

        for (Map.Entry psmEntry : unmarshaller.getPSMs().entrySet()) {

            String psmId = psmEntry.getKey().toString();

            PSM psm       = (PSM) psmEntry.getValue();

            SplitList<SpectraRef> refs = psm.getSpectraRef();

            //Every PSM is the reference of PSM + reference spectra
            int count = 1;

            for(SpectraRef ref:refs){

                String msRunId = ref.getMsRun().getId().toString();

                String reference = ref.getReference();

                String currentPSMId = psmId + "!" + count;

                if(spectraDataMap.containsKey(msRunId))
                    spectraDataMap.get(msRunId).add(currentPSMId);
                else{
                    List<String> psmIDs = new ArrayList<String>();
                    psmIDs.add(currentPSMId);
                    spectraDataMap.put(msRunId, psmIDs);
                }
                // extract the spectrum ID from the provided identifier
                String formattedSpectrumID = MzTabUtils.getSpectrumId(spectraDataIds.get(msRunId), reference);
                Tuple<String, String> spectrumFeatures = new Tuple<String, String>(formattedSpectrumID, msRunId);
                identSpectrumMap.put(currentPSMId, spectrumFeatures);
                spectrumIdentified.add(spectrumFeatures);

                for(Map.Entry proteinEntry: unmarshaller.getAllProteins().entrySet()){
                    Protein protein = (Protein) proteinEntry.getValue();
                    String proteinId = proteinEntry.getKey().toString();
                    List<String> psmIds = new ArrayList<String>();
                    if(psm.getAccession().equalsIgnoreCase(protein.getAccession())){
                        if(proteinPSMIds.containsKey(proteinId))
                            psmIds = proteinPSMIds.get(proteinId);
                        psmIds.add(currentPSMId);
                        proteinPSMIds.put(proteinId,psmIds);
                    }
                    if(!proteinAccessions.containsKey(proteinId))
                        proteinAccessions.put(proteinId, protein.getAccession());
                }

                count++;
            }
        }

        cache.clear(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        cache.storeInBatch(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS, spectraDataMap);

        cache.clear(CacheEntry.PEPTIDE_TO_SPECTRUM);
        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);

        cache.storeInBatch(CacheEntry.SPECTRUM_IDENTIFIED, spectrumIdentified);

        cache.clear(CacheEntry.SPECTRA_DATA);
        cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataIds);

        List<String> ids = new ArrayList<String>(proteinPSMIds.keySet());
        cache.clear(CacheEntry.PROTEIN_ID);
        cache.storeInBatch(CacheEntry.PROTEIN_ID, ids);

        cache.clear(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES);
        cache.storeInBatch(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES, proteinPSMIds);

        return proteinAccessions;

    }

    /*
     * Cache all proteins
     * @param unmarshaller The MzTab unmarshaller
     */
    private void cacheProteins(MzTabUnmarshallerAdaptor unmarshaller){

        Tuple<Map<String, List<String>>, Map<String, List<String>>> proteinHIds = unmarshaller.getAllProteinAccessions();

        if (!proteinHIds.getValue().isEmpty()) {

            List<String> ids = new ArrayList<String>(proteinHIds.getValue().keySet());
            cache.clear(CacheEntry.PROTEIN_ID);
            cache.storeInBatch(CacheEntry.PROTEIN_ID, ids);

            cache.clear(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES);
            cache.storeInBatch(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES, proteinHIds.getValue());
        }

        if (!proteinHIds.getKey().isEmpty()) {
            cache.clear(CacheEntry.PROTEIN_TO_QUANTPEPTIDES);
            cache.storeInBatch(CacheEntry.PROTEIN_TO_QUANTPEPTIDES, proteinHIds.getKey());
        }

    }

    /**
     * Cache all peptides that are quantified or different expressed in mzTab files.
     * @param unmarshaller mzTab Unmarshaller
     */

    private void cacheQuantPeptideIds(MzTabUnmarshallerAdaptor unmarshaller, Map<String, String> proteinAccession) {

        Map<Comparable, Tuple<String, String>> identSpectrumMap    = new HashMap<Comparable, Tuple<String, String>>();

        Map<Comparable, SpectraData> spectraDataIds   = MzTabTransformer.transformMsRunMap(unmarshaller.getMRunMap());

        Map<String, List<String>> proteinPeptides     = new HashMap<String, List<String>>();

        List<Tuple<String, String>> spectrumIdentified = new ArrayList<Tuple<String, String>>();

        for (Map.Entry peptideEntry : unmarshaller.getPeptides().entrySet()) {

            String peptideId          = peptideEntry.getKey().toString();

            Peptide peptide           = (Peptide) peptideEntry.getValue();

            SplitList<SpectraRef> refs = peptide.getSpectraRef();

            //Every Peptide is the reference of PSM + reference spectra
            int count = 1;

            // Some peptides do not contains the reference to the original spectra because is not mandatory.
            if(refs != null && !refs.isEmpty()){

                for(SpectraRef ref: refs){

                    String msRunId = ref.getMsRun().getId().toString();
                    String currentPeptideId = peptideId + "!" + count;

                    String reference = ref.getReference();

                    // extract the spectrum ID from the provided identifier
                    String formattedSpectrumID = MzTabUtils.getSpectrumId(spectraDataIds.get(msRunId), reference);
                    Tuple<String, String> spectrumFeatures = new Tuple<String, String>(formattedSpectrumID, msRunId);
                    identSpectrumMap.put(currentPeptideId, spectrumFeatures);
                    spectrumIdentified.add(spectrumFeatures);
                    count++;
                    for(Map.Entry proteinEntry: proteinAccession.entrySet()){
                        String proteinID = proteinEntry.getKey().toString();
                        String accession = proteinEntry.getValue().toString();
                        List<String> peptideIds = new ArrayList<String>();
                        if(peptide.getAccession().equalsIgnoreCase(accession)){
                            if(proteinPeptides.containsKey(proteinID))
                                peptideIds = proteinPeptides.get(proteinID);
                            peptideIds.add(currentPeptideId);
                            proteinPeptides.put(proteinID, peptideIds);
                        }
                    }

                }
            }else{
                identSpectrumMap.put(peptideId + "!" + count, null);
                for(Map.Entry proteinEntry: proteinAccession.entrySet()){
                    String proteinID = proteinEntry.getKey().toString();
                    String accession = proteinEntry.getValue().toString();
                    List<String> peptideIds = new ArrayList<String>();
                    if(peptide.getAccession().equalsIgnoreCase(accession)){
                        if(proteinPeptides.containsKey(proteinID))
                            peptideIds = proteinPeptides.get(proteinID);
                        peptideIds.add(peptideId + "!" + count);
                        proteinPeptides.put(proteinID, peptideIds);
                    }
                }
            }

        }

        cache.clear(CacheEntry.QUANTPEPTIDE_TO_SPECTREUM);
        cache.storeInBatch(CacheEntry.QUANTPEPTIDE_TO_SPECTREUM, identSpectrumMap);

        cache.storeInBatch(CacheEntry.SPECTRUM_IDENTIFIED, spectrumIdentified);

        cache.clear(CacheEntry.PROTEIN_TO_QUANTPEPTIDES);
        cache.storeInBatch(CacheEntry.PROTEIN_TO_QUANTPEPTIDES, proteinPeptides);


    }

}
