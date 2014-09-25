package uk.ac.ebi.pride.utilities.data.controller.cache.strategy;


import uk.ac.ebi.pride.jmztab.model.Peptide;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.Transformer.MzTabTransformer;
import uk.ac.ebi.pride.utilities.data.core.SpectraData;
import uk.ac.ebi.pride.utilities.data.io.file.MzTabUnmarshallerAdaptor;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;

import uk.ac.ebi.pride.jmztab.model.PSM;
import uk.ac.ebi.pride.jmztab.model.SpectraRef;
import uk.ac.ebi.pride.jmztab.model.SplitList;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.util.*;

/**
 * Cache the mzTab structures such as proteins, peptide evidences and spectrum references
 * //Todo: No way to match PSMs and Peptides which means that the current model is only for Protein + PMSs for identifications and Protein + Peptides for Quantitation.
 *
 *
 * @author ypriverol
 */
public class MzTabCachingStrategy extends AbstractCachingStrategy {

    /**
     * Spectrum ids and identification ids are cached.
     */
    @Override
    public void cache() {

        MzTabUnmarshallerAdaptor unmarshaller = ((MzTabControllerImpl) controller).getReader();

        cacheSpectrumIds(unmarshaller);


        /* Get a preScan of the File, the PreCan of the mzidentml File gets the information
         * about all the spectrums, protein identifications, and peptide-spectrum matchs with the
         * same structure that currently follow the mzidentml library.
         * */
        if(hasProteinGroup(unmarshaller)){
            cacheProteinGroups(unmarshaller);
        }else{
            cacheProteins(unmarshaller);
        }

        if(unmarshaller.hasQuantitationData())
            cacheQuantPeptideIds(unmarshaller);

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
     * Cache all spectra and Spectrum Identification Items
     * @param unmarshaller the Mztab Unmarshaller
     */
    private void cacheSpectrumIds(MzTabUnmarshallerAdaptor unmarshaller){

        Map<Comparable, String[]> identSpectrumMap    = new HashMap<Comparable, String[]>();

        Map<Comparable, List<String>> spectraDataMap  = new HashMap<Comparable, List<String>>();

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

                if(spectraDataMap.containsKey(msRunId))
                    spectraDataMap.get(msRunId).add(psmId.toString());

                else{
                    List<String> psmIDs = new ArrayList<String>();
                    psmIDs.add(psmId.toString());
                    spectraDataMap.put(msRunId, psmIDs);
                }
                // extract the spectrum ID from the provided identifier
                String formattedSpectrumID = MzTabUtils.getSpectrumId(spectraDataIds.get(msRunId), reference);
                String[] spectrumFeatures = {formattedSpectrumID, msRunId};
                identSpectrumMap.put(psmId.toString() + "!" + count, spectrumFeatures);
                count++;
            }
        }

        cache.clear(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        cache.storeInBatch(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS, spectraDataMap);

        cache.clear(CacheEntry.PEPTIDE_TO_SPECTRUM);
        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);

        cache.clear(CacheEntry.SPECTRA_DATA);
        cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataIds);
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

    private void cacheQuantPeptideIds(MzTabUnmarshallerAdaptor unmarshaller) {

        Map<Comparable, String[]> identSpectrumMap    = new HashMap<Comparable, String[]>();

        Map<Comparable, List<String>> spectraDataMap  = new HashMap<Comparable, List<String>>();

        Map<Comparable, SpectraData> spectraDataIds   = MzTabTransformer.transformMsRunMap(unmarshaller.getMRunMap());

        for (Map.Entry psmEntry : unmarshaller.getPeptides().entrySet()) {

            String psmId  = psmEntry.getKey().toString();

            PSM psm       = (PSM) psmEntry.getValue();

            SplitList<SpectraRef> refs = psm.getSpectraRef();

            //Every PSM is the reference of PSM + reference spectra
            int count = 1;

            for(SpectraRef ref:refs){

                String msRunId = ref.getMsRun().getId().toString();

                String reference = ref.getReference();

                if(spectraDataMap.containsKey(msRunId))
                    spectraDataMap.get(msRunId).add(psmId.toString());
                else{
                    List<String> psmIDs = new ArrayList<String>();
                    psmIDs.add(psmId.toString());
                    spectraDataMap.put(msRunId, psmIDs);
                }
                // extract the spectrum ID from the provided identifier
                String formattedSpectrumID = MzTabUtils.getSpectrumId(spectraDataIds.get(msRunId), reference);
                String[] spectrumFeatures = {formattedSpectrumID, msRunId};
                identSpectrumMap.put(psmId.toString() + "!" + count, spectrumFeatures);
                count++;
            }
        }

        cache.clear(CacheEntry.QUANTPEPTIDE_TO_SPECTREUM);
        cache.storeInBatch(CacheEntry.PEPTIDE_TO_SPECTRUM, identSpectrumMap);

        cache.clear(CacheEntry.SPECTRA_DATA);
        cache.storeInBatch(CacheEntry.SPECTRA_DATA, spectraDataIds);

    }

    private boolean matchPSMPeptide(Peptide peptide, PSM psm){
        if(peptide.getSequence().equalsIgnoreCase(psm.getSequence()) &&
           peptide.getAccession().equalsIgnoreCase(psm.getAccession())){
            for(SpectraRef ref: peptide.getSpectraRef())
                if(psm.getSpectraRef().contains(ref))
                    return true;
        }
        return false;
    }
}
