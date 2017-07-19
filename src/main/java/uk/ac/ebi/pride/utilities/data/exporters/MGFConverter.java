package uk.ac.ebi.pride.utilities.data.exporters;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessController;
import uk.ac.ebi.pride.utilities.data.controller.DataAccessException;
import uk.ac.ebi.pride.utilities.data.core.ExperimentMetaData;
import uk.ac.ebi.pride.utilities.data.core.Spectrum;
import uk.ac.ebi.pride.utilities.data.utils.Constants;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * This class allow to export the DataAccessController spectra to MGF file
 * @author ypriverol, rwang
 */
public class MGFConverter {

    protected static Logger logger = LoggerFactory.getLogger(MGFConverter.class);

    private String outputFilePath;

    private DataAccessController controller;

    /**
     * Retrieve spectrum data in an MGF file format
     *
     * @param controller     DataAccessController
     * @param outputFilePath file to output the result.
     */
    public MGFConverter(DataAccessController controller, String outputFilePath) {
        this.outputFilePath = outputFilePath;
        this.controller = controller;
    }

    protected void convert() throws Exception {

        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new FileWriter(new File(outputFilePath)));

            ExperimentMetaData exp = controller.getExperimentMetaData();

            //------- Comment section -------

            // data source
            if (controller.getType().equals(DataAccessController.Type.XML_FILE)) {
                writer.println("# Data source: " + ((File) controller.getSource()).getAbsolutePath());
            }

            // accession if exist
            String acc = (exp.getId() !=null)?exp.getId().toString():null;
            if (acc != null) {
                writer.println("# Experiment accession: " + acc);
            }

            String title = exp.getName();
            if (title != null) {
                writer.println("# Experiment title: " + title);
            }

            // number of spectrum
            if (controller.hasSpectrum()) {
                writer.println("# Number of spectra: " + controller.getNumberOfSpectra());
            }

            // number of protein identifications
            if (controller.hasProtein()) {
                writer.println("# Number of protein identifications: " + controller.getNumberOfProteins());
            }

            // number of peptides
            if (controller.hasPeptide()) {
                writer.println("# Number of peptides: " + controller.getNumberOfPeptides());
            }

            //------- MGF content section -------
            for (Comparable spectrumId : controller.getSpectrumIds()) {
                Spectrum spectrum = controller.getSpectrumById(spectrumId);
                int msLevel = controller.getSpectrumMsLevel(spectrumId);
                if (msLevel == 2) {
                    writer.println("BEGIN IONS");
                    writer.println("TITLE=" + spectrumId);
                    writer.println("PEPMASS=" + controller.getSpectrumPrecursorMz(spectrumId));
                    // precursor charge
                    Integer charge = controller.getSpectrumPrecursorCharge(spectrumId);
                    if (charge != null) {
                        writer.println("CHARGE=" + charge + (charge >= 0 ? "+" : "-"));
                    }
                    //get both arrays
                    double[] mzBinaryArray = spectrum.getMzBinaryDataArray().getDoubleArray();
                    double[] intensityArray = spectrum.getIntensityBinaryDataArray().getDoubleArray();

                    for (int i = 0; i < mzBinaryArray.length; i++) {
                        writer.println(mzBinaryArray[i] + Constants.TAB + intensityArray[i]);
                    }
                    writer.println("END IONS" + Constants.LINE_SEPARATOR);

                    // this is important for cancelling
                    if (Thread.interrupted()) {
                        throw new InterruptedException();
                    }
                    writer.flush();
                }
            }
            writer.flush();
        } catch (DataAccessException e2) {
            String msg = "Failed to retrieve data from data source";
            logger.error(msg, e2);
        } catch (IOException e1) {
            String msg = "Failed to write data to the output file, please check you have the right permission";
            logger.error(msg, e1);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }
}
