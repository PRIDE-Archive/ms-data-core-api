package uk.ac.ebi.pride.utilities.data.exporters;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.core.*;
import uk.ac.ebi.pride.utilities.data.core.Modification;
import uk.ac.ebi.pride.utilities.data.core.Peptide;
import uk.ac.ebi.pride.utilities.data.core.Protein;

import java.io.*;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;


/**
 * Class to convert MzTab files to Bed files. Requires chromosome information to be present.
 *
 * @author Tobias Ternent tobias@ebi.ac.uk
 */
public class MzTabBedConverter {

    protected static Logger logger = LoggerFactory.getLogger(MzTabBedConverter.class);

    private MzTabControllerImpl mzTabController;
    private String projectAccession;
    private String assayAccession;
    private boolean proteoAnnotatorReporcessed;
    private final static String PROBED_VERSION = "1.0";

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

        HashMap<String, ArrayList<Locus>> peptidesLoci = new HashMap<>();
        HashMap<PeptidePtmKey, Integer> psmCount = new HashMap<>();
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : mzTabController.getProteinById(proteinID).getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptideEvidence)) {
                        evidences.add(peptideEvidence);
                        if (hasChromCvps(peptideEvidence.getCvParams())) {
                            Locus locus = new Locus();
                            String blockStarts = "", blockSizes = "";
                            for (CvParam cvParam : peptideEvidence.getCvParams()) {
                                switch (cvParam.getAccession()) {
                                    case ("MS:1002644"):
                                        locus.setGeneBuild(FilenameUtils.removeExtension(cvParam.getValue()));
                                        break;
                                    case ("MS:1002637"):
                                        locus.setChromosome(cvParam.getValue());
                                        break;
                                    case ("MS:1002643"):
                                        String[] starts = checkFixEndComma(cvParam.getValue()).split(",");
                                        String chromstart = starts[0];
                                        for (int i=0; i<starts.length; i++) {
                                            starts[i] = "" + (Integer.parseInt(starts[i]) - Integer.parseInt(chromstart));
                                        }
                                        blockStarts = StringUtils.join(starts, ",");
                                        locus.setStartLocation(chromstart);
                                        break;
                                    case ("MS:1002642"):
                                        blockSizes = checkFixEndComma(cvParam.getValue());
                                        break;
                                    default:
                                        break;
                                }
                            }
                            String[] starts = blockStarts.split(",");
                            String[] sizes = blockSizes.split(",");
                            locus.setEndLocation("" + (Integer.parseInt(locus.getStartLocation()) + Integer.parseInt(starts[starts.length-1]) + Integer.parseInt(sizes[sizes.length-1])));
                            if (peptidesLoci.containsKey(peptideEvidence.getPeptideSequence().getSequence())) {
                                peptidesLoci.get(peptideEvidence.getPeptideSequence().getSequence()).add(locus);
                            } else {
                                ArrayList<Locus> lociList = new ArrayList<>();
                                lociList.add(locus);
                                peptidesLoci.put(peptideEvidence.getPeptideSequence().getSequence(), lociList);
                            }
                            ArrayList<String> peptideModifications = new ArrayList<>();
                            String pepMods = ".";
                            for (Modification modification : peptideEvidence.getPeptideSequence().getModifications()) {
                                peptideModifications.addAll(modification.getCvParams().stream().map(cvParam -> modification.getLocation() + "-" + cvParam.getAccession()).collect(Collectors.toList()));
                            }
                            if (peptideModifications.size() > 0) {
                                pepMods = StringUtils.join(peptideModifications, ", ");
                            }
                           PeptidePtmKey key = new PeptidePtmKey(peptideEvidence.getPeptideSequence().getSequence(), pepMods);
                            if (psmCount.containsKey(key)) {
                                psmCount.put(key, psmCount.get(key)+1);
                            } else {
                                psmCount.put(key, 1);
                            }
                        }
                    }
                }
            }
        }
        for (Comparable proteinID : mzTabController.getProteinIds()) {
            Protein protein = mzTabController.getProteinById(proteinID);
            ArrayList<PeptideEvidence> evidences = new ArrayList<>();
            for (Peptide peptide : protein.getPeptides()) {
                for (PeptideEvidence peptideEvidence : peptide.getPeptideEvidenceList()) {
                    if (!evidences.contains(peptideEvidence)) {
                        evidences.add(peptideEvidence);
                        String chrom = ".", chromstart = ".", chromend = ".", strand = ".", pepMods = ".",
                                psmScore = ".", fdrScore = ".",
                                blockStarts = ".", blockSizes = ".", blockCount="1", buildVersion=".";
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
                                    buildVersion = FilenameUtils.removeExtension(cvParam.getValue());
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
                            peptideModifications.addAll(modification.getCvParams().stream().map(cvParam -> modification.getLocation() + "-" + cvParam.getAccession()).collect(Collectors.toList()));
                        }
                        if (peptideModifications.size() > 0) {
                            pepMods = StringUtils.join(peptideModifications, ", ");
                        }
                        if (!chrom.equalsIgnoreCase(".")) {
                            String[] starts = blockStarts.split(",");
                            String[] sizes = blockSizes.split(",");
                            if (".".equalsIgnoreCase(chromstart) ||
                                starts.length<1 || ".".equalsIgnoreCase(starts[0]) ||
                                sizes.length<1 || ".".equalsIgnoreCase(sizes[0])) {
                                logger.info("Skipping peptide with bad chromosome information: " + peptideEvidence.getPeptideSequence());
                                stringBuilder.setLength(0);
                                continue; // peptide with no chrom information
                            }
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
                            PeptidePtmKey key = new PeptidePtmKey(peptideEvidence.getPeptideSequence().getSequence(), pepMods);
                            final int TOTAL_EVIDENCES = psmCount.get(key);
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

                            ArrayList<Locus> lociOfPeptide = peptidesLoci.get(peptideEvidence.getPeptideSequence().getSequence());
                            final int LOCI_THRESHOLD = 1;
                            if (lociOfPeptide.size()==1) {
                                stringBuilder.append("unique");
                            } else {
                                HashSet<String> geneBuilds = new HashSet<>();
                                lociOfPeptide.parallelStream().forEach(locus -> geneBuilds.add(locus.getGeneBuild()));
                                int currentLoci = 0;
                                int otherLoci = 0;
                                final Locus CURRENT_LOCUS = new Locus(buildVersion, chrom, chromstart, chromend);
                                for (Locus locus : lociOfPeptide) {
                                    if (CURRENT_LOCUS.equals(locus)) {
                                        currentLoci++;
                                    } else {
                                        otherLoci++;
                                    }
                                }
                                if (otherLoci == 0) {
                                    stringBuilder.append("unique");
                                } else {
                                    if (geneBuilds.size() == 1) {
                                        if ((currentLoci - otherLoci) > LOCI_THRESHOLD) {
                                            stringBuilder.append("not-unique[super-set]");
                                        } else if (currentLoci == otherLoci) {
                                            stringBuilder.append("not-unique[same-set]");
                                        } else if ((currentLoci - otherLoci) < LOCI_THRESHOLD) {
                                            stringBuilder.append("not-unique[sub-set]");
                                        } else {
                                            stringBuilder.append("not-unique[conflict]");
                                        }
                                    } else if (geneBuilds.size() > 1) {
                                        stringBuilder.append("not-unique[unknown]");
                                    }
                                }
                            }// peptide uniqueness
                            stringBuilder.append('\t');
                            stringBuilder.append(buildVersion); // buildVersion
                            stringBuilder.append('\t');
                            if (psmScore==null || psmScore.isEmpty() || psmScore.equalsIgnoreCase(".")) {
                                for (CvParam cvp :  protein.getCvParams()) {
                                    if (cvp.getAccession().equalsIgnoreCase("MS:1002235")) {
                                        psmScore = cvp.getValue();
                                        if (psmScore==null || psmScore.isEmpty()) {
                                            psmScore = ".";
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
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "." : peptide.getPrecursorCharge()); // charge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "." : peptide.getSpectrumIdentification().getExperimentalMassToCharge()); // expMassToCharge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "." : peptide.getSpectrumIdentification().getCalculatedMassToCharge()); // calcMassToCharge (null if row = group of PSMs)
                            stringBuilder.append('\t');
                            stringBuilder.append(mzTabController.hasProteinAmbiguityGroup() ? "." : peptide.getSpectrumIdentification().getRank()); // PSM rank (null if row = group of PSMs)
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
                                projectUri = ".";
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

    public static File sortProBed(File inputProBed, File inputChromSizes) throws IOException, InterruptedException {
        logger.info("Input unsorted BED file: " + inputProBed.getPath());
        logger.info("Input chrom  file: " + inputChromSizes.getPath());
        File tempSortedBedFile =  new File(inputProBed.getPath() + ".sorted_tmp");
        File sortedProBed = new File(inputProBed.getParentFile().getPath() + File.separator + FilenameUtils.getBaseName(inputProBed.getName()) + "_sorted.pro.bed");
        tempSortedBedFile.createNewFile();
        sortedProBed.createNewFile();
        logger.info("Sorting BED file: " + inputProBed.getPath());
        logger.info("Writing to sorted pro bed file, filtered by chrom names: " + sortedProBed.getPath());
        List<String> lines = Files.readAllLines(inputChromSizes.toPath(), Charset.defaultCharset());
        Set chromNames = new TreeSet();
        for (String line : lines) {
            String[] chromLine = line.split("\t");
            chromNames.add(chromLine[0]);
        }
        List<String> sortedLines;

        try (Stream<String> stream = Files.lines(inputProBed.toPath())) {
            sortedLines = stream.sorted((o1, o2) -> {
                String firstKey1 = o1.substring(0, o1.indexOf('\t'));
                String firstKey2 = o2.substring(0, o2.indexOf('\t'));
                int aComp = firstKey1.compareTo(firstKey2);
                if (aComp != 0) {
                    return aComp; //1st key by 1st column (chrom name) as String
                } else {
                    String secondKey1 = o1.substring(StringUtils.ordinalIndexOf(o1, "\t", 1)+1, StringUtils.ordinalIndexOf(o1, "\t", 2));
                    String secondKey2 = o2.substring(StringUtils.ordinalIndexOf(o2, "\t", 1)+1, StringUtils.ordinalIndexOf(o2, "\t", 2));
                    return Integer.parseInt(secondKey1) - Integer.parseInt(secondKey2); //2nd key by 2nd column (chrom start) as int
                }
            }).collect(Collectors.toList());
            BufferedWriter writerTemp = Files.newBufferedWriter(tempSortedBedFile.toPath());
            BufferedWriter writerProBed = Files.newBufferedWriter(sortedProBed.toPath());
            sortedLines.stream().forEachOrdered(s -> {
                try {
                    writerTemp.write(s);
                    writerTemp.newLine();
                    if (chromNames.contains(s.substring(0, s.indexOf('\t')))) {
                        writerProBed.write(s);
                        writerProBed.newLine();
                    } else {
                        logger.debug("Chromosome not present in chrom txt file:" + s);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
            writerTemp.close();
            writerProBed.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Files.move(tempSortedBedFile.toPath(), inputProBed.toPath(), REPLACE_EXISTING);
        logger.info("Sorted new proBed fle: " + sortedProBed.getAbsolutePath());
        return sortedProBed;
    }

    public static File convertProBedToBigBed(File aSQL, String bedColumnsType, File sortedProBed, File inputChromSizes, File bigBedConverter) throws IOException, InterruptedException, URISyntaxException{
       logger.info("convertProBedToBigBed: " + aSQL.getAbsolutePath() +" , " + sortedProBed.getAbsolutePath() + ", " + inputChromSizes.getAbsolutePath());
        File result;
        final String OS = System.getProperty("os.name").toLowerCase();
        logger.info("OS version: " + OS);
        if (!OS.contains("win")) {
            InputStream inputStream = MzTabBedConverter.class.getClassLoader().getResourceAsStream("bedBigBed.sh");
            if (inputStream == null) {
                logger.error("no file for bedBigBed.sh found!");
                throw new IOException("no file for bedBigBed.sh found!");
            }
            File bedToBigBed = File.createTempFile("bigBedHelper", ".sh");
            FileUtils.copyInputStreamToFile(inputStream, bedToBigBed);
            logger.info("Created temp bedToBigBed script: " + bedToBigBed.toPath());
            bedToBigBed.setExecutable(true);
            result = new File(sortedProBed.getParentFile().getPath() + File.separator + FilenameUtils.getBaseName(sortedProBed.getName()) + ".bb");
            logger.info("command to run: \n" +
                    bigBedConverter.getPath() + ", " +
                            "-as=\"" + aSQL.getPath() + "\"" + ", " +
                            "-type=\"" + bedColumnsType + "\"" + ", " +
                            "-tab"+ ", " +
                    sortedProBed.getPath()+ ", " +
                    inputChromSizes.getPath()+ ", " +
                    result.getPath());
            Process bigbed_proc = new ProcessBuilder(
                    bedToBigBed.getAbsoluteFile().getAbsolutePath(),
                    bigBedConverter.getAbsoluteFile().getAbsolutePath(),
                    aSQL.getAbsoluteFile().getAbsolutePath(),
                    bedColumnsType,
                    sortedProBed.getAbsolutePath(),
                    inputChromSizes.getAbsoluteFile().getAbsolutePath(),
                    result.getPath())
                    .redirectErrorStream(true)
                    .start();
            BufferedReader in = new BufferedReader(new InputStreamReader(bigbed_proc.getInputStream()));
            String scriptOutput;
            while ((scriptOutput = in.readLine()) != null) {
                logger.info(scriptOutput);
                logger.info("Output System message: " + scriptOutput);
            }
            bigbed_proc.waitFor();
            in.close();
            logger.info("Finished generating bigBed file: " + result.getPath());
            File sortedTempFile = new File(sortedProBed.getParentFile().getPath() + File.separator + FilenameUtils.getBaseName(sortedProBed.getName()) + "_temp");
            BufferedReader reader = Files.newBufferedReader(sortedProBed.toPath(), Charset.defaultCharset());
            BufferedWriter writer = Files.newBufferedWriter(sortedTempFile.toPath(), Charset.defaultCharset());
            writer.write("# proBed-version\t" + PROBED_VERSION);
            writer.newLine();
            String line;
            while ((line = reader.readLine()) != null) {
                writer.write(line, 0, line.length());
                writer.newLine();
            }
            reader.close();
            writer.close();
            sortedProBed.delete();
            Files.move(sortedTempFile.toPath(), sortedProBed.toPath());
            logger.info("Added proBed version number to the sorted proBed File.");
        } else {
            final String MESSAGE = "Unable to convert to bigBed on the Windows platform.";
            logger.error(MESSAGE);
            throw new IOException(MESSAGE);
        }
        logger.info("returning: " + result.getAbsolutePath());
        return result;
    }

    public static void createAsql(String name, String path) throws IOException{
        String text = "table " + name.replace(' ', '_') + "\n" +
                "\"" + name + "\"\n" +
                "(\n" +
                "string  chrom;          \"The reference sequence chromosome.\"\n" +
                "uint    chromStart;     \"The position of the first DNA base.\"\n" +
                "uint    chromEnd;       \"The position of the last DNA base.\"\n" +
                "string  name;           \"Unique name for the BED line.\"\n" +
                "uint    score; \"A score used for shading by visualisation software.\"\n" +
                "char[1]  strand;                \"The strand.\"\n" +
                "uint    thickStart ;     \"Start position of feature on chromosome.\"\n" +
                "uint    thickEnd ;       \"End position of feature on chromosome.\"\n" +
                "uint    reserved  ; \"Reserved.\" \n" +
                "int  blockCount ; \"The number of blocks (exons) in the BED line.\"\n" +
                "int[blockCount] blockSizes ; \"A comma-separated list of the block sizes.\"\n" +
                "int[blockCount] chromStarts ; \"A comma-separated list of block starts.\"\n" +
                "string  proteinAccession ; \"The accession number of the protein.\"\n" +
                "string  peptideSequence; \"The peptide sequence.\"\n" +
                "string  uniqueness; \"The uniqueness of the peptide in the context of the genome sequence.\"\n" +
                "string  genomeReferenceVersion ; \"The genome reference version number\"\n" +
                "string  psmScore; \"One representative PSM score.\"\n" +
                "string  fdr; \"A cross-platform measure of the likelihood of the identification being incorrect.\"\n" +
                "string  modifications; \"Semicolon-separated list of modifications identified on the peptide.”\n" +
                "string  charge; \"The value of the charge.\"\n" +
                "string  expMassToCharge; \"The value of the experimental mass to charge.\"\n" +
                "string  calcMassToCharge; \"The value of the calculated mass to charge.\"\n" +
                "string  datasetID;  \"A unique identifier or name for the data set.\"\n" +
                "string  psmRank;  \"The rank of the PSM.\"\n" +
                "string  uri; \"A URI pointing to the file's source data.\"\n" +
                ")";
        Files.write(Paths.get(path), text.getBytes());
        logger.info("Finished creating new aSQL file: " + path);
    }

}

class Locus {
    private String geneBuild;
    private String chromosome;
    private String startLocation;
    private String endLocation;

    /**
     * Default constructor for a Loci object.
     */
    Locus() {
    }

    /**
     * Constructor for a Loci object, with all the required information.
     *
     * @param geneBuild the gene build version
     * @param chromosome the chromsome of the Lccus
     * @param startLocation the start position of the Locus
     * @param endLocation the end position of the Locus
     */
    Locus(String geneBuild, String chromosome, String startLocation, String endLocation) {
        this.geneBuild = geneBuild;
        this.chromosome = chromosome;
        this.startLocation = startLocation;
        this.endLocation = endLocation;
    }

    /**
     * Sets new endLocation.
     *
     * @param endLocation New value of endLocation.
     */
    void setEndLocation(String endLocation) {
        this.endLocation = endLocation;
    }

    /**
     * Sets new geneBuild.
     *
     * @param geneBuild New value of geneBuild.
     */
    void setGeneBuild(String geneBuild) {
        this.geneBuild = geneBuild;
    }

    /**
     * Sets new startLocation.
     *
     * @param startLocation New value of startLocations.
     */
    void setStartLocation(String startLocation) {
        this.startLocation = startLocation;
    }

    /**
     * Gets startLocations.
     *
     * @return Value of startLocations.
     */
    String getStartLocation() {
        return startLocation;
    }

    /**
     * Gets chromosome.
     *
     * @return Value of chromosome.
     */
    String getChromosome() {
        return chromosome;
    }

    /**
     * Gets endLocations.
     *
     * @return Value of endLocations.
     */
    String getEndlocation() {
        return endLocation;
    }

    /**
     * Sets new chromosome.
     *
     * @param chromosome New value of chromosome.
     */
    void setChromosome(String chromosome) {
        this.chromosome = chromosome;
    }

    /**
     * Gets geneBuild.
     *
     * @return Value of geneBuild.
     */
    String getGeneBuild() {
        return geneBuild;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + geneBuild.hashCode();
        result = 31 * result + chromosome.hashCode();
        result = 31 * result + startLocation.hashCode();
        result = 31 * result + endLocation.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof Locus)) {
            return false;
        }
        Locus locus = (Locus) o;
        return locus.chromosome.equals(chromosome) &&
            locus.startLocation.equals(startLocation) &&
            locus.endLocation.equals(endLocation);
    }
}

class PeptidePtmKey {
    private String sequence;
    private String mods;

    PeptidePtmKey() {
    }

    PeptidePtmKey(String sequence, String mods) {
        this.sequence = sequence;
        this.mods = mods;
    }

    @Override
    public int hashCode() {
        int result = 17;
        result = 31 * result + sequence.hashCode();
        result = 31 * result + mods.hashCode();
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (!(o instanceof PeptidePtmKey)) {
            return false;
        }
        PeptidePtmKey peptidePtmKey = (PeptidePtmKey) o;
        return peptidePtmKey.sequence.equals(sequence) &&
            peptidePtmKey.mods.equals(mods);
    }

    /**
     * Sets new sequence.
     *
     * @param sequence New value of sequence.
     */
    void setSequence(String sequence) {
        this.sequence = sequence;
    }

    /**
     * Gets mods.
     *
     * @return Value of mods.
     */
    String getMods() {
        return mods;
    }

    /**
     * Gets sequence.
     *
     * @return Value of sequence.
     */
    String getSequence() {
        return sequence;
    }

    /**
     * Sets new mods.
     *
     * @param mods New value of mods.
     */
    void setMods(String mods) {
        this.mods = mods;
    }
}
