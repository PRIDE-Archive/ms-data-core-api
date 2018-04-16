package uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessMode;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessUtilities;
import uk.ac.ebi.pride.utilities.data.controller.cache.CacheEntry;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.utils.Constants;
import uk.ac.ebi.pride.utilities.data.utils.MzIdentMLUtils;
import uk.ac.ebi.pride.utilities.util.Tuple;

import java.io.File;
import java.util.*;

/**
 * Abstract Class that contains methods shared by mzTab controller and
 * mzIdentML controller and also some data structures share for both classes. This class is specialized in the
 * Controller of the MS files referenced from the mzIdentML and mzTab files.
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public abstract class ReferencedIdentificationController extends ResultFileController {

    // Logger property to trace the Errors
    private static final Logger logger = LoggerFactory.getLogger(ReferencedIdentificationController.class);

    /*
      * This is a set of controllers related with the MS information in the mzidentml file
      * one or more controllers can be related with the same file formats. The Comparable
      * name of the file is an id of SPECTRA DATA the file and the controller is the DataAccessController
       * related with the file.
     */
    protected Map<Comparable, DataAccessController> msDataAccessControllers;

    protected Set<Comparable> spectrumIds = new HashSet<Comparable>();


    public ReferencedIdentificationController(File file, DataAccessMode cacheAndSource) {
        super(file, cacheAndSource);
    }

    /**
     * Return the number of Spectra in the DataAccessController
     * @return The number of Spectra for DataAccessController
     */
    @Override
    public int getNumberOfSpectra() {
        int numberOfSpectra = 0;
        if (!msDataAccessControllers.isEmpty()) {
            Iterator iterator = msDataAccessControllers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                if (mapEntry.getValue() != null) {
                    numberOfSpectra += ((DataAccessController) mapEntry.getValue()).getNumberOfSpectra();
                }
            }
        }
        return numberOfSpectra;
    }

    /**
     * This method returns the number of spectra that are missing reference in the related peak file.
     * @return the total number of missing spectra.
     */
    @Override
    public int getNumberOfMissingSpectra(){
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        int totalSpecta = 0;
        int foundSpectra = 0;
        int currentsize;
        for(Comparable spectData: spectraDataIdMap.keySet()){
            currentsize = spectraDataIdMap.get(spectData).size();
            totalSpecta += currentsize;
            if (msDataAccessControllers != null && msDataAccessControllers.containsKey(spectData))
                foundSpectra += currentsize;
        }
        return totalSpecta-foundSpectra;
    }

    protected void cacheProtein(Protein ident) {
        // store identification into cache
        getCache().store(CacheEntry.PROTEIN, ident.getId(), ident);
        // store precursor charge and m/z
        Iterator<Peptide> itPeptide = ident.getPeptides().iterator();
        while(itPeptide.hasNext()){

            Peptide peptide = itPeptide.next();
            getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide);
            getCache().store(CacheEntry.PEPTIDE_START, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getPeptideEvidence().getStartPosition());
            getCache().store(CacheEntry.PEPTIDE_END, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getPeptideEvidence().getEndPosition());
            getCache().store(CacheEntry.PEPTIDE_TO_MODIFICATION, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getModifications());
            getCache().store(CacheEntry.PEPTIDE_SEQUENCE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getPeptideSequence().getSequence());
            getCache().store(CacheEntry.PEPTIDE_RANK, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getSpectrumIdentification().getRank());
            getCache().store(CacheEntry.PEPTIDE_TO_PARAM, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getParamGroup());
            getCache().store(CacheEntry.PEPTIDE_PRECURSOR_CHARGE, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getPrecursorCharge());
            getCache().store(CacheEntry.PEPTIDE_PRECURSOR_MZ, new Tuple<Comparable, Comparable>(ident.getId(), peptide.getId()), peptide.getPrecursorMz());

            if (hasSpectrum()) {
                Comparable spectrumId = getSpectrumIdBySpectrumIdentificationItemId(peptide.getSpectrumIdentification().getId());
                if(spectrumId != null){
                    Spectrum spectrum = getSpectrumById(spectrumId);
                    if(spectrum != null) {
                        List<Peptide> peptides = new ArrayList<Peptide>();
                        if(spectrum.getPeptide() != null)
                            peptides = spectrum.getPeptide();
                        peptides.add(peptide);
                        spectrum.setPeptide(peptides);
                        peptide.setSpectrum(spectrum);

                    }
                }
            }
        }
    }

    public List<DataAccessController> getSpectrumDataAccessControllers() {
        return new ArrayList<DataAccessController>(msDataAccessControllers.values());
    }

    private Map<Comparable, SpectraData> getSpectraDataMap() {
        Map<Comparable, SpectraData> spectraDataMapResult = (Map<Comparable, SpectraData>) getCache().get(CacheEntry.SPECTRA_DATA);
        if (spectraDataMapResult == null) {
            return new HashMap<Comparable, SpectraData>();
        } else {
            return spectraDataMapResult;
        }
    }


    /**
     * Get the number of Spectra by File associated with the mzidentml
     *
     * @param spectraData The SpectraData file with the Spectra
     * @return Number of Spectra Identified in the File.
     */
    public Integer getNumberOfSpectrabySpectraData(SpectraData spectraData) {
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        if(spectraDataIdMap != null && spectraDataIdMap.containsKey(spectraData.getId())){
            return spectraDataIdMap.get(spectraData.getId()).size();
        }
        return 0;
    }

    public Map<SpectraData, DataAccessController> getSpectraDataMSControllers() {

        Map<Comparable, SpectraData> spectraDataMap = getSpectraDataMap();

        Map<SpectraData, DataAccessController> mapResult = new HashMap<SpectraData, DataAccessController>();

        for (Comparable id : spectraDataMap.keySet()) {
            if (msDataAccessControllers.containsKey(id)) {
                mapResult.put(spectraDataMap.get(id), msDataAccessControllers.get(id));
            } else {
                mapResult.put(spectraDataMap.get(id), null);
            }
        }
        return mapResult;
    }

    /**
     * Get the Spectra Data Map with the corresponding File.
     * @return A Map of the SpectraData Objects with the corresponding MS File.
     */
    public Map<SpectraData, File> getSpectraDataMSFiles() {

        Map<SpectraData, DataAccessController> spectraDataControllerMAp = getSpectraDataMSControllers();

        Map<SpectraData, File> spectraDataFileMap = new HashMap<SpectraData, File>();

        for (SpectraData spectraData : spectraDataControllerMAp.keySet()) {
            DataAccessController controller = spectraDataControllerMAp.get(spectraData);
            spectraDataFileMap.put(spectraData, (controller == null) ? null : (File) controller.getSource());
        }
        return spectraDataFileMap;
    }

    public List<Comparable> getSupportedSpectraData() {
        Map<Comparable, SpectraData> spectraDataControllerMAp = getSpectraDataMap();
        List<Comparable> supported = new ArrayList<Comparable>();
        for (Comparable id : spectraDataControllerMAp.keySet()) {
            if (isSpectraDataSupported(spectraDataControllerMAp.get(id))) {
                supported.add(id);
            }
        }
        return supported;
    }

    protected boolean isSpectraDataSupported(SpectraData spectraData) {
        return (!(MzIdentMLUtils.getSpectraDataIdFormat(spectraData) == Constants.SpecIdFormat.NONE));

    }

    /**
     * Add a List of MS Files to the mzidentml.
     * @param dataAccessControllerFiles A List of DataAccessControllers related with the MzIdentML
     */
    public void addMSController(List<File> dataAccessControllerFiles) {

        Map<SpectraData, File> spectraDataFileMap = checkMScontrollers(dataAccessControllerFiles);
        addMSController(spectraDataFileMap);
    }

    /**
     * Check if the ms File is supported and match with some of the par of the name in the Spectra Files
     * This method should be used in high-throughput, when you add different files.
     *
     * @param msIdentMLFiles List of  the MS files related with the MZIdentML
     * @return The relation between the SpectraData and the corresponding File.
     */
    public Map<SpectraData, File> checkMScontrollers(List<File> msIdentMLFiles) {

        Map<Comparable, SpectraData> spectraDataMap = getSpectraDataMap();

        Map<SpectraData, File> spectraFileMap = new HashMap<SpectraData, File>();

        for (File file : msIdentMLFiles) {
            Set<Map.Entry<Comparable, SpectraData>> entries = spectraDataMap.entrySet();
            Iterator iterator = entries.iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                SpectraData spectraData = (SpectraData) mapEntry.getValue();
                if (spectraData.getLocation() != null && spectraData.getLocation().contains(file.getName())) {
                    spectraFileMap.put(spectraData, file);
                }else if(file.getName().contains(spectraData.getId().toString())
                        || (spectraData.getName() != null && file.getName().contains(spectraData.getName()))){
                    spectraFileMap.put(spectraData, file);
                }
            }
        }
        return spectraFileMap;
    }

    /**
     * Check if the File format is supported and the spectrum Id and add a Set of DataAccessControllers.
     * @param spectraDataFileMap A Map of SpectraData Files.
     */
    public void addMSController(Map<SpectraData, File> spectraDataFileMap) {

        Map<SpectraData, File> spectraDataControllerMap = getSpectraDataMSFiles();

        for (SpectraData spectraData : spectraDataControllerMap.keySet()) {
            for (SpectraData spectraDataFile : spectraDataFileMap.keySet()) {
                if (spectraDataControllerMap.get(spectraData) == null && spectraData.getId().equals(spectraDataFile.getId())) {
                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MZXML)
                        msDataAccessControllers.put(spectraData.getId(), new MzXmlControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MGF){
                        if(!getSpectraDataBasedOnTitle().contains(spectraDataFile.getId().toString()))
                            msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                        else
                            msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile),true));
                    }

                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MZML)
                        msDataAccessControllers.put(spectraData.getId(), new MzMLControllerImpl(spectraDataFileMap.get(spectraDataFile)));

                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.DTA)
                        msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));

                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.PKL)
                        msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));

                    if (Constants.getSpectraDataFormat(spectraData) == Constants.SpecFileFormat.MS2)
                        msDataAccessControllers.put(spectraData.getId(), new PeakControllerImpl(spectraDataFileMap.get(spectraDataFile)));
                    //Todo: Need to check if changes
                }
            }

        }
    }

    public void clearMSControllers() {
        msDataAccessControllers.clear();
    }


    public boolean addNewMSController(Map<SpectraData, File> spectraDataFileMap, Map<Comparable, File> newFiles, Map<Comparable, String> fileTypes) {

        Map<SpectraData, File> spectraDataControllerMap = getSpectraDataMSFiles();

        boolean changeStatus = false;

        for (SpectraData spectraData : spectraDataControllerMap.keySet()) {
            File newFile = newFiles.get(spectraData.getId());
            String fileType = fileTypes.get(spectraData.getId());
            File oldSpectraDataFile = spectraDataFileMap.get(spectraData);
            if (oldSpectraDataFile != null && newFile == null) {
                DataAccessController peakList = msDataAccessControllers.remove(spectraData.getId());
                peakList.close();
                changeStatus = true;
            } else if (oldSpectraDataFile == null && newFile != null ||
                    (Constants.getSpecFileFormat(fileType) != Constants.SpecFileFormat.NONE && oldSpectraDataFile != null && newFile != null && !newFile.getAbsolutePath().equalsIgnoreCase(oldSpectraDataFile.getAbsolutePath()))) {
                DataAccessController peakList = createMSDataAccessController(newFile, fileType, getSpectraDataBasedOnTitle().contains(spectraData.getId()));
                msDataAccessControllers.put(spectraData.getId(), peakList);
                changeStatus = true;
            }
            if (changeStatus) {
                getCache().clear(CacheEntry.SPECTRUM);
            }
        }
        return changeStatus;
    }

    DataAccessController createMSDataAccessController(File file, String fileType, boolean useTitle) {
        Constants.SpecFileFormat fileFormatType = Constants.SpecFileFormat.valueOf(fileType);
        if (fileFormatType != null && file != null) {
            if (fileFormatType == Constants.SpecFileFormat.MZXML)
                return new MzXmlControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MGF)
                if(useTitle)
                    return new PeakControllerImpl(file,true);
                else
                    return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MZML)
                return new MzMLControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.DTA)
                return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.PKL)
                return new PeakControllerImpl(file);
            if(fileFormatType == Constants.SpecFileFormat.MS2)
                return new PeakControllerImpl(file);
            if (fileFormatType == Constants.SpecFileFormat.MZDATA)
                return new MzDataControllerImpl(file);
            //Todo: Need to check if changes
        }
        return null;
    }

    /**
     * Is identified Spectrum return true if the spectrum was identified
     *
     * @param specId The Spectrum Identification Item, it Can be an spectrum Identification Item or a Peptide ID
     * @return True if the spectrum is identified or false if is not identified
     */
    @Override
    public boolean isIdentifiedSpectrum(Comparable specId) {
        String[] array = specId.toString().split("!");
        if(array.length < 2){
            if(getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM, specId) != null)
                return true;
        }else{
            Tuple<String, String> specTuple = new Tuple<String, String>(array[0], array[1]);
            if(getCache().get(CacheEntry.SPECTRUM_IDENTIFIED,specTuple) != null)
                return true;
        }
        return false;
    }

    /**
     * Retrieve the Identified Peptides related with one spectrum
     * @param specId The spectrum-identification identifier
     * @return java.lang.List<Peptide> A list of peptides identified by this Spectrum
     */
    public List<Peptide> getPeptidesBySpectrum(Comparable specId){
        Map<Comparable, Tuple<String,String>> peptideToSpectrum = ((Map<Comparable, Tuple<String,String>>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM));
        Map<Comparable, List<Comparable>> proteinToPeptide  = ((Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.PROTEIN_TO_PEPTIDE_EVIDENCES));
        List<Peptide> peptides = new ArrayList<Peptide>();

        for(Map.Entry spectrumIDEntry: peptideToSpectrum.entrySet()){
            Comparable peptideID = (Comparable) spectrumIDEntry.getKey();
            Tuple<String,String> spectrumID  = (Tuple<String,String>) spectrumIDEntry.getValue();
            String spectrumIDString = spectrumID.getKey() + "!" + spectrumID.getValue();
            if(spectrumIDString.equalsIgnoreCase(specId.toString()) && proteinToPeptide != null){
                for(Map.Entry proteinEntry: proteinToPeptide.entrySet()){
                    Comparable proteinId = (Comparable) proteinEntry.getKey();
                    List<Comparable> peptidePerProtein = (List<Comparable>) proteinEntry.getValue();
                    if(peptidePerProtein.contains(peptideID)){
                        Peptide peptideIdentified = getPeptideByIndex(proteinId, peptideID);
                        peptides.add(peptideIdentified);
                    }
                }
            }
        }

        return peptides;
    }

    @Override
    public Collection<Comparable> getSpectrumIds() {
        Collection<Comparable> spectrumIds = super.getSpectrumIds();
        if (spectrumIds.size() == 0 && hasSpectrum()) {
            spectrumIds = new HashSet<Comparable>();
            for (Comparable id : msDataAccessControllers.keySet()) {
                if (msDataAccessControllers.get(id) != null)
                    for (Comparable idSpectrum : msDataAccessControllers.get(id).getSpectrumIds()) {
                        spectrumIds.add(idSpectrum + "!" + id);
                    }
            }
            getCache().storeInBatch(CacheEntry.SPECTRUM_ID, spectrumIds);
        }

        return spectrumIds;
    }

    /**
     * Get spectrum using a spectrumIdentification id, gives the option to choose whether to
     * use cache. This implementation provides a way of by passing the cache.
     *
     * @param id       spectrum Identification ID
     * @param useCache true means to use cache
     * @return Spectrum spectrum object
     */
    @Override
    public Spectrum getSpectrumById(Comparable id, boolean useCache) {
        Spectrum spectrum = super.getSpectrumById(id, useCache);

        if(spectrum == null){
            Tuple<String,String> spectrumIdArray;
            if (((String) id).split("!").length != 2) {
                spectrumIdArray = ((Map<Comparable, Tuple<String,String>>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM)).get(id);
            }else{
                spectrumIdArray = new Tuple<>(((String) id).split("!")[0], ((String) id).split("!")[1]);
            }
            if (spectrumIdArray != null) {
                logger.debug("Get new spectrum from file: {}", id);
                try {
                    DataAccessController spectrumController = msDataAccessControllers.get(spectrumIdArray.getValue());
                    if(spectrumController != null){
                        Collection<Comparable> spectrumIds = spectrumController.getSpectrumIds();
                        if (spectrumController != null && spectrumIds.contains(spectrumIdArray.getKey())) {
                            spectrum = spectrumController.getSpectrumById(spectrumIdArray.getKey());
                            if (useCache && spectrum != null) {
                                getCache().store(CacheEntry.SPECTRUM, id, spectrum);
                                getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE, id, DataAccessUtilities.getPrecursorChargeParamGroup(spectrum));
                                getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_MZ, id, DataAccessUtilities.getPrecursorMz(spectrum));
                            }
                        }
                    }

                } catch (Exception ex) {
                    String msg = "Error while getting spectrum: " + id;
                    logger.error(msg, ex);
                    throw new DataAccessException(msg, ex);
                }
            }
        }
        if (spectrum != null) {
            spectrum.setId(id);
        }
        return spectrum;
    }

    /**
     * If the spectrum information associated with the identification files is provided
     * then the mzidentml contains spectra.
     *
     * @return if the spectrum files is provided then is true else false.
     */
    @Override
    public boolean hasSpectrum() {
        if (msDataAccessControllers != null) {
            for (Comparable id : msDataAccessControllers.keySet()) {
                if (msDataAccessControllers.get(id) != null) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get the number of Spectra Identified in the DataAccessController
     * @return int the number of spectra identified in the DataAccessController
     */
    @Override
    public int getNumberOfIdentifiedSpectra() {
        Map<Comparable, List<Comparable>> spectraDataIdMap = (Map<Comparable, List<Comparable>>) getCache().get(CacheEntry.SPECTRADATA_TO_SPECTRUMIDS);
        int countSpectra = 0;
        if(spectraDataIdMap != null){
            Iterator iterator = spectraDataIdMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                if(mapEntry != null && mapEntry.getValue() != null)
                    countSpectra += ((List<Comparable>) mapEntry.getValue()).size();
            }                            }
        return countSpectra;
    }

    public Comparable getSpectrumIdBySpectrumIdentificationItemId(Comparable id) {

        Tuple<String, String> spectrumIdArray = (Tuple<String, String>) getCache().get(CacheEntry.PEPTIDE_TO_SPECTRUM,id);

        /** To store in cache the Spectrum files, an Id was constructed using the spectrum ID and the
         *  id of the File.
         **/
        if (spectrumIdArray == null || spectrumIdArray.getKey() == null || spectrumIdArray.getValue() == null) {
            return null;
        } else {
            return spectrumIdArray.getKey() + "!" + spectrumIdArray.getValue();
        }
    }

    @Override
    public Comparable getPeptideSpectrumId(Comparable proteinId, Comparable peptideId) {
        Peptide peptide = super.getPeptideByIndex(proteinId, peptideId, true);

        if (peptide == null) {
            logger.debug("Get new peptide from file: {}", peptideId);
            Protein ident = getProteinById(proteinId);
            peptide = ident.getPeptides().get(Integer.parseInt(peptideId.toString()));
        }

        Comparable spectrumIdentificationId = peptide.getSpectrumIdentification().getId();
        return getSpectrumIdBySpectrumIdentificationItemId(spectrumIdentificationId);
    }

    /**
     * Get peptide using a given identification id and a given peptide index
     *
     * @param index    peptide index
     * @param useCache whether to use cache
     * @return Peptide  peptide
     */
    @Override
    public Peptide getPeptideByIndex(Comparable proteinId, Comparable index, boolean useCache) {
        Peptide peptide = super.getPeptideByIndex(proteinId, index, useCache);
        if (peptide == null || (peptide.getSpectrum() == null && hasSpectrum())) {
            logger.debug("Get new peptide from file: {}", index);
            Protein ident = getProteinById(proteinId);

            peptide = ident.getPeptides().get(Integer.parseInt(index.toString()));
            if (useCache && peptide != null) {
                //getCache().store(CacheEntry.PEPTIDE, new Tuple<Comparable, Comparable>(proteinId, index), peptide);
                Spectrum spectrum = peptide.getSpectrum();
                if (hasSpectrum()) {
                    spectrum = getSpectrumById(peptide.getSpectrumIdentification().getId());
                    if(spectrum != null){
                        List<Peptide> peptides = new ArrayList<Peptide>();
                        if(spectrum.getPeptide() != null)
                            peptides = spectrum.getPeptide();
                        peptides.add(peptide);
                        spectrum.setPeptide(peptides);
                        peptide.setSpectrum(spectrum);
                    }
                }
                if (spectrum != null) {
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_CHARGE, spectrum.getId(), DataAccessUtilities.getPrecursorChargeParamGroup(spectrum));
                    getCache().store(CacheEntry.SPECTRUM_LEVEL_PRECURSOR_MZ, spectrum.getId(), DataAccessUtilities.getPrecursorMz(spectrum));
                }
            }
        }
        return peptide;
    }

    @Override
    public int getPeptideRank(Comparable proteinId, Comparable peptideId) {
        Integer rank = (Integer) getCache().get(CacheEntry.PEPTIDE_RANK, new Tuple<Comparable, Comparable>(proteinId, peptideId));

        if (rank == null) {
            logger.debug("Get new peptide from file: {}", peptideId);
            Protein ident = getProteinById(proteinId);
            Peptide peptide = ident.getPeptides().get(Integer.parseInt(peptideId.toString()));
            rank = peptide.getSpectrumIdentification().getRank();
        }

        return rank;
    }

    /**
     * Check if the PROTEIN_GROUP Cache contains any element
     * @return TRUE if the file contains protein groups
     */
    @Override
    public boolean hasProteinAmbiguityGroup() {
        return super.getProteinAmbiguityGroupIds().size() > 0;
    }

    abstract public List<SpectraData> getSpectraDataFiles();
}
