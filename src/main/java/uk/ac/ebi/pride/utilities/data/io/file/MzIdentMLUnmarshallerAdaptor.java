package uk.ac.ebi.pride.utilities.data.io.file;


import psidev.psi.tools.xxindex.index.IndexElement;
import uk.ac.ebi.jmzidml.MzIdentMLElement;
import uk.ac.ebi.jmzidml.model.mzidml.*;
import uk.ac.ebi.jmzidml.model.mzidml.DBSequence;
import uk.ac.ebi.jmzidml.model.mzidml.Organization;
import uk.ac.ebi.jmzidml.model.mzidml.PeptideEvidence;
import uk.ac.ebi.jmzidml.model.mzidml.Person;
import uk.ac.ebi.jmzidml.model.mzidml.Provider;
import uk.ac.ebi.jmzidml.model.mzidml.Sample;
import uk.ac.ebi.jmzidml.model.mzidml.SourceFile;
import uk.ac.ebi.jmzidml.model.mzidml.SpectraData;
import uk.ac.ebi.jmzidml.model.mzidml.SpectrumIdentificationProtocol;
import uk.ac.ebi.jmzidml.xml.io.MzIdentMLUnmarshaller;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;

import javax.naming.ConfigurationException;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.util.*;


/**
 * MzMLIdentMLUnmarshallerAdaptor provides a list of convenient
 * methods to access mzidentML files.
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 * Date: 23/09/11
 * Time: 15:28
 */
public class MzIdentMLUnmarshallerAdaptor extends MzIdentMLUnmarshaller {

    private static final int LOOP_THRESHOLD = 20;

    private Map<String, Map<String, List<IndexElement>>> scannedIdMappings;

    private Inputs inputs = null;

    private AuditCollection auditCollection = null;

    private boolean avoidProteinInference = false;


    public MzIdentMLUnmarshallerAdaptor(File mzIdentMLFile, boolean inMemory) throws ConfigurationException {
        super(mzIdentMLFile, inMemory);
        scanIdMappings();

    }

    public MzIdentMLUnmarshallerAdaptor(File mzIdentMLFile, boolean inMemory, boolean avoidProteinInference) throws ConfigurationException{
       super(mzIdentMLFile, inMemory);
       this.avoidProteinInference = avoidProteinInference;
       scanIdMappings();
    }

    private void scanIdMappings() throws ConfigurationException {

        scannedIdMappings = new HashMap<String, Map<String, List<IndexElement>>>();

        // get id to index element mappings of SpectrumIdentificationResult
        Map<String, IndexElement> spectrumIdentResultIdToIndexElements = this.index.getIndexElements(SpectrumIdentificationResult.class);

        // get id to index element mappings of SpectrumIdentificationItem
        Map<String, IndexElement> spectrumIdentItemIdToIndexElements = this.index.getIndexElements(SpectrumIdentificationItem.class);

        // get index elements of PeptideEvidenceRef
        List<IndexElement> peptideEvidenceRefIndexElements = this.index.getIndexElements(MzIdentMLElement.PeptideEvidenceRef.getXpath());

        boolean proteinGroupPresent = hasProteinGroup();
        proteinGroupPresent = (!avoidProteinInference) && proteinGroupPresent;

        scanForIdMappings(spectrumIdentResultIdToIndexElements, spectrumIdentItemIdToIndexElements, peptideEvidenceRefIndexElements, proteinGroupPresent);

    }

    private void scanForIdMappings(Map<String, IndexElement> spectrumIdentResultIdToIndexElements,
                                   Map<String, IndexElement> spectrumIdentItemIdToIndexElements,
                                   List<IndexElement> peptideEvidenceRefIndexElements,
                                   boolean proteinGroupPresent) {


        for (String spectrumIdentResultId : spectrumIdentResultIdToIndexElements.keySet()) {
            IndexElement spectrumIdentResultIndexElement = spectrumIdentResultIdToIndexElements.get(spectrumIdentResultId);

            Iterator<Map.Entry<String, IndexElement>> spectrumIdentItemElementEntryIterator = spectrumIdentItemIdToIndexElements.entrySet().iterator();
            while (spectrumIdentItemElementEntryIterator.hasNext()) {
                Map.Entry<String, IndexElement> spectrumIdentItemElementEntry = spectrumIdentItemElementEntryIterator.next();
                String spectrumIdentItemId = spectrumIdentItemElementEntry.getKey();
                IndexElement spectrumIdentItemIndexElement = spectrumIdentItemElementEntry.getValue();
                if (isParentIndexElement(spectrumIdentResultIndexElement, spectrumIdentItemIndexElement)) {
                    Map<String, List<IndexElement>> spectrumIdentItemWithin = scannedIdMappings.get(spectrumIdentResultId);
                    if (spectrumIdentItemWithin == null) {
                        spectrumIdentItemWithin = new HashMap<String, List<IndexElement>>();
                        scannedIdMappings.put(spectrumIdentResultId, spectrumIdentItemWithin);
                    }

                    if (proteinGroupPresent) {
                        spectrumIdentItemWithin.put(spectrumIdentItemId, null);
                    } else {
                        spectrumIdentItemWithin.put(spectrumIdentItemId, findPeptideEvidenceRefIndexElements(spectrumIdentItemIndexElement, peptideEvidenceRefIndexElements));
                    }

                    spectrumIdentItemElementEntryIterator.remove();
                }
            }
        }
    }

    private List<IndexElement> findPeptideEvidenceRefIndexElements(IndexElement spectrumIdentItemIndexElement, List<IndexElement> peptideEvidenceRefIndexElements) {
        List<IndexElement> peptideEvidenceRefIndexElementsFound = new ArrayList<IndexElement>();

        Iterator<IndexElement> peptideEvidenceRefIndexElementIterator = peptideEvidenceRefIndexElements.iterator();
        while (peptideEvidenceRefIndexElementIterator.hasNext()) {
            IndexElement peptideEvidenceRefIndexElement = peptideEvidenceRefIndexElementIterator.next();
            if (isParentIndexElement(spectrumIdentItemIndexElement, peptideEvidenceRefIndexElement)) {
                peptideEvidenceRefIndexElementsFound.add(peptideEvidenceRefIndexElement);
                peptideEvidenceRefIndexElementIterator.remove();
            }
        }

        return peptideEvidenceRefIndexElementsFound;
    }

    private boolean isParentIndexElement(IndexElement parent, IndexElement child) {
        return parent.getStart() <= child.getStart() && parent.getStop() >= child.getStop();
    }

    public List<Sample> getSampleList() {
        uk.ac.ebi.jmzidml.model.mzidml.AnalysisSampleCollection asc =
                this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.AnalysisSampleCollection.class);
        return (asc != null) ? asc.getSample() : null;
    }

    public List<SourceFile> getSourceFiles() {
        if(inputs == null) inputs = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Inputs.class);
        return inputs.getSourceFile();
    }

    public List<AnalysisSoftware> getSoftwares() {
        uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftwareList asl =
                this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.AnalysisSoftwareList.class);

        return (asl != null) ? asl.getAnalysisSoftware() : null;
    }

    public List<Person> getPersonContacts() {
        if(auditCollection == null) auditCollection = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.AuditCollection.class);
        return (auditCollection != null) ? auditCollection.getPerson() : null;
    }

    public List<Organization> getOrganizationContacts() {
        if(auditCollection == null) auditCollection = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.AuditCollection.class);
        return (auditCollection != null) ? auditCollection.getOrganization() : null;
    }

    public Iterator<BibliographicReference> getReferences() {
        return this.unmarshalCollectionFromXpath(uk.ac.ebi.jmzidml.MzIdentMLElement.BibliographicReference);
    }

    public ProteinDetectionHypothesis getIdentificationById(Comparable identId) throws JAXBException {
        return this.unmarshal(ProteinDetectionHypothesis.class, (String) identId);
    }

    public int getNumIdentifiedPeptides() throws ConfigurationException {
        List<IndexElement> spectrumIdentItemRefs = this.index.getIndexElements(MzIdentMLElement.SpectrumIdentificationItemRef.getXpath());

        if (spectrumIdentItemRefs == null || spectrumIdentItemRefs.isEmpty()) {
            return this.getIDsForElement(MzIdentMLElement.SpectrumIdentificationItem).size();
        } else {
            return spectrumIdentItemRefs.size();
        }
    }

    public FragmentationTable getFragmentationTable() {
        return this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.FragmentationTable.class);
    }

    public List<Cv> getCvList() {
        return (this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.CvList.class)).getCv();
    }

    public String getMzIdentMLName() {
        Map<String, String> properties = this.getElementAttributes(this.getMzIdentMLId(),
                uk.ac.ebi.jmzidml.model.mzidml.MzIdentML.class);

        /*
         * This is the only way that we can use now to retrieve the name property
         * In the future we need to think in more elaborated way.
         */
        return (properties.containsKey("name")) ? properties.get("name") : "no assay title provided (mzIdentML)";
    }

    public Date getCreationDate() {
        Map<String, String> properties = this.getElementAttributes(this.getMzIdentMLId(),
                uk.ac.ebi.jmzidml.model.mzidml.MzIdentML.class);

        /*
         * This is the only way that we can use now to retrieve the name property
         * In the future we need to think in more elaborated way.
         */
        Date dateCreation = null;
        if (properties.containsKey("creationDate")) {
            Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(properties.get("creationDate"));
            dateCreation = calendar.getTime();
        }
        return dateCreation;

    }

    public Provider getProvider() {
        return this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Provider.class);
    }

    public List<SpectrumIdentificationProtocol> getSpectrumIdentificationProtocol() {
        AnalysisProtocolCollection apc = this.unmarshal(AnalysisProtocolCollection.class);
        return (apc != null) ? apc.getSpectrumIdentificationProtocol() : null;
    }

    public ProteinDetectionProtocol getProteinDetectionProtocol() {
        return this.unmarshal(ProteinDetectionProtocol.class);
    }

    public List<SearchDatabase> getSearchDatabases() {
        if(inputs == null) inputs = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Inputs.class);
        return inputs.getSearchDatabase();
    }

    public List<SpectraData> getSpectraData() {
        if(inputs == null) inputs = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Inputs.class);
        return inputs.getSpectraData();
    }

    public Map<Comparable, SpectraData> getSpectraDataMap() {
        if(inputs == null) inputs = this.unmarshal(uk.ac.ebi.jmzidml.model.mzidml.Inputs.class);
        List<SpectraData> spectraDataList = inputs.getSpectraData();
        Map<Comparable, SpectraData> spectraDataMap = null;
        if (spectraDataList != null && spectraDataList.size() > 0) {
            spectraDataMap = new HashMap<Comparable, SpectraData>();
            for (SpectraData spectraData : spectraDataList) {
                spectraDataMap.put(spectraData.getId(), spectraData);
            }
        }
        return spectraDataMap;
    }

    public ProteinAmbiguityGroup getProteinAmbiguityGroup(Comparable id) throws JAXBException {
        return this.unmarshal(ProteinAmbiguityGroup.class, (String) id);
    }

    public DBSequence getDBSequenceById(Comparable id) throws JAXBException {
        return this.unmarshal(DBSequence.class, (String) id);
    }

    public boolean hasProteinGroup() throws ConfigurationException {
        Set<String> proteinAmbiguityGroupIds = this.getIDsForElement(MzIdentMLElement.ProteinAmbiguityGroup);

        return proteinAmbiguityGroupIds != null && !proteinAmbiguityGroupIds.isEmpty() && !avoidProteinInference;
    }

    public List<SpectrumIdentificationItem> getSpectrumIdentificationsByIds(List<Comparable> spectrumIdentIds) throws JAXBException {
        List<SpectrumIdentificationItem> spectrumIdentifications = null;
        if (spectrumIdentIds != null && spectrumIdentIds.size() > 0) {
            spectrumIdentifications = new ArrayList<SpectrumIdentificationItem>();
            for (Comparable id : spectrumIdentIds) {
                SpectrumIdentificationItem spectrumIdentification = this.unmarshal(SpectrumIdentificationItem.class, (String) id);
                spectrumIdentifications.add(spectrumIdentification);
            }

        }
        return spectrumIdentifications;
    }

    public Set<String> getSpectrumIdentificationItemIds(String spectrumIdentResultId) throws JAXBException {
        Map<String, List<IndexElement>> elementsWithSpectrumIdentResult = scannedIdMappings.get(spectrumIdentResultId);

        if (elementsWithSpectrumIdentResult != null) {
            return new LinkedHashSet<String>(elementsWithSpectrumIdentResult.keySet());
        } else {
            return Collections.emptySet();
        }
    }

    public Set<String> getPeptideEvidenceReferences(String spectrumIdentResultId, String spectrumIdentItemId) {
        Map<String, List<IndexElement>> elementsWithSpectrumIdentResult = scannedIdMappings.get(spectrumIdentResultId);

        if (elementsWithSpectrumIdentResult != null) {
            List<IndexElement> peptideEvidenceRefIndexElements = elementsWithSpectrumIdentResult.get(spectrumIdentItemId);
            if (peptideEvidenceRefIndexElements != null) {
                Set<String> peptideEvidenceRefs = new LinkedHashSet<String>();

                for (IndexElement peptideEvidenceRefIndexElement : peptideEvidenceRefIndexElements) {
                    Map<String, String> peptideEvidenceRefAttributes = this.getElementAttributes(this.index.getXmlString(peptideEvidenceRefIndexElement));
                    if (peptideEvidenceRefAttributes.containsKey("peptideEvidence_ref")) {
                        peptideEvidenceRefs.add(peptideEvidenceRefAttributes.get("peptideEvidence_ref"));
                    }
                }

                return peptideEvidenceRefs;
            } else {
                return Collections.emptySet();
            }
        } else {
            return Collections.emptySet();
        }
    }

    public boolean hasProteinSequence() throws ConfigurationException {
        Set<String> proteinSequence = this.getIDsForElement(MzIdentMLElement.DBSequence);
        if (proteinSequence != null && !proteinSequence.isEmpty()) {
            Map<String, String> attributes = this.getElementAttributes((String) proteinSequence.toArray()[0], DBSequence.class);
            return attributes.containsKey("Seq");
        }
        return false;
    }

    public boolean hasDecoyInformation() throws ConfigurationException {
        Set<String> proteinSequence = this.getIDsForElement(MzIdentMLElement.PeptideEvidence);
        if (proteinSequence != null && !proteinSequence.isEmpty()) {
            int i = 0;
            while(i < LOOP_THRESHOLD && i < proteinSequence.size()){
                Map<String, String> attributes = this.getElementAttributes((String) proteinSequence.toArray()[i], PeptideEvidence.class);
                if(attributes.containsKey("isDecoy"))
                   return true;
                i++;
            }
        }
        return false;
    }

    /**
     * Check for all the SpectraData if they are referenced by title instead of using the normal index.
     * @param spectraDataMap Map of all the SpectraData
     * @return java.lang.List<Comparable> A list of Identifier from the Mapper
     * @throws JAXBException
     */
    public List<Comparable> getTitleReferenceFile(Map<Comparable, SpectraData> spectraDataMap) throws JAXBException {
        List<Comparable> collection = new ArrayList<Comparable>();
        for(SpectraData spectraData: spectraDataMap.values()){
            if(MzIdentMLUtils.isSpectraDataReferencedByTitle(spectraData)){
                collection.add(spectraData.getId());
            }
        }
        return collection;
    }

    public Comparable getMGFTitleReference(String spectrumIdentResultId) throws JAXBException {
        SpectrumIdentificationResult result = this.unmarshal(SpectrumIdentificationResult.class, spectrumIdentResultId);
        if(result != null)
            return MzIdentMLUtils.MGFTitleCVtermValue(result.getCvParam());

        return null;
    }

    public SpectrumIdentificationItem getSpectrumIdentificationsById(String ref) throws JAXBException {
        if(ref != null){
            return  this.unmarshal(SpectrumIdentificationItem.class, ref);
        }
        return null;
    }

    public PeptideEvidence getPeptideEvidenceById(String peptideEvidenceRef) throws JAXBException {
        if(peptideEvidenceRef != null){
            return this.unmarshal(PeptideEvidence.class, peptideEvidenceRef);
        }
        return null;
    }
}



