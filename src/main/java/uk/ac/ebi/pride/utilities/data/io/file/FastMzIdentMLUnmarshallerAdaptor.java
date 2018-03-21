package uk.ac.ebi.pride.utilities.data.io.file;

import uk.ac.ebi.pride.utilities.data.lightModel.*;

import java.io.File;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Suresh Hewapathirana
 */
public class FastMzIdentMLUnmarshallerAdaptor {

    private FastMzIdentMLUnmarshaller fastMzIdentMLUnmarshaller;

    /**
     * This method retrieves the MzIdentML object from the FastMzIdentMLUnmarshaller
     *
     * @param mzIdentMLFile MzIdentML file
     * @see FastMzIdentMLUnmarshaller
     */
    public FastMzIdentMLUnmarshallerAdaptor(File mzIdentMLFile) {
        fastMzIdentMLUnmarshaller = FastMzIdentMLUnmarshaller.getInstance(mzIdentMLFile);
    }

    /**
     * Get the complete MzIdentML object which contrains the entire unmarshalled content
     *
     * @return MzIdentML ojbect
     */
    public MzIdentML getMzIdentML() {
        return fastMzIdentMLUnmarshaller.getMzIdentML();
    }

    /**
     * Get all the Protein Ids reported in the MzIdentML
     *
     * @return Collection of Protein Ids
     */
    public Collection<Comparable> getProteinIds() {
        return fastMzIdentMLUnmarshaller.getMzIdentML().getSequenceCollection().getDBSequence()
                .parallelStream()
                .map(DBSequence::getId)
                .collect(Collectors.toList());
    }

    /**
     * Get all the Peptide Ids reported in the MzIdentML
     *
     * @return Collection of Peptide Ids
     */
    public Collection<Comparable> getPeptideIds() {
        return fastMzIdentMLUnmarshaller.getMzIdentML().getSequenceCollection().getPeptide()
                .parallelStream()
                .map(Peptide::getId)
                .collect(Collectors.toList());
    }

    /**
     * Get Peptide by peptide ID
     *
     * @param peptideID Peptide Reference ID
     * @return Peptide if matching peptide exists, otherwise return a null
     */
    public Peptide getPeptideById(Comparable peptideID) {
        return fastMzIdentMLUnmarshaller.getMzIdentML().getSequenceCollection().getPeptide()
                .stream()
                .filter(p -> p.getId().equals(peptideID))
                .findFirst()
                .orElse(null);
    }

    /**
     * Get all the SpectrumIdentificationList from the MzIdentML
     *
     * @return List of SpectrumIdentificationList
     */
    public List<SpectrumIdentificationList> getSpectrumIdentificationList() {
        return fastMzIdentMLUnmarshaller.getMzIdentML().getDataCollection().getAnalysisData().getSpectrumIdentificationList();
    }

    /**
     * Given a peptide, this method extract peptide modifications and their Monoisotopic MassDelta
     *
     * @param peptide uk.ac.ebi.pride.utilities.data.lightModel.Peptide type object
     * @return List of PTM Masses
     */
    public List<Double> getPTMMassesFromPeptide(Peptide peptide) {
        List<Double> ptmMasses = new ArrayList<>();
        for (Modification modification : peptide.getModification()) {
            double monoMasses = modification.getMonoisotopicMassDelta();
            ptmMasses.add(monoMasses);
        }
        return ptmMasses;
    }


    /**
     * This method extract the Inputs from the MzIdentML object and
     * for each input file, it retrieves the Spectra Data such as
     * file location, file format etc.
     *
     * @return Map<Comparable, SpectraData> where Comparable will be the Id of the SpectraData
     *
     *  Example of DataCollection section in MzIdentML:
     * <DataCollection>
     * <Inputs>
     * <SpectraData location="file:///lolo//small.mgf" id="SD_1">
     * <FileFormat>
     * <cvParam accession="MS:1001062" name="Mascot MGF file" cvRef="PSI-MS" />
     * </FileFormat>
     * <SpectrumIDFormat>
     * <cvParam accession="MS:1000774" name="multiple peak list nativeID format" cvRef="PSI-MS" />
     * </SpectrumIDFormat>
     * </SpectraData>
     * </Inputs>
     * </DataCollection>
     */
    public Map<Comparable, SpectraData> getSpectraDataMap() {
        Inputs inputs = fastMzIdentMLUnmarshaller.getMzIdentML().getDataCollection().getInputs();
        List<SpectraData> spectraDataList = inputs.getSpectraData();
        Map<Comparable, SpectraData> spectraDataMap = null;
        if (spectraDataList != null && spectraDataList.size() > 0) {
            spectraDataMap = new HashMap<>();
            for (SpectraData spectraData : spectraDataList) {
                spectraDataMap.put(spectraData.getId(), spectraData);
            }
        }
        return spectraDataMap;
    }

    /**
     * Close FastMzIdentMLUnmarshallerAdaptor by clearing the entire mzIdentML Object.
     *
     * Warning: This should be carefully used only after performing all the data access operations.
     * If not, fastMzIdentMLUnmarshaller has to unmarshall MzIdentML file again, which is
     * computationally expensive.
     */
    public void close() {
        fastMzIdentMLUnmarshaller.destroy();
    }
}
