package uk.ac.ebi.pride.utilities.data.utils;


import org.junit.Before;
import org.junit.Test;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.net.URL;
import java.util.List;

public class MzIdentMLUtilsTest {

    private File mzIdentMLFile = null;

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(MzIdentMLUtils.class);

    @Before
    public void setUp() throws Exception {
//        URL mzIdentMLFileURL = MzIdentMLUtilsTest.class.getClassLoader().getResource("carb.mzid"); // 1.1.0 version
        URL mzIdentMLFileURL = MzIdentMLUtilsTest.class.getClassLoader().getResource("OpenxQuest_example_1_2.mzid"); // 1.2.0 version
        if (mzIdentMLFileURL == null) {
            throw new IllegalStateException("no file for input found!");
        }
        mzIdentMLFile = new File(mzIdentMLFileURL.toURI());
    }

    @Test
    public void testValidateMzIdentMLSchema() throws Exception {
        List<String> errors = MzIdentMLUtils.validateMzIdentMLSchema(mzIdentMLFile);
        for(String error: errors){
            logger.error(error);
        }
    }
}