package uk.ac.ebi.pride.utilities.data.io.file;

import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.*;


/**
 * Unmarshaller an MZTab file
 * @author ypriverol
 * @author rwang
 */
public class MzTabUnmarshallerAdaptor extends MZTabFileParser{


    private List databases;
    private Map<Integer, Param> protocol;

    public MzTabUnmarshallerAdaptor(File tabFile, OutputStream out) throws IOException {
        super(tabFile, out);
    }

    public Map<Integer,MsRun> getSourceFiles(){
        return this.getMZTabFile().getMetadata().getMsRunMap();
    }

    public Map<Integer, Contact> getContacts() {
        return getMZTabFile().getMetadata().getContactMap();
    }

    public Map<Integer, Sample> getSamples() {
        return getMZTabFile().getMetadata().getSampleMap();
    }

    public Map<Integer, Software> getDataSoftwares() {
        return getMZTabFile().getMetadata().getSoftwareMap();
    }

    /**
     * Retrieve all publications from mzTab file
     * @return a Map of Reference from mzTab File
     */
    public Map<Integer, Publication> getReferences() {
        return getMZTabFile().getMetadata().getPublicationMap();
    }

    /**
     * Retrieve a general description from the file
     * @return Retrieve the mzTab CvParams
     */
    public List<Param> getAdditionalParams() {
        return getMZTabFile().getMetadata().getCustomList();
    }

    public String getExpTitle() {
        return getMZTabFile().getMetadata().getTitle();
    }

    public String getExpAccession() {
        return getMZTabFile().getMetadata().getMZTabID();
    }

    public String getVersion() {
        return getMZTabFile().getMetadata().getMZTabVersion();
    }

    public int getNumberOfPeptides(int rank) {
        return 0;
    }

    public int getNumberOfPeptides() {
        return 0;
    }

    public Protein getIdentById(String s) {
        for(Protein protein: getMZTabFile().getProteins())
          if(protein.getAccession().equalsIgnoreCase(s))
            return protein;
        return null;
    }

    public void getPeptide(String s, int i) {

    }

    public boolean hasProteinSequence() {
        return getMZTabFile().getProteinColumnFactory().isOptionalColumn(MzTabUtils.OPTIONAL_SEQUENCE_COLUMN);
    }

    public Collection<Comparable> getSpectrumIds() {
        List<Comparable> spectrumIds = new ArrayList<Comparable>();
        Iterator<PSM> psmIterator = getMZTabFile().getPSMs().iterator();
        return spectrumIds;
    }

    public Collection<Comparable> PSMIds() {
        List<Comparable> psmIds = new ArrayList<Comparable>();
        Iterator<PSM> psmIterator = getMZTabFile().getPSMs().iterator();
        while(psmIterator.hasNext()){
            PSM psm = psmIterator.next();
            psmIds.add(psm.getPSM_ID());
        }
        return psmIds;
    }

    public Map<Integer, MsRun> getMRunMap() {
        return getMZTabFile().getMetadata().getMsRunMap();
    }


    public Map<Integer, List<String>> getPSMtoMsRunMap() {
        Map<Integer, List<String>> psmIds = new HashMap<Integer, List<String>>();
        Iterator<PSM> psmIterator = getMZTabFile().getPSMs().iterator();
        while(psmIterator.hasNext()){
            PSM psm = psmIterator.next();
            SplitList<SpectraRef> refs = psm.getSpectraRef();
            for(SpectraRef ref:refs){
                MsRun msRun = ref.getMsRun();
                if(psmIds.containsKey(msRun.getId()))
                    psmIds.get(msRun.getId()).add(psm.getPSM_ID());
                else{
                    List<String> psmIDs = new ArrayList<String>();
                    psmIDs.add(psm.getPSM_ID());
                    psmIds.put(msRun.getId(), psmIDs);
                }
            }
        }
        return psmIds;
    }

    public Collection<PSM> getPSMs() {
        return getMZTabFile().getPSMs();
    }

    public boolean hasProteinGroup() {
        Collection<Protein> proteins = getMZTabFile().getProteins();
        for(Protein protein: proteins)
            if(protein.getAmbiguityMembers() != null)
                return true;
        return false;
    }

    public List<String> getProteinGroupIds() {
        List<String> accessions = new ArrayList<String>();
        Collection<Protein> proteins = getMZTabFile().getProteins();
        for(Protein protein: proteins)
          accessions.add(protein.getAccession());
        return accessions;
    }

    public Collection<String> getAllProteinAccessions() {
        Set<String> proteinIds = new HashSet<String>();
        for(PSM psm: getMZTabFile().getPSMs())
            proteinIds.add(psm.getAccession());
        return proteinIds;
    }

    public Map<Integer, Instrument> getInstrument() {
        return getMZTabFile().getMetadata().getInstrumentMap();
    }

    public Set<String[]> getDatabases() {
        Set<String[]> databases = new HashSet<String[]>();
        for(PSM psm: getMZTabFile().getPSMs()){
            if(psm.getDatabase() != null){
                String[] database = new String[2];
                database[0] = psm.getDatabase();
                database[1] = psm.getDatabaseVersion();
                databases.add(database);
            }
        }
        return databases;
    }

    public SortedMap<Integer, SplitList<Param>> getProtocol() {
        return getMZTabFile().getMetadata().getSampleProcessingMap();
    }
}
