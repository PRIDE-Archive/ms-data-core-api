package uk.ac.ebi.pride.utilities.data.exporters;

import junit.framework.TestCase;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzIdentMLControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.ControllerImpl.MzTabControllerImpl;
import uk.ac.ebi.pride.utilities.data.controller.impl.PrideXmlControllerImplTest;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;

/**
 * MzTab to Bed Converter Test
 * @author tobias
 */
public class MzTabBedConverterTest {

    private File mzTabFile = null;
    private File mzIdentMLFile = null;

    @Before
    public void setUp() throws Exception {
        URL url = PrideXmlControllerImplTest.class.getClassLoader().getResource("PXD000764_34937_combined_fdr.mztab");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        mzTabFile = new File(url.toURI());

        url = MzTabPRIDEConverterTest.class.getClassLoader().getResource("PXD000764_34937_combined_fdr.mzid");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        mzIdentMLFile = new File(url.toURI());
    }

    @Test
    public void convertAnnotatedMzIdentMLMzTab() throws Exception {
        MzIdentMLControllerImpl mzIdentMLController = new MzIdentMLControllerImpl(mzIdentMLFile);
        MzIdentMLMzTabConverter mzTabconverter = new MzIdentMLMzTabConverter(mzIdentMLController);
        MZTabFile mzTabFile = mzTabconverter.getMZTabFile();
        MZTabFileConverter checker = new MZTabFileConverter();
        File temp = File.createTempFile("PXD000764_34937_mztab_test", ".tmp");
        TestCase.assertTrue("No errors reported during the conversion from mzIdentML to MzTab", checker.getErrorList().size() == 0);
        BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(new FileOutputStream(temp));
        mzTabFile.printMZTab(bufferedOutputStream);
        bufferedOutputStream.close();
        mzIdentMLController.close();
        temp.deleteOnExit();
    }

    @Test
    public void convertMzTabBed() throws Exception{
        MzTabControllerImpl mzTabController = new MzTabControllerImpl(mzTabFile);
        MzTabBedConverter mzTabBedConverter = new MzTabBedConverter(mzTabController);
        File temp = File.createTempFile("PXD000764_34937_20150326_bed_test", ".tmp");
        mzTabBedConverter.convert(temp);
        mzTabController.close();
        temp.deleteOnExit();
    }

    @After
    public void tearDown() throws Exception {

    }



}
