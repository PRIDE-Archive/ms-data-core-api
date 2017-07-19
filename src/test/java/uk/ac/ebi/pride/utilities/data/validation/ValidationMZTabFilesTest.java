package uk.ac.ebi.pride.utilities.data.validation;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.pride.jmztab.model.MZTabFile;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileConverter;
import uk.ac.ebi.pride.jmztab.utils.MZTabFileParser;
import uk.ac.ebi.pride.jmztab.utils.errors.MZTabErrorList;

import java.io.File;
import java.io.IOException;
import java.net.URL;

/**
 * This code is licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * ==Overview==
 * <p>
 * This class
 * <p>
 * Created by ypriverol (ypriverol@gmail.com) on 05/07/2017.
 */
public class ValidationMZTabFilesTest {


    private File inputFile;

    @Before
    public void setUp() throws Exception {
        URL url = ValidationMZTabFilesTest.class.getClassLoader().getResource("example-wrong.mztab");
        if (url == null) {
            throw new IllegalStateException("no file for input found!");
        }
        inputFile = new File(url.toURI());
    }

    @Test
    public void mzTabValidation() throws IOException {

        MZTabErrorList mzTabErrorList = validateMzTab(inputFile);
        if (!mzTabErrorList.isEmpty()) {
            System.out.println("Error on validating mzTab file " + inputFile.getAbsolutePath() + ": " + mzTabErrorList.toString());
        }
    }

    private MZTabErrorList validateMzTab(File inputFile) throws IOException {

        MZTabFileParser mzTabFileParser = new MZTabFileParser(inputFile, System.out);

        MZTabFileConverter mzTabFileConverter = new MZTabFileConverter();

        MZTabFile mzTab = mzTabFileParser.getMZTabFile();

        mzTabFileConverter.check(mzTab);

        return mzTabFileConverter.getErrorList();
    }

}
