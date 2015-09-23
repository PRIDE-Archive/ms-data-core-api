package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;


/**
 * Class to convert MzTab files to Bed files. Requires chromosome information to be present.
 *
 * @author Tobias Ternent tobias@ebi.ac.uk
 */
public class MzTabBedConverter {

    private MzTabControllerImpl mzTabController;
    private String projectAccession;
    private String assayAccession;

    /**
     * Constructor to setup conversion of an mzTabFile into a bed file.
     * @param mzTabFile to be converted to a bed file.
     */
    public MzTabBedConverter(MzTabControllerImpl mzTabFile) {
        this.mzTabController = mzTabFile;
        this.projectAccession = "";
        this.assayAccession = "";
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, String assayAccession) {
        this.mzTabController = mzTabFile;
        this.projectAccession = projectAccession;
        this.assayAccession = assayAccession;
    }


    /**
     * Performs the conversion of the mzTabFile into a bed file.
     * @param outputFile is the generated output bed file,
     * @throws Exception
     */
    public void convert(File outputFile) throws Exception {
        FileWriter file = new FileWriter(outputFile.getPath());
        BufferedWriter bf = new BufferedWriter(file);
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.setLength(0);
        int lineNumber = 1;
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            Protein protein = mzTabController.getProteinById(proteinID);
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : protein.getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptide.getPeptideEvidence())) {
                        evidences.add(peptide.getPeptideEvidence());
                        String chrom = "null", chromstart = "null", chromend = "null", strand = "null", mods = "null", psmScore = "null";
                        for (UserParam userParam : peptideEvidence.getUserParams()) {
                            switch (userParam.getName()) {
                                case ("chr"):
                                    chrom = userParam.getValue();
                                    break;
                                case ("start_map"):
                                    chromstart = userParam.getValue();
                                    break;
                                case ("end_map"):
                                    chromend = userParam.getValue();
                                    break;
                                case ("strand"):
                                    strand = userParam.getValue();
                                    break;
                                default:
                                    break;
                            }
                            for (CvParam cvParam : peptideEvidence.getCvParams()) {
                                if (cvParam.getAccession().equalsIgnoreCase("MS:1002356")) {
                                    psmScore = "[" + cvParam.getCvLookupID() + ", " + cvParam.getAccession() + ", " + cvParam.getName()
                                            + ", " + cvParam.getValue() + "]";
                                    break;
                                }
                            }
                        }
                        ArrayList<String> modifications = new ArrayList<>();
                        for (Modification modification : peptideEvidence.getPeptideSequence().getModifications()) {
                            int location = modification.getLocation();
                            for (CvParam cvParam : modification.getCvParams()) {
                                modifications.add(location + "-" + cvParam.getAccession());
                            }
                        }
                        if (modifications.size() > 0) {
                            mods = StringUtils.join(modifications, ", ");
                        }

                        if (!chrom.equalsIgnoreCase("null")) {
                            stringBuilder.append(chrom); // chrom
                            stringBuilder.append('\t');
                            stringBuilder.append(chromstart); // chromstart
                            stringBuilder.append('\t');
                            stringBuilder.append(chromend); // chromend
                            stringBuilder.append('\t');
                            String name =  protein.getDbSequence().getName();
                            if (!projectAccession.isEmpty()) {
                                name = name + "_" + projectAccession;
                            }
                            if (!assayAccession.isEmpty()) {
                                name = name + "_" + assayAccession;
                            }
                            name = name + "_" + ++lineNumber;
                            stringBuilder.append(name); // name
                            stringBuilder.append('\t');
                            stringBuilder.append(1000); // score (1000)
                            stringBuilder.append('\t');
                            stringBuilder.append(strand); // strand
                            stringBuilder.append('\t');
                            stringBuilder.append(chromstart); // thickStart
                            stringBuilder.append('\t');
                            stringBuilder.append(chromend); // thickEnd
                            stringBuilder.append('\t');
                            stringBuilder.append("0"); // reserved - (0 only)
                            stringBuilder.append('\t');
                            stringBuilder.append("1"); // blockCount (1 only)
                            stringBuilder.append('\t');
                            stringBuilder.append(Integer.parseInt(chromend) - Integer.parseInt(chromstart)); // blockSizes (1 only)
                            stringBuilder.append('\t');
                            stringBuilder.append("0"); // blockStarts (0 only)
                            stringBuilder.append('\t') ;
                            stringBuilder.append(protein.getDbSequence().getName()); // protein_name
                            stringBuilder.append('\t') ;
                            stringBuilder.append(peptideEvidence.getPeptideSequence().getSequence());  // peptide_sequence
                            stringBuilder.append('\t');
                            stringBuilder.append(peptideEvidence.getStartPosition()); // pep_start
                            stringBuilder.append('\t');
                            stringBuilder.append(peptideEvidence.getEndPosition()); // pep_end
                            stringBuilder.append('\t');
                            stringBuilder.append(psmScore); // psm_score
                            stringBuilder.append('\t');
                            stringBuilder.append(mods); // modifications
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getPrecursorCharge()); // charge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getExperimentalMassToCharge()); // exp_mass_to_charge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getCalculatedMassToCharge()); // calc_mass_to_charge
                            stringBuilder.append('\t');
                            stringBuilder.append(projectAccession); // project_accession
                            stringBuilder.append('\t');
                            stringBuilder.append(assayAccession); // assay_accession
                            stringBuilder.append('\t');
                            if (!projectAccession.isEmpty()) {
                                stringBuilder.append("http://www.ebi.ac.uk/pride/archive/projects/").append(projectAccession); // project_uri
                            }  else {
                                stringBuilder.append(""); // project_uri
                            }

                            stringBuilder.append('\n');
                            bf.write(stringBuilder.toString());
                            bf.flush();
                        }
                        stringBuilder.setLength(0);
                    }
                }
            }
        }
        bf.close();
    }
}
