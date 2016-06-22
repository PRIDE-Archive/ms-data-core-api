package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
    private boolean proteoAnnotatorReporcessed;
    private String PROBED_VERSION = "0.4";

    /**
     * Constructor to setup conversion of an mzTabFile into a bed file.
     * @param mzTabFile to be converted to a bed file.
     */
    public MzTabBedConverter(MzTabControllerImpl mzTabFile) {
        this(mzTabFile, "", "", false);
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, boolean proteoAnnotatorReporcessed) {
        this(mzTabFile, "", "", proteoAnnotatorReporcessed);
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession) {
        this(mzTabFile, projectAccession, "", false);
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, boolean proteoAnnotatorReporcessed) {
        this(mzTabFile, projectAccession, "", proteoAnnotatorReporcessed);
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, String assayAccession) {
        this(mzTabFile, projectAccession, assayAccession, false);
    }

    public MzTabBedConverter(MzTabControllerImpl mzTabFile, String projectAccession, String assayAccession, boolean proteoAnnotatorReporcessed) {
        this.mzTabController = mzTabFile;
        this.projectAccession = projectAccession;
        this.assayAccession = assayAccession;
        this.proteoAnnotatorReporcessed = proteoAnnotatorReporcessed;
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

        List<String> allPeptideSequences = new ArrayList<>();
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : mzTabController.getProteinById(proteinID).getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptideEvidence)) {
                        evidences.add(peptideEvidence);
                        if (hasChromCvps(peptideEvidence.getCvParams())) {
                            allPeptideSequences.add(peptideEvidence.getPeptideSequence().getSequence());
                        }
                    }
                }
            }
        }
        Set<String> duplicatePeptideSequences = new HashSet();
        Set<String> tempePeptideSequences = new HashSet();
        for (String peptideSequence : allPeptideSequences) {
            if (!tempePeptideSequences.add(peptideSequence)) {
                duplicatePeptideSequences.add(peptideSequence);
            }
        }

        HashMap<Comparable, ProteinGroup> proteinGroupHashMap = new HashMap<>();
        if (mzTabController.hasProteinAmbiguityGroup()) {
            Collection<Comparable>  groupIDs = mzTabController.getProteinAmbiguityGroupIds();
            proteinGroupHashMap = new HashMap<>();
            for (Comparable groupID : groupIDs) {
                proteinGroupHashMap.put(groupID, mzTabController.getProteinAmbiguityGroupById(groupID));
            }
        }
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            Protein protein = mzTabController.getProteinById(proteinID);
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : protein.getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptideEvidence)) {
                        evidences.add(peptideEvidence);
                        String chrom = "null", chromstart = "null", chromend = "null", strand = "null", pepMods = "null",
                                psmScore = "null", fdrScore = "null",
                                blockStarts = "null", blockSizes = "null", blockCount="1", buildVersion="null";
                        for (CvParam cvParam : peptideEvidence.getCvParams()) {
                            switch (cvParam.getAccession()) {
                                case ("MS:1002637"):
                                    chrom = cvParam.getValue();
                                    break;
                                case ("MS:1002643"):
                                    String[] starts = checkFixEndComma(cvParam.getValue()).split(",");
                                    chromstart = starts[0];
                                    for (int i=0; i<starts.length; i++) {
                                        starts[i] = "" + (Integer.parseInt(starts[i]) - Integer.parseInt(chromstart));
                                    }
                                    blockStarts = StringUtils.join(starts, ",");
                                    break;
                                case ("MS:1002641"):
                                    blockCount = cvParam.getValue();
                                    break;
                                case ("MS:1002638"):
                                    strand = cvParam.getValue();
                                    break;
                                case ("MS:1002642"):
                                    blockSizes = checkFixEndComma(cvParam.getValue());
                                    break;
                                case ("MS:1002356"):
                                    fdrScore = cvParam.getValue();
                                    break;
                                case ("MS:1002345"):
                                    psmScore = cvParam.getValue();
                                    break;
                                case ("MS:1002644"):
                                    buildVersion = cvParam.getValue();
                                    break;
                                default:
                                    break;
                                // todo
                                // has protein group/inference?
                                // has proteindetectionprotocol thresholding?
                                // grouping PSMs?
                            }
                        }
                       ArrayList<String> peptideModifications = new ArrayList<>();
                        for (Modification modification : peptideEvidence.getPeptideSequence().getModifications()) {
                            for (CvParam cvParam : modification.getCvParams()) {
                                peptideModifications.add(modification.getLocation() + "-" + cvParam.getAccession());
                            }
                        }
                        if (peptideModifications.size() > 0) {
                            pepMods = StringUtils.join(peptideModifications, ", ");
                        }
                        if (!chrom.equalsIgnoreCase("null")) {
                            String[] starts = blockStarts.split(",");
                            String[] sizes = blockSizes.split(",");
                            chromend = "" + (Integer.parseInt(chromstart) + Integer.parseInt(starts[starts.length-1]) + Integer.parseInt(sizes[sizes.length-1]));

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
                            final int TOTAL_EVIDENCES = peptide.getPeptideEvidenceList().size();
                            if (mzTabController.hasProteinAmbiguityGroup()) {
                                final int RANGE = 110;
                                int difference = 0;
                                double zeroRange = 1.00;
                                double oneRange = 0.5;
                                double twoRange = 0.25;
                                int constant = 0; /*
                                <167	1
                                167-277	2-4
                                278-388	5-7
                                389-499	8-10
                                500-610	11-13
                                611-722	14-16
                                723-833	17-19
                                834-944	20-22
                                >944	>22     */
                                if (TOTAL_EVIDENCES==1) {
                                    constant = 166;
                                } else if (TOTAL_EVIDENCES>1 && TOTAL_EVIDENCES<5) {
                                    difference = 4-TOTAL_EVIDENCES;
                                    constant = 167;
                                } else if (TOTAL_EVIDENCES>4 && TOTAL_EVIDENCES<8) {
                                    difference = 7-TOTAL_EVIDENCES;
                                    constant = 278;
                                } else if (TOTAL_EVIDENCES>7 && TOTAL_EVIDENCES<11) {
                                    difference = 10-TOTAL_EVIDENCES;
                                    constant = 389;
                                } else if (TOTAL_EVIDENCES>10 && TOTAL_EVIDENCES<14) {
                                    difference = 13-TOTAL_EVIDENCES;
                                    constant = 500;
                                } else if (TOTAL_EVIDENCES>13 && TOTAL_EVIDENCES<17) {
                                    difference = 16-TOTAL_EVIDENCES;
                                    constant = 611;
                                } else if (TOTAL_EVIDENCES>16 && TOTAL_EVIDENCES<20) {
                                    difference = 19-TOTAL_EVIDENCES;
                                    constant = 723;
                                } else if (TOTAL_EVIDENCES>19 && TOTAL_EVIDENCES<23) {
                                    difference = 22-TOTAL_EVIDENCES;
                                    constant = 834;
                                } else if (TOTAL_EVIDENCES>22) {
                                    constant = 1000;
                                }
                                double chosenComponent;
                                switch (difference) {
                                    case 0:
                                        chosenComponent = zeroRange;
                                        break;
                                    case 1:
                                        chosenComponent = oneRange;
                                        break;
                                    case 2:
                                        chosenComponent = twoRange;
                                        break;
                                    default:
                                        chosenComponent = 0.0;
                                        break;
                                }
                                stringBuilder.append(new Double(Math.floor((chosenComponent * RANGE) + constant)).intValue());
                                // score, according to evidence
                            } else {
                                stringBuilder.append(1000);
                            } // score, no PSM group : 1000 TODO test
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
                            if (!duplicatePeptideSequences.contains(peptide.getSequence())) {
                                stringBuilder.append("unique");
                            } else {
                                HashSet<ProteinGroup> groups = getProtgeinGroups(proteinID, proteinGroupHashMap);
                                int evidenceOnThisLoci = 0, evidenceOnOtherLoci = 0;
                                Collection<Comparable> proteinIDs = new HashSet<>();
                                if (groups.size()>0) {
                                    for (ProteinGroup group : groups) {
                                            proteinIDs.addAll(group.getProteinIds());
                                    }
                                } else {
                                    proteinIDs = mzTabController.getProteinIds();
                                }
                                for (Comparable proteinIdToCheck : proteinIDs) {
                                    List<Peptide> peptidesList = mzTabController.getProteinById(proteinIdToCheck).getPeptides();
                                    for (Peptide peptideToCheck : peptidesList) {
                                        if (!peptideToCheck.equals(peptide) && peptideToCheck.getSequence().equals(peptide.getSequence())) {
                                            List<CvParam> cvParams = peptideToCheck.getPeptideEvidence().getCvParams();
                                            Map<String, CvParam> cvps = new HashMap<>();
                                            for (CvParam cvp : cvParams) {
                                                cvps.put(cvp.getAccession(), cvp);
                                            }
                                            if (hasChromCvps(peptideToCheck.getPeptideEvidence().getCvParams())) {
                                                String cvpChrom = cvps.get("MS:1002637").getValue();
                                                String cvpStart = checkFixEndComma(cvps.get("MS:1002643").getValue()).split(",")[0];
                                                String cvpEnd = cvps.get("MS:1002640").getValue();
                                                if (cvpChrom.equalsIgnoreCase(chrom) && (
                                                        ((Integer.parseInt(cvpStart)-18)<=Integer.parseInt(chromstart) && (Integer.parseInt(cvpStart)+18>=Integer.parseInt(chromstart)))
                                                      && (Integer.parseInt(cvpEnd)-18)<=Integer.parseInt(chromend) &&  (Integer.parseInt(cvpEnd)+18>=(Integer.parseInt(chromend))))) {
                                                    evidenceOnThisLoci++;
                                                } else {
                                                    evidenceOnOtherLoci++;
                                                }
                                            }

                                        }
                                    }
                                 }
                                final int RANGE = 4;
                                if (evidenceOnOtherLoci==0 && evidenceOnThisLoci==0) {
                                    stringBuilder.append("not-unique[conflict]");
                                } else {
                                    if ((evidenceOnOtherLoci - evidenceOnThisLoci) > RANGE) {
                                        stringBuilder.append("not-unique[subset]");
                                    } else if ((evidenceOnOtherLoci - evidenceOnThisLoci) < RANGE){
                                        stringBuilder.append("not-unique[same-set]");
                                    } else {
                                        stringBuilder.append("not-unique[unknown]");
                                    }
                                }
                            } // peptide uniqueness
                            stringBuilder.append('\t');
                            stringBuilder.append(buildVersion); // buildVersion
                            stringBuilder.append('\t');
                            if (psmScore==null || psmScore.isEmpty() || psmScore.equalsIgnoreCase("null")) {
                                for (CvParam cvp :  protein.getCvParams()) {
                                    if (cvp.getAccession().equalsIgnoreCase("MS:1002235")) {
                                        psmScore = cvp.getValue();
                                        if (psmScore==null || psmScore.isEmpty()) {
                                            psmScore = "null";
                                        }
                                        break;
                                    }
                                }
                            }
                            stringBuilder.append(psmScore); // psmScore
                            stringBuilder.append('\t');
                            stringBuilder.append(fdrScore); // fdr
                            stringBuilder.append('\t');
                            stringBuilder.append(pepMods); // peptide location modifications
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "null" : peptide.getPrecursorCharge()); // charge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "null" : peptide.getSpectrumIdentification().getExperimentalMassToCharge()); // expMassToCharge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "null" : peptide.getSpectrumIdentification().getCalculatedMassToCharge()); // calcMassToCharge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "null" : peptide.getSpectrumIdentification().getRank()); // PSM rank (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            String datasetID;
                            if (!projectAccession.isEmpty()) {
                                datasetID = proteoAnnotatorReporcessed ? projectAccession + "_proteoannotator_reprocessed"
                                                                       : projectAccession;
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
                                projectUri = proteoAnnotatorReporcessed ?
                                        "http://ftp.pride.ebi.ac.uk/pride/data/proteogenomics/latest/proteoannotator/reprocessed_data/" + projectAccession :
                                        "http://www.ebi.ac.uk/pride/archive/projects/" + projectAccession;
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

    private String checkFixEndComma(String input) {
        return (input.charAt(input.length()-1)==',' ? input.substring(0, input.length()-1) : input);
    }

    private HashSet<ProteinGroup> getProtgeinGroups(Comparable proteinID, HashMap<Comparable, ProteinGroup> proteinGroupHashMap) {
        HashSet<ProteinGroup> result = new HashSet<>();
        Collection<Comparable> keys = proteinGroupHashMap.keySet();
        for (Comparable groupID : keys) {
            ProteinGroup proteinGroup = proteinGroupHashMap.get(groupID);
            if (proteinGroup.getProteinIds().contains(proteinID)) {
                result.add(proteinGroup);
            }
        }
        return result;
    }

    private boolean hasChromCvps(Collection<CvParam> cvParams) {
        Map<String, CvParam> cvps = new HashMap<>();
        for (CvParam cvp : cvParams) {
            cvps.put(cvp.getAccession(), cvp);
        }
        return cvps.containsKey("MS:1002637") && cvps.containsKey("MS:1002643") && cvps.containsKey("MS:1002640");
    }
    
    public File sortProBed(File inputProBed, File inputChromSizes) throws IOException, InterruptedException {
        File result = null;
        if (!System.getProperty("os.name").startsWith("Windows")) {
            Path chromPath = inputChromSizes.toPath();
            List<String> lines = Files.readAllLines(chromPath, Charset.defaultCharset());
            Set chromNames = new TreeSet();
            for (String line : lines) {
                String[] chromLine = line.split("\t");
                chromNames.add(chromLine[0]);
            }

            File temp = new File(inputProBed.getPath() + ".tmp");
            temp.createNewFile();
            BufferedWriter writer = new BufferedWriter(new FileWriter(temp));
            BufferedReader reader = new BufferedReader(new FileReader(inputProBed));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] bedLine = line.split("\t");
                if (chromNames.contains(bedLine[0])) {
                    writer.write(line, 0, line.length());
                    writer.newLine();
                } // else: don't write
            }
            writer.close();
            System.out.println("Sorting temp bed file to new bed file. PB command: " +
                    "sort -k1,1 -k2,2n " + temp.getAbsoluteFile().getPath() + " > " + inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed");
            Runtime rt = Runtime.getRuntime();
            Process p = rt.exec("sort -k1,1 -k2,2n " + temp.getAbsoluteFile().getPath() + " > " + inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed");
            //Process p = new ProcessBuilder(sortScript.getAbsoluteFile().getPath(), temp.getAbsoluteFile().getPath(), inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed").start();
            BufferedReader in = new BufferedReader(new InputStreamReader(p.getInputStream()));
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(p.getErrorStream()));
            String scriptOutput;
            while ((scriptOutput = in.readLine()) != null) {
                System.out.println(scriptOutput);
            }
            System.out.println("Error stream text:");
            while ((scriptOutput = errorStream.readLine()) != null) {
                System.out.println(scriptOutput);
            }
            p.waitFor();
            in.close();
            errorStream.close();
            temp.delete();
            result =  new File(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed");
        }
        return result;
    }

    public File convertProBedToBigBed(File aSQL, File bedToBigBed, File inputProBed, File inputChromSizes) throws IOException, InterruptedException{
        File result = null;
        if (!System.getProperty("os.name").startsWith("Windows")) {
            //System.out.println(convertBigBed.getAbsoluteFile().getPath() + " " +
            System.out.println(
                    bedToBigBed.getAbsoluteFile().getPath() + " " +
                    aSQL.getAbsoluteFile().getPath() + " " +
                    "bed12+13" + " " +
                            inputProBed + " " +
                    inputChromSizes.getAbsoluteFile().getPath() + " " +
                    inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + ".bb");
            Runtime rt = Runtime.getRuntime();
            Process bigbed_proc = rt.exec(
                            bedToBigBed.getAbsoluteFile().getPath() +
                            " -as=\"" + aSQL.getAbsoluteFile().getPath() + "\" " +
                            "-type=\"bed12+13\" "  +
                            "-tab " +
                            inputProBed + " " +
                            inputChromSizes.getAbsoluteFile().getPath() + " " +
                            inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + ".bb");
            /* Process bigbed_proc = new ProcessBuilder(convertBigBedScript.getAbsoluteFile().getPath(),
                    aSQL.getAbsoluteFile().getPath(),
                    bedToBigBed.getAbsoluteFile().getPath(),
                    "bed12+13",
                    inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed",
                    inputChromSizes.getAbsoluteFile().getPath(),
                    inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + ".bb")
                    .start();*/
            BufferedReader in = new BufferedReader(new InputStreamReader(bigbed_proc.getInputStream()));
            BufferedReader errorStream = new BufferedReader(new InputStreamReader(bigbed_proc.getErrorStream()));
            String scriptOutput;
            while ((scriptOutput = in.readLine()) != null) {
                System.out.println(scriptOutput);
            }
            System.out.println("Error stream text:");
            while ((scriptOutput = errorStream.readLine()) != null) {
                System.out.println(scriptOutput);
            }
            bigbed_proc.waitFor();
            in.close();
            errorStream.close();
            inputProBed.delete();

            BufferedReader reader = Files.newBufferedReader(Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed"), Charset.defaultCharset() );
            BufferedWriter writer = Files.newBufferedWriter(Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed_temp"), Charset.defaultCharset());
            writer.write("# proBed-version\t" + PROBED_VERSION);
            writer.newLine();
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line, 0, line.length());
                writer.newLine();
            }
            reader.close();
            writer.close();
            Files.delete(Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed"));
            Files.move(Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed_temp")
                    , Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed"));
            System.out.println("Added proBed version number to the sorted proBed File.");
            result =  Paths.get(inputProBed.getAbsoluteFile().getParentFile().getCanonicalPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed").toFile();
        }
        return result;
    }

}
