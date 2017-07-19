package uk.ac.ebi.pride.utilities.data.exporters;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.jmztab.utils.convert.ConvertProvider;
import uk.ac.ebi.pride.jmztab.utils.convert.SearchEngineScoreParam;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.CvParam;
import uk.ac.ebi.pride.utilities.data.core.Person;
import uk.ac.ebi.pride.utilities.data.core.Reference;
import uk.ac.ebi.pride.utilities.data.core.SpectrumIdentification;
import uk.ac.ebi.pride.utilities.data.utils.CvUtilities;
import uk.ac.ebi.pride.utilities.data.utils.MzTabUtils;
import uk.ac.ebi.pride.utilities.pridemod.ModReader;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static uk.ac.ebi.pride.jmztab.model.MZTabUtils.isEmpty;
import static uk.ac.ebi.pride.utilities.data.utils.MzTabUtils.removeNewLineAndTab;

/**
 * ms-data-core api will plan to export all the data to mztab files using the jmzTab library.
 * At the very beginning we will support only the Identification exporter, but in the future we should be
 * able to support Quantitation export.
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public abstract class AbstractMzTabConverter extends ConvertProvider<DataAccessController, Void> {

    protected static final Logger logger = LoggerFactory.getLogger(AbstractMzTabConverter.class);

    protected Metadata metadata;

    protected MZTabColumnFactory proteinColumnFactory;

    protected MZTabColumnFactory psmColumnFactory;

    protected Map<String, Integer> proteinScoreToScoreIndex;

    protected Map<String, Integer> psmScoreToScoreIndex;

    public static ModReader modReader;

    public AbstractMzTabConverter(DataAccessController controller) {
        super(controller, null);
        modReader = ModReader.getInstance();
    }

    @Override
    protected void init() {
        super.init();
        if(!AbstractMzTabConverter.isSupported(source))
            throw new DataAccessException("Error exporting a non Identification file!!!");
    }

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.Metadata}
     */
    @Override
    protected Metadata convertMetadata() {

        this.metadata = new Metadata();

        /**
         * It is a good practice to take the name of the file as the Ids because the writers never use proper IDs
         * in the mzidentml files.
         */
        String mzTabId = getFileNameWithoutExtension(source.getName());
        metadata.setMZTabID(mzTabId);

        /**
         *  Is really common that name of the mzidentml is not provided then we will use Ids if the name is not provided.
         */
        String title = source.getExperimentMetaData().getName() != null? source.getExperimentMetaData().getName():source.getExperimentMetaData().getId().toString();

        /**
         * We need to remove the tab and other special characters for lines because they break the mzTab validator.
         */

        metadata.setTitle(removeNewLineAndTab(title));

        //Get Software information from DataAccsessController
        loadSoftware();
        // process the references
        loadReferences();
        // process the contacts
        loadContacts();
        // process the experiment params
        loadExperimentParams();
        //process the sample processing information
        loadSampleProcessing();
        // process the instrument information
        loadInstrument();

        // process search engines. The information is in every identification, we assume that is going to be the same per
        // protein and psm (constant number of scores in all the proteins and constant number of score per psm)
        // They can not be added while processing proteins and psm identification because the initialization of the protein/psms
        // need to know in advance all the columns for the factory, they can not grow dynamically inside (the values are
        // not properly shifted)
        loadSearchEngineScores();

        // set Ms Run
        loadMsRun();

        // process samples
        loadSamples();

        // set mzTab- description
        if (isIdentification()) {
            metadata.setMZTabType(MZTabDescription.Type.Identification);
            metadata.setMZTabMode(MZTabDescription.Mode.Complete);
        } else {
            metadata.setMZTabType(MZTabDescription.Type.Quantification);
            metadata.setMZTabMode(MZTabDescription.Mode.Summary);
            logger.debug("Converting quantification file from PRIDE XML.");
        }

        //Load URI
        if(source.getExperimentMetaData()!=null && source.getExperimentMetaData().getId()!=null) {
            loadURI(source.getExperimentMetaData().getId().toString());
        }

        //The description should be added in loadExperiment()
        //TODO: Move to the right place, it is a default checking (ConverterProvider)
        if (metadata.getDescription() == null || metadata.getDescription().isEmpty()) {
            metadata.setDescription("Description not available");
        }

        //TODO for PRIDEXML
        loadGelData();

        metadata.addCustom(new uk.ac.ebi.pride.jmztab.model.UserParam("Date of export", new Date().toString()));
        metadata.addCustom(new uk.ac.ebi.pride.jmztab.model.UserParam("Original converted file", ((File)(source.getSource())).toURI().getPath()));

        return metadata;
    }

    /**
     * Load software information
     */
    protected abstract void loadSoftware();

    /**
     * Converts the experiment's references into a couple of {@link uk.ac.ebi.pride.jmztab.model.PublicationItem} (including DOIs and PubMed ids)
     */
    protected void loadReferences() {


        List<Reference> references = source.getExperimentMetaData().getReferences();
        int i = 1;
        for(Reference ref: references){
            List<PublicationItem> items = new ArrayList<PublicationItem>();

            //DOI
            String doi = ref.getDoi();
            if(doi != null && !doi.isEmpty()){
                items.add(new PublicationItem(PublicationItem.Type.DOI, removeNewLineAndTab(doi)));
            }
            //PubMed
            if(ref.getCvParams() != null){
                // check if there's a pubmed id
                String pubmed = getPublicationAccession(ref.getCvParams(), CvTermReference.PRIDE_REFERENCE_PUBMED.getName());
                if (!isEmpty(pubmed)) {
                    items.add(new PublicationItem(PublicationItem.Type.PUBMED, removeNewLineAndTab(pubmed)));
                }
            }
            if(!items.isEmpty()) {
                metadata.addPublicationItems(i, items);
                i++;
            }
        }
    }

    /**
     * Extract publication accession number from CVParam.
     */
    private String getPublicationAccession(List<CvParam> param, String name) {
        if (param == null || isEmpty(name)) {
            return null;
        }

        // this only makes sense if we have a list of params and an accession!
        for (CvParam p : param) {
            if (name.equalsIgnoreCase(p.getCvLookupID())) {   //At least in PRIDE XML the PubMed in the reference is in the CvLabel
                return p.getAccession();
            }
        }

        return null;
    }

    /**
     * Converts a list of Contacts into an ArrayList of mzTab Contacts.
     */
    protected void loadContacts() {
        List<Person> contactList = source.getExperimentMetaData().getPersons();
        // make sure there are contacts to be processed
        if (contactList == null || contactList.size() == 0) {
            return;
        }

        String regexp = "[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-']+)*@[A-Za-z0-9]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})";

        Pattern pattern = Pattern.compile(regexp);
        Matcher matcher;

        // initialize the return variable
        int id = 1;
        for (Person c : contactList) {
            String name = (c.getName()!=null)?c.getName():c.getId().toString();
            name = (c.getLastname() != null)? name + " " + c.getLastname():name;

            String affiliation = null;
            String originalContact = c.getContactInfo();

            if(!name.isEmpty()) {
                metadata.addContactName(id, removeNewLineAndTab(name));
            }
            if(c.getAffiliation() != null && !c.getAffiliation().isEmpty()) {
                if (c.getAffiliation().get(0) != null) {
                    if (c.getAffiliation().get(0).getName() != null)
                        affiliation = c.getAffiliation().get(0).getName();
                    else
                        affiliation = c.getAffiliation().get(0).getId().toString();
                }
                if(affiliation!= null){
                    metadata.addContactAffiliation(id, removeNewLineAndTab(affiliation));
                }
            }
            if(!isEmpty(originalContact)) {
                //Some files are annotated using more than one email, we will use the first one
                matcher = pattern.matcher(originalContact);

                if (matcher.find()) {
                    originalContact = matcher.group();
                    logger.debug("Original contact info: \"" + c.getContactInfo() + "\" email extracted -> \"" + originalContact + "\"");
                    if (!isEmpty(originalContact)) {
                        metadata.addContactEmail(id, originalContact);
                    }
                    else {
                        String mail  = CvUtilities.getMailFromCvParam(c);
                        if (!mail.isEmpty()) {
                            metadata.addContactEmail(id, mail);
                        }
                    }
                }
            }
            id++;

        }
    }

    /**
     * Processes the experiment additional params: (f.e. quant method, description...).
     */
    protected abstract void loadExperimentParams();

    /**
     * Load Sample Processing should be implemented by different controllers
     */
    protected abstract void loadSampleProcessing();

    /**
     * Load Instrument should be implemented by different controllers
     */
    protected abstract void loadInstrument();

    /*
     * Load Search Engines for from all proteins and psm.
     *
     * */
    protected void loadSearchEngineScores(){

        Map<SearchEngineScoreParam, Integer> psmScores = new HashMap<SearchEngineScoreParam, Integer>();
        Map<SearchEngineScoreParam, Integer> proteinScores = new HashMap<SearchEngineScoreParam, Integer>();
        proteinScoreToScoreIndex = new HashMap<String, Integer>();
        psmScoreToScoreIndex = new HashMap<String, Integer>();

        /**
         * Look for all scores are protein level, PSM, and ProteinHypothesis, PeptideHypothesis. We should
         * implement a way to keep track the order of Score in the mzTab related with the rank
         */

        Collection<Comparable> proteinHypothesisIds = source.getProteinIds();
        List<uk.ac.ebi.pride.utilities.data.core.Peptide> peptides = new ArrayList<uk.ac.ebi.pride.utilities.data.core.Peptide>();
        Iterator<Comparable> idProtein = proteinHypothesisIds.iterator();
        int iProtein =0;
        while(idProtein.hasNext()) {
            Comparable proteinId = idProtein.next();
            uk.ac.ebi.pride.utilities.data.core.Protein protein = source.getProteinById(proteinId);
            List<SearchEngineScoreParam> proteinParams = MzTabUtils.getSearchEngineScoreTerm(protein.getScore());

            for(SearchEngineScoreParam scoreCv: proteinParams){
                proteinScores.put(scoreCv, iProtein);
            }

            iProtein++;
            peptides.addAll(protein.getPeptides());
        }
        Iterator<uk.ac.ebi.pride.utilities.data.core.Peptide> idPeptide = peptides.iterator();
        int iPeptide =1;
        //Todo: Define the way to capture the order related with rank
        while(idPeptide.hasNext()) {
            SpectrumIdentification psmId = idPeptide.next().getSpectrumIdentification();
            List<SearchEngineScoreParam> psmParams = MzTabUtils.getSearchEngineScoreTerm(psmId.getScore());
            for(SearchEngineScoreParam scoreCv: psmParams){
                psmScores.put(scoreCv,iPeptide);
            }
            iPeptide++;
        }

        for(SearchEngineScoreParam param: proteinScores.keySet()){
            int idCount = metadata.getProteinSearchEngineScoreMap().size() + 1;
            metadata.addProteinSearchEngineScoreParam(idCount, param.getParam(null));
            proteinScoreToScoreIndex.put(param.getParam(null).getAccession(),idCount);
        }
        for(SearchEngineScoreParam param: psmScores.keySet()){
            int idCount = metadata.getPsmSearchEngineScoreMap().size() + 1;
            metadata.addPsmSearchEngineScoreParam(idCount,param.getParam(null));
            psmScoreToScoreIndex.put(param.getParam(null).getAccession(),idCount);
        }

        if (metadata.getProteinSearchEngineScoreMap().isEmpty()) {
            metadata.addProteinSearchEngineScoreParam(1, SearchEngineScoreParam.MS_SEARCH_ENGINE_SPECIFIC_SCORE.getParam(null));
            proteinScoreToScoreIndex.put(SearchEngineScoreParam.MS_SEARCH_ENGINE_SPECIFIC_SCORE.getParam(null).getAccession(), metadata.getProteinSearchEngineScoreMap().size() + 1);
        }
        if (metadata.getPsmSearchEngineScoreMap().isEmpty()) {
            metadata.addPsmSearchEngineScoreParam(1, SearchEngineScoreParam.MS_SEARCH_ENGINE_SPECIFIC_SCORE.getParam(null));
            psmScoreToScoreIndex.put(SearchEngineScoreParam.MS_SEARCH_ENGINE_SPECIFIC_SCORE.getParam(null).getAccession(), metadata.getPsmSearchEngineScoreMap().size() + 1);
        }
    }

    /**
     * Load all the files related with the original Identification file
     */
    protected abstract void loadMsRun();

    /**
     * Adds the sample parameters (species, tissue, cell type, disease) to the unit and the various sub-samples.
     */
    protected abstract void loadSamples();

    /**
     * Load Gel Data
     */
    protected abstract void loadGelData();


    private void loadURI(String expAccession) {
        if (expAccession == null || expAccession.isEmpty()) {
            return;
        }

        expAccession = expAccession.trim().replaceAll("\\s+","-");
        try {
            URI uri = new URI("http://www.ebi.ac.uk/pride/archive/assays/" + expAccession);
            metadata.addUri(uri);
        } catch (URISyntaxException e) {
            throw new DataAccessException("Error while building URI at the metadata section", e);
        }
    }

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory} which maintain a couple of {@link uk.ac.ebi.pride.jmztab.model.ProteinColumn}
     */
    @Override
    protected abstract MZTabColumnFactory convertProteinColumnFactory();

    /**
     * Generate {@link uk.ac.ebi.pride.jmztab.model.MZTabColumnFactory} which maintain a couple of {@link uk.ac.ebi.pride.jmztab.model.PSMColumn}
     */
    @Override
    protected MZTabColumnFactory convertPSMColumnFactory() {
        this.psmColumnFactory = MZTabColumnFactory.getInstance(Section.PSM);
        psmColumnFactory.addDefaultStableColumns();

        //Search engine score information (mandatory for all)
        for (Integer id : metadata.getPsmSearchEngineScoreMap().keySet()) {
            psmColumnFactory.addSearchEngineScoreOptionalColumn(PSMColumn.SEARCH_ENGINE_SCORE, id, null);
        }

        return this.psmColumnFactory;
    }

    /**
     * Fill records into model. This method will be called in {@link #getMZTabFile()} method.
     */
    @Override
    protected abstract void fillData();

    /**
     * Only one sample means this file is Identification and Complete. Otherwise, the file is Quantification and Summary.
     */
    protected boolean isIdentification() {
        return metadata.getSampleMap().size() == 1;
    }

    /**
     * This function validate if the file is supported in this case it only supports MZIdentML, PRIDE XML and MzTab
     * @param controller DataAccessController
     * @return A boolean value if the DataAccess controller is supported or not
     */
    protected static boolean isSupported(DataAccessController controller){
        return ((controller.getType() == DataAccessController.Type.MZIDENTML ||
                controller.getType() == DataAccessController.Type.MZTAB ||
                controller.getType() == DataAccessController.Type.XML_FILE) && controller.hasProtein());
    }

    /**
     * Return the Name of a file without the Extension
     * @param fileName The filename of the File
     * @return A filename String without the extension.
     */
    private String getFileNameWithoutExtension(String fileName) {
        int lastIndexOfDot = fileName.lastIndexOf(".");
        return fileName.substring(0, lastIndexOfDot);
    }

    protected static String generateAccession(uk.ac.ebi.pride.utilities.data.core.Protein identification) {
        String accession = identification.getDbSequence().getAccession();
        if(identification.getDbSequence().getAccessionVersion() != null ){
            //TODO: Standardise the way that we add the version
            accession = accession + '.' + identification.getDbSequence().getAccessionVersion();
        }
        return accession;
    }

    protected static String generateAccession(uk.ac.ebi.pride.utilities.data.core.Peptide identification) {
        String accession = identification.getPeptideEvidence().getDbSequence().getAccession();
        if(identification.getPeptideEvidence().getDbSequence().getAccessionVersion() != null ){
            //TODO: Standardise the way that we add the version
            accession = accession + '.' + identification.getPeptideEvidence().getDbSequence().getAccessionVersion();
        }
        return accession;
    }
}
