package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;



import uk.ac.ebi.pride.jmztab.model.Peptide;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Reference;
import uk.ac.ebi.pride.utilities.data.core.Sample;
import uk.ac.ebi.pride.utilities.data.core.Software;
import uk.ac.ebi.pride.utilities.data.core.SourceFile;
import uk.ac.ebi.pride.utilities.data.core.UserParam;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.model.Contact;
import uk.ac.ebi.pride.jmztab.model.Instrument;
import uk.ac.ebi.pride.jmztab.model.Param;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.*;

/**
 * MzTab Transformer to ms-data-core-api Objects
 * @author ypriverol
 * @author rwang
 */
public class MzTabTransformer {

    private final static String PROTOCOL_ID = "protocol1";


    public static List<SourceFile> transformSourceFiles(Map<Integer, uk.ac.ebi.pride.jmztab.model.MsRun> msRunMap) {
        List<SourceFile> sourceFiles = new ArrayList<SourceFile>();
        if(msRunMap != null && msRunMap.size() > 0){
            for(Map.Entry entry: msRunMap.entrySet())
                sourceFiles.add(transformSourceFile((MsRun)entry.getValue()));
        }
        return sourceFiles;
    }

    /**
     * Conver an msRun to SourceFile
     * @param msRun msRun
     * @return     SourceFile in in ms-data-core api
     */
    public static SourceFile transformSourceFile(MsRun msRun){
        SourceFile sourceFile = null;
        if(msRun != null){
            ParamGroup paramGroup = new ParamGroup();
            //Adding fragmetation type as a CVParameter
            if(msRun.getFragmentationMethod() != null){
                CvParam fragmentationType = MzTabUtils.convertCVParamToCvParam(msRun.getFragmentationMethod());
                paramGroup.addCvParam(fragmentationType);
            }
            //Adding hash method to CvParam
            if(msRun.getHashMethod() != null){
                CvParam hashType = MzTabUtils.convertCVParamToCvParam(msRun.getHashMethod());
                hashType.setValue(msRun.getHash());
                paramGroup.addCvParam(hashType);
            }
            // Add CvFormat
            CvParam format = null;
            if(msRun.getFormat() != null)
                format = MzTabUtils.convertCVParamToCvParam(msRun.getFormat());

            sourceFile = new SourceFile(paramGroup,msRun.getId().toString(),msRun.getReference(),msRun.getLocation().getPath(),format,null);

        }
        return sourceFile;
    }

    public static Collection<Person> transformContactToPersons(Map<Integer, uk.ac.ebi.pride.jmztab.model.Contact> contacts) {
        List<Person> persons = new ArrayList<Person>();
        if(contacts != null & (contacts != null ? contacts.size() : 0) > 0){
            for(Map.Entry contact: contacts.entrySet()){
                persons.add(transformContactToPerson((uk.ac.ebi.pride.jmztab.model.Contact)contact.getValue()));
            }
        }
        return persons;
    }

    public static Person transformContactToPerson(Contact contact){
        Person person = null;
        if(contact != null){
            CvTermReference contactTerm = CvTermReference.CONTACT_NAME;
            List<CvParam> cvParams = DataAccessUtilities.getCvParam(contactTerm.getName(), contactTerm.getCvLabel(), contactTerm.getAccession(), contact.getName());
            CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
            cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), contact.getAffiliation(), null, null, null));
            CvTermReference contactMail = CvTermReference.CONTACT_EMAIL;
            cvParams.add(new CvParam(contactMail.getAccession(), contactMail.getName(), contactMail.getCvLabel(), contact.getEmail(), null, null, null));
            List<UserParam> userParams = null;
            List<Organization> affiliation = new ArrayList<Organization>();
            affiliation.add(new Organization(null, contact.getAffiliation(), null, null));
            ParamGroup paramGroup = new ParamGroup(cvParams, userParams);
            person = new Person(paramGroup, contact.getName(), contact.getName(), null, null, null, affiliation, contact.getEmail());
        }
        return person;
    }

    public static List<Organization> transformContactToOrganization(Map<Integer, uk.ac.ebi.pride.jmztab.model.Contact> contacts){
        List<Organization> organizations = new ArrayList<Organization>();
        if(contacts != null & (contacts != null ? contacts.size() : 0) > 0){
            for(Map.Entry contact: contacts.entrySet()){
                organizations.add(transformContactToOrganization((uk.ac.ebi.pride.jmztab.model.Contact) contact.getValue()));
            }
        }
        return organizations;
    }

    public static Organization transformContactToOrganization(Contact contact) {
        Organization organization = null;
        if (contact != null) {
                CvTermReference contactOrg = CvTermReference.CONTACT_ORG;
                List<CvParam> cvParams = new ArrayList<CvParam>();
                cvParams.add(new CvParam(contactOrg.getAccession(), contactOrg.getName(), contactOrg.getCvLabel(), contact.getAffiliation(), null, null, null));
                List<UserParam> userParams = null;
                ParamGroup paramGroup = new ParamGroup(cvParams, userParams);
                organization = new Organization(paramGroup, null, contact.getAffiliation(), null, null);
        }
        return organization;
    }

    public static List<Sample> transformSamples(Map<Integer, uk.ac.ebi.pride.jmztab.model.Sample> oldSamples) {
        List<Sample> samples = new ArrayList<Sample>();
        if(oldSamples != null && oldSamples.size()>0){
            for(Map.Entry entry: oldSamples.entrySet())
                samples.add(transformSample((uk.ac.ebi.pride.jmztab.model.Sample)entry.getValue()));
        }
        return samples;
    }

    private static Sample transformSample(uk.ac.ebi.pride.jmztab.model.Sample oldSample) {
        if(oldSample != null){
            ParamGroup paramGroup = new ParamGroup();
            if(oldSample.getCellTypeList() != null && oldSample.getCellTypeList().size() > 0){
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getCellTypeList()));
            }
            if(oldSample.getDiseaseList() != null && oldSample.getDiseaseList().size() > 0){
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getDiseaseList()));
            }
            if(oldSample.getSpeciesList() != null && oldSample.getSpeciesList().size() > 0){
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getSpeciesList()));
            }
            if(oldSample.getTissueList() != null && oldSample.getTissueList().size() > 0){
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getTissueList()));
            }

            if(oldSample.getCustomList() != null && oldSample.getCustomList().size() > 0){
                paramGroup.addCvParams(MzTabUtils.convertParamToCvParam(oldSample.getCustomList()));
            }
            return  new Sample(paramGroup,oldSample.getId().toString(),oldSample.getDescription());
        }
        return null;
    }

    /**
     * Transform a List of softwares
     * @param dataSoftwares mzTab softwares
     * @return  List of Softwares
     */
    public static List<Software> transformSoftwares(Map<Integer, uk.ac.ebi.pride.jmztab.model.Software> dataSoftwares) {
        List<Software> softwares = new ArrayList<Software>();
        if(dataSoftwares != null && dataSoftwares.size() >0){
            for(Map.Entry entry: dataSoftwares.entrySet()){
                softwares.add(transformSoftware((uk.ac.ebi.pride.jmztab.model.Software)entry.getValue()));
            }
        }
        return softwares;
    }

    /**
     * Transform a Software Object
     * @param oldSoftware The mzTab Software
     * @return Software
     */
    public static Software transformSoftware(uk.ac.ebi.pride.jmztab.model.Software oldSoftware){
        if(oldSoftware != null){
            ParamGroup paramGroup = new ParamGroup();
            String version = null;
            String name = null;
            if(oldSoftware.getParam() != null) {
                paramGroup.addCvParam(MzTabUtils.convertCVParamToCvParam(oldSoftware.getParam()));
                version = oldSoftware.getParam().getValue();
                name = oldSoftware.getParam().getName();
            }
            if(oldSoftware.getSettingList() != null && oldSoftware.getSettingList().size()>0){
                paramGroup.addUserParams(MzTabUtils.convertStringListToUserParam(oldSoftware.getSettingList()));

            }
            return new Software(paramGroup,oldSoftware.getId().toString(),name,null,null,null,version);
        }
        return null;
    }

    public static Collection<Reference> transformReferences(Map<Integer, uk.ac.ebi.pride.jmztab.model.Publication> publications) {
        Collection<Reference> references = new ArrayList<Reference>();
        if(publications != null && publications.size() > 0){
            for(Map.Entry entry: publications.entrySet()){
                Publication publication = (Publication) entry.getValue();
                Reference newReference = null;
                if (publication != null) {
                    ParamGroup paramGroup = new ParamGroup();
                    CvTermReference term = CvTermReference.MS_PUBLICATION_DOI;
                    paramGroup.addCvParam(new CvParam(term.getAccession(),term.getName(), term.getCvLabel(),publication.toString(),null,null,null));
                    newReference = new Reference(paramGroup, publication.toString());
                }
                references.add(newReference);
            }
        }
        return references;
    }

    public static ParamGroup transformAdditional(List<Param> additionalParams) {
        if(additionalParams != null && additionalParams.size() >0){
            ParamGroup paramGroup = new ParamGroup();
            for(Param param: additionalParams)
                paramGroup.addCvParam(MzTabUtils.convertCVParamToCvParam(param));
            return paramGroup;
        }
        return null;
    }

    /**
     * Convert protein identification
     *
     * @param identification pride xml protein identification
     * @return Identification  protein identification
     */
    public static Protein transformIdentification(uk.ac.ebi.pride.jmztab.model.Protein identification) {
        //Todo: Check how in mztab gel information is annotated
        return transformGelFreeIdent(identification);
    }

    /**
     * Convert two dimensional identification
     * <p/>
     * ToDo: there are code duplication between transformTwoDimIdent and transformGelFreeIdent
     *
     * @param rawIdent pride xml two dimensional identification
     * @return TwoDimIdentification    two dimentional identification
     */
    public static Protein transformTwoDimIdent(uk.ac.ebi.pride.jaxb.model.TwoDimensionalIdentification rawIdent) {

        /**Protein ident = null;

        if (rawIdent != null) {
            // peptides

            SearchDataBase searchDataBase = new SearchDataBase(rawIdent.getDatabase(), rawIdent.getDatabaseVersion());
            DBSequence dbSequence = new DBSequence(rawIdent.getAccession(), searchDataBase, rawIdent.getAccessionVersion(), rawIdent.getSpliceIsoform());
            dbSequence.setId(rawIdent.getAccession());

            List<uk.ac.ebi.pride.jaxb.model.PeptideItem> rawPeptides = rawIdent.getPeptideItem();
            List<Peptide> peptides = null;
            int peptideIndex = 0;
            if (rawPeptides != null) {
                peptides = new ArrayList<Peptide>();
                for (uk.ac.ebi.pride.jaxb.model.PeptideItem rawPeptide : rawPeptides) {
                   // peptides.add(transformPeptide(rawPeptide, dbSequence, peptideIndex));
                    peptideIndex++;
                }
            }

            // params
            ParamGroup params = transformParamGroup(rawIdent.getAdditional());

            // gel
            uk.ac.ebi.pride.jaxb.model.SimpleGel rawGel = rawIdent.getGel();
            Gel gel = null;

            if (rawGel != null) {
                gel = transformGel(rawGel, rawIdent.getGelLocation(), rawIdent.getMolecularWeight(), rawIdent.getPI());
            }

            Double seqConverage = rawIdent.getSequenceCoverage();
            double seqConverageVal = seqConverage == null ? -1 : seqConverage;

            Double threshold = rawIdent.getThreshold();
            double thresholdVal = threshold == null ? -1 : threshold;


            Score score = DataAccessUtilities.getScore(params);

            //Todo: We need to define the best way to retrieve the SearchEngine value for PRIDE XML
            if(score == null)
                score = new Score();
            Number scoreValue = (rawIdent.getScore() != null)? rawIdent.getScore(): null;
            score.addScore(SearchEngineType.getByName(rawIdent.getSearchEngine()),CvTermReference.MS_SEARCH_ENGINE_SPECIFIC_SCORE,scoreValue);

            ident = new Protein(params, rawIdent.getId(), null, dbSequence, false, peptides, score, thresholdVal, seqConverageVal, gel);
        } **/

       // return ident;
        return null;
    }

    /**
     * Convert gel free protein identification
     *
     * @param rawIdent pride xml protein identification
     * @return GelFreeIdentification   gel free identification
     */
    public static Protein transformGelFreeIdent(uk.ac.ebi.pride.jmztab.model.Protein rawIdent) {
        Protein ident = null;

        /**if (rawIdent != null) {
            // peptides
            List<Peptide> rawPeptides = rawIdent.getPeptideItem();
            CvParam cvParam = CvUtilities.getCVTermFromCvReference(CvTermReference.MS_DATABASE, rawIdent.getDatabase());
            ParamGroup paramGroup = new ParamGroup(cvParam, null);
            SearchDataBase searchDataBase = new SearchDataBase(rawIdent.getDatabase(), rawIdent.getDatabaseVersion(), paramGroup);
            DBSequence dbSequence = new DBSequence(rawIdent.getAccession(), searchDataBase, rawIdent.getAccessionVersion(), rawIdent.getSpliceIsoform());
            dbSequence.setId(rawIdent.getAccession());

            List<Peptide> peptides = null;
            int peptideIndex = 0;
            if (rawPeptides != null) {
                peptides = new ArrayList<Peptide>();
                for (uk.ac.ebi.pride.jaxb.model.PeptideItem rawPeptide : rawPeptides) {
                    peptides.add(transformPeptide(rawPeptide, dbSequence, peptideIndex));
                    peptideIndex++;
                }
            }

            // params
            ParamGroup params = transformParamGroup(rawIdent.getAdditional());

            Double seqConverage = rawIdent.getSequenceCoverage();
            double seqConverageVal = seqConverage == null ? -1 : seqConverage;
            Double threshold = rawIdent.getThreshold();
            double thresholdVal = threshold == null ? -1 : threshold;
            Score score = DataAccessUtilities.getScore(params);

            //Todo: We need to define the best way to retrieve the SearchEngine value for PRIDE XML
            if(score == null)
                score = new Score();
            Number scoreValue = (rawIdent.getScore() != null)? rawIdent.getScore(): null;
            score.addScore(SearchEngineType.getByName(rawIdent.getSearchEngine()),CvTermReference.MS_SEARCH_ENGINE_SPECIFIC_SCORE,scoreValue);

            return new Protein(params, rawIdent.getId(), null, dbSequence, false, peptides, score, thresholdVal, seqConverageVal, null);

        }**/

        return ident;
    }

    /**
     * Transform gel from pride xml to core data model.
     *
     * @param rawGel      gel in pride xml format.
     * @param gelLocation gel location in pride xml format.
     * @param mw          molecular weight in pride xml.
     * @param pI          pI in pride xml.
     * @return Gel  gel in core data model.
     */
    private static Gel transformGel(uk.ac.ebi.pride.jaxb.model.SimpleGel rawGel,
                                    uk.ac.ebi.pride.jaxb.model.GelLocation gelLocation,
                                    Double mw, Double pI) {
       /**
        String gelLink = null;
        ParamGroup params = null;

        if (rawGel != null) {
            gelLink = rawGel.getGelLink();
            params = transformParamGroup(rawGel.getAdditional());
        }
        double xCoordinate = -1;
        double yCoordinate = -1;

        if (gelLocation != null) {
            xCoordinate = gelLocation.getXCoordinate();
            yCoordinate = gelLocation.getYCoordinate();
        }

        double molWeight = mw == null ? -1 : mw;
        double pi = pI == null ? -1 : pI;

        return new Gel(params, gelLink, xCoordinate, yCoordinate, molWeight, pi);
        **/
        return null;
    }

    public static ExperimentProtocol transformProtocol(SortedMap<Integer, SplitList<Param>> sampleProcession) {
        ExperimentProtocol protocol = null;
        if (sampleProcession != null) {
            List<ParamGroup> protocolSteps = new ArrayList<ParamGroup>();
            for(Map.Entry entry: sampleProcession.entrySet()){
                ParamGroup paramGroup = new ParamGroup();
                SplitList<Param> params = (SplitList<Param>) entry.getValue();
                for(Param param: params)
                    paramGroup.addCvParam(MzTabUtils.convertCVParamToCvParam(param));
                protocolSteps.add(paramGroup);
            }
            protocol = new ExperimentProtocol(null,MzTabTransformer.PROTOCOL_ID , null, protocolSteps);
        }
        return protocol;

    }

    public static Map<Integer, SpectraData> transformMsRunMap(Map<Integer, MsRun> mRunMap) {
        Map<Integer, SpectraData> spectraDataMap = new HashMap<Integer, SpectraData>();
        for (Map.Entry entry : mRunMap.entrySet())
            spectraDataMap.put((Integer)entry.getKey(), transformMsRunToSpectraData((MsRun) entry.getValue()));
        return spectraDataMap;
    }

    private static SpectraData transformMsRunToSpectraData(MsRun msRun) {
        SpectraData spectraData;
        CvParam paramFormat = null;
        if(msRun.getFormat() != null)
            paramFormat = MzTabUtils.convertCVParamToCvParam(msRun.getFormat());

        CvParam idFormat = null;
        if(msRun.getIdFormat() != null)
            idFormat = MzTabUtils.convertCVParamToCvParam(msRun.getIdFormat());

        spectraData = new SpectraData(msRun.getId().toString(),msRun.getReference(),msRun.getLocation().getFile(),paramFormat,null, idFormat);
        return spectraData;

    }

    public static Collection<InstrumentConfiguration> transformInstrument(Map<Integer, Instrument> oldInstruments) {
        List<InstrumentConfiguration> instrumentConfigurations = null;

        for(Map.Entry entry: oldInstruments.entrySet()){
            Instrument instrument = (Instrument) entry.getValue();
            if (instrument != null) {
                instrumentConfigurations = new ArrayList<InstrumentConfiguration>();
            // create instrument param group to aid semantic support
            ParamGroup params = new ParamGroup();

            params.addCvParam(MzTabUtils.convertCVParamToCvParam(instrument.getName()));
            // create instrument components
            int sourceOrder = 1;

            //Source Instrument File Creation
            int detectorOrder = instrument.getAnalyzerList().size() + 2;
            List<InstrumentComponent> source = new ArrayList<InstrumentComponent>();
            InstrumentComponent sourceInstrument = transformSource(sourceOrder, instrument.getSource());
            source.add(sourceInstrument);

            List<InstrumentComponent> detector = new ArrayList<InstrumentComponent>();
            InstrumentComponent detectorInstrument = transformDetector(detectorOrder, instrument.getDetector());
            detector.add(detectorInstrument);

            List<Param> rawAnalyzers = instrument.getAnalyzerList();

            int orderCnt = 2;
            List<InstrumentComponent> analyzer = new ArrayList<InstrumentComponent>();
            for (Param rawAnalyzer : rawAnalyzers) {
                InstrumentComponent analyzerInstrument = transformAnalyzer(orderCnt, rawAnalyzer);
                analyzer.add(analyzerInstrument);
                orderCnt++;
            }
            instrumentConfigurations.add(new InstrumentConfiguration(instrument.getId().toString(), null, null, source, analyzer, detector, params));
            }
        }

        return instrumentConfigurations;
    }

    private static InstrumentComponent transformAnalyzer(int orderCnt, Param rawAnalyzer) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have mass analyzer type (MS:1000443)
        if (rawAnalyzer != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertCVParamToCvParam(rawAnalyzer));
            component = new InstrumentComponent(orderCnt, params);
        }
        return component;
    }

    private static InstrumentComponent transformDetector(int detectorOrder, Param detector) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have detector type (MS:1000026)
        if (detector != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertCVParamToCvParam(detector));
            component = new InstrumentComponent(detectorOrder, params);
        }

        return component;
    }

    private static InstrumentComponent transformSource(int sourceOrder, Param source) {
        InstrumentComponent component = null;
        // ToDo: add semantic support
        // ToDo: must have ionization type (MS:1000008)
        if (source != null) {
            ParamGroup params = new ParamGroup();
            params.addCvParam(MzTabUtils.convertCVParamToCvParam(source));
            component = new InstrumentComponent(sourceOrder, params);
        }

        return component;
    }

    public static List<SearchDataBase> transformDatabases(Set<String[]> oldDatabases) {
        List<SearchDataBase> databases = new ArrayList<SearchDataBase>();
        if(oldDatabases != null && oldDatabases.size() > 0){
            for(String[] databaseString: oldDatabases){
                String name = databaseString[0];
                String version = databaseString[1];
                databases.add(new SearchDataBase(name,version));
            }
        }
        return databases;
    }
}
