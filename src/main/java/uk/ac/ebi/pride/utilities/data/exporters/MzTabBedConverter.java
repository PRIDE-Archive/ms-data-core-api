package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.distribution.IntegerDistribution;
import uk.ac.ebi.pride.jmztab.model.*;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.UserParam;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.*;


/**
 * Class to convert MzTab files to Bed files. Requires chromosome information to be present.
 *
 * @author Tobias Ternent tobias@ebi.ac.uk
 */
public class MzTabBedConverter {

    private MzTabControllerImpl mzTabController;
    private String projectAccession;
    private String assayAccession;
    private boolean peptideLocation;

    /**
     * Constructor to setup conversion of an mzTabFile into a bed file.
     * @param mzTabFile to be converted to a bed file.
     */
    public MzTabBedConverter(MzTabControllerImpl mzTabFile) {
        this.mzTabController = mzTabFile;
        this.projectAccession = "";
        this.assayAccession = "";
        peptideLocation = true;
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, String assayAccession) {
        this.mzTabController = mzTabFile;
        this.projectAccession = projectAccession;
        this.assayAccession = assayAccession;
        peptideLocation = true;
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, String assayAccession, boolean peptideLocation) {
        this.mzTabController = mzTabFile;
        this.projectAccession = projectAccession;
        this.assayAccession = assayAccession;
        this.peptideLocation = peptideLocation;
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
                        String chrom = "null", chromstart = "null", chromend = "null", strand = "null", pepMods = "null",
                                genMods = "null",psmScore = "null", fdrScore = "null",
                                blockStarts = "null", blockSizes = "null", blockCount="1";
                        ArrayList<Position> positions = new ArrayList<>();
                        for (UserParam userParam : peptideEvidence.getUserParams()) {
                            switch (userParam.getName()) {
                                case ("chr"):
                                    chrom = userParam.getValue();
                                    break;
                                case ("start_map"):
                                    positions.add(new Position(Position.LocationEnum.START.name(), Integer.parseInt(userParam.getValue())));
                                    //chromstart = userParam.getValue();
                                    break;
                                case ("end_map"):
                                    positions.add(new Position(Position.LocationEnum.END.name(), Integer.parseInt(userParam.getValue())));
                                    //chromend = userParam.getValue();
                                    break;
                                case ("strand"):
                                    strand = userParam.getValue();
                                    break;
                                default:
                                    break;
                            }
                        }
                        int totalBlocksSize = 0;
                        List<String> blockStartsStrings = new ArrayList<>();
                        List<String> blockSizesStrings  = new ArrayList<>();
                        Collections.sort(positions);
                        Iterator iterator = positions.iterator();
                        String previousLocation = Position.LocationEnum.END.name();
                        int previousEndValue = 0;
                        ArrayList<Integer> startIntegers = new ArrayList<>();
                        ArrayList<Integer> endIntegers = new ArrayList<>();
                        while (iterator.hasNext()) {
                            Position element = (Position) iterator.next();
                            if (previousLocation.equalsIgnoreCase(Position.LocationEnum.START.name())) {
                                if (element.location.equalsIgnoreCase(Position.LocationEnum.START.name())) {
                                    // - START, then now START - do nothing
                                } else {
                                    // - START, then now END - add new end
                                    endIntegers.add(new Integer(element.value));
                                    previousLocation = element.location;
                                    previousEndValue = element.value;
                                }
                            } else {
                                if (element.location.equalsIgnoreCase(Position.LocationEnum.START.name())) {
                                    // - END, then now START -  add new start
                                    startIntegers.add(new Integer(element.value));
                                    previousLocation = element.location;
                                } else {
                                    // - END, then now END - replace previous end
                                    endIntegers.remove(new Integer(previousEndValue));
                                    endIntegers.add(new Integer(element.value));
                                    previousEndValue = element.value;
                                }
                            }
                        }
                        if (startIntegers.size()>0) {
                            Collections.sort(startIntegers);
                            Collections.sort(endIntegers);
                            chromstart = startIntegers.get(0).toString();
                            chromend = endIntegers.get(endIntegers.size()-1).toString();
                            ArrayList<Integer> blockSizesIntegers = new ArrayList<>();
                            ArrayList<Integer> blockStartsIntegers = new ArrayList<>();
                            for (int i =0; i < startIntegers.size(); i++) {
                                blockSizesIntegers.add(new Integer(endIntegers.get(i).intValue() - startIntegers.get(i).intValue()));
                                blockStartsIntegers.add(new Integer(startIntegers.get(i) - Integer.parseInt(chromstart)));
                            }

                            blockStartsStrings = new ArrayList<String>(blockStartsIntegers.size());
                            for (Integer myInt : blockStartsIntegers) {
                                blockStartsStrings.add(String.valueOf(myInt));
                            }
                            blockSizesStrings = new ArrayList<String>(blockSizesIntegers.size());
                            for (Integer myInt : blockSizesIntegers) {
                                blockSizesStrings.add(String.valueOf(myInt));
                                totalBlocksSize += myInt.intValue();
                            }
                            blockStarts = StringUtils.join(blockStartsStrings, ",");
                            blockSizes = StringUtils.join(blockSizesStrings, ",");
                            blockCount =  "" + startIntegers.size();


                            for (CvParam cvParam : peptideEvidence.getCvParams()) {
                                if (cvParam.getAccession().equalsIgnoreCase("MS:1002356")) {
                                    fdrScore = "[" + cvParam.getCvLookupID() + ", " + cvParam.getAccession() + ", " + cvParam.getName()
                                            + ", " + cvParam.getValue() + "]";
                                    break;
                                }
                            }
                        }
                        ArrayList<String> peptideModifications = new ArrayList<>();
                        ArrayList<String> genomeModifications = new ArrayList<>();
                        for (Modification modification : peptideEvidence.getPeptideSequence().getModifications()) {
                            int peptideLocation = modification.getLocation();
                            int genomeLocation;
                            final int TOTAL = peptideEvidence.getPeptideSequence().getSequence().length();
                            double i = modification.getLocation() / TOTAL;
                            double j = i * totalBlocksSize;
                            int blockIndex = 0;
                            int currentBlockTotal = 0;
                            genomeLocation = (int) Math.round(j);
                            for (String blockSize : blockSizesStrings) {
                                int block = Integer.valueOf(blockSize);
                                currentBlockTotal += block;
                                if (j <= currentBlockTotal) {
                                    genomeLocation = Integer.valueOf(blockStartsStrings.get(blockIndex)) + genomeLocation;
                                    break;
                                }
                                genomeLocation -= block;
                                blockIndex++;
                            }
                            for (CvParam cvParam : modification.getCvParams()) {
                                peptideModifications.add(peptideLocation + "-" + cvParam.getAccession());
                                genomeModifications.add(genomeLocation + "-" + cvParam.getAccession());
                            }
                        }
                        if (peptideModifications.size() > 0) {
                            pepMods = StringUtils.join(peptideModifications, ", ");
                            genMods = StringUtils.join(genomeModifications, ", ");
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
                            stringBuilder.append(blockCount); // blockCount
                            stringBuilder.append('\t');
                            stringBuilder.append(blockSizes); // blockSizes
                            stringBuilder.append('\t');
                            stringBuilder.append(blockStarts); // chromStarts (actual name, but refers to blocks)
                            stringBuilder.append('\t') ;
                            stringBuilder.append(protein.getDbSequence().getName()); // proteinAccession
                            stringBuilder.append('\t') ;
                            stringBuilder.append(peptideEvidence.getPeptideSequence().getSequence());  // peptideSequence
                            stringBuilder.append('\t');
                            stringBuilder.append(psmScore); // psmScore
                            stringBuilder.append('\t');
                            stringBuilder.append(fdrScore); // fdrScore
                            stringBuilder.append('\t');
                            stringBuilder.append(pepMods); // peptide location modifications
                            stringBuilder.append('\t');
                            stringBuilder.append(genMods); // genome location modifications
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getPrecursorCharge()); // charge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getExperimentalMassToCharge()); // expMassToCharge
                            stringBuilder.append('\t');
                            stringBuilder.append(peptide.getSpectrumIdentification().getCalculatedMassToCharge()); // calcMassToCharge
                            stringBuilder.append('\t');
                            String datasetID = "";
                            if (!projectAccession.isEmpty()) {
                                datasetID = projectAccession;
                                if (!assayAccession.isEmpty()) {
                                    datasetID = datasetID + "_" + assayAccession;
                                }
                            } else {
                                datasetID = mzTabController.getReader().getMetadata().getMZTabID();
                            }
                            stringBuilder.append(datasetID); // datasetID
                            stringBuilder.append('\t');
                            String projectUri ;
                            if (!projectAccession.isEmpty()) {
                                projectUri = "http://www.ebi.ac.uk/pride/archive/projects/" + projectAccession;
                            }  else {
                                projectUri = "null";
                            }
                            stringBuilder.append(projectUri); // projectURI
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

class Position implements Comparable<Position>{
    String location;
    int value;
    public enum LocationEnum {
        START, END
    }

    Position(String location, int value) {
        this.location = location;
        this.value = value;
    }

    public int compareTo(Position anotherPosition) {
        return Integer.compare(value, anotherPosition.value);
    }

}
