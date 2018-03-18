package uk.ac.ebi.pride.utilities.data.controller.impl.Transformer;

import uk.ac.ebi.pride.utilities.data.lightModel.SpectraData;

/**
 * @author Suresh Hewapathirana
 */
public class SimpleToJmzIdentMLTransformer {

    public static uk.ac.ebi.jmzidml.model.mzidml.SpectraData convertSpectraDataToJmzidml(SpectraData spectraData) {

        uk.ac.ebi.jmzidml.model.mzidml.SpectraData spectraDataJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.SpectraData();
        uk.ac.ebi.jmzidml.model.mzidml.SpectrumIDFormat spectrumIDFormatJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.SpectrumIDFormat();
        uk.ac.ebi.jmzidml.model.mzidml.CvParam CVParamJmzidml = new uk.ac.ebi.jmzidml.model.mzidml.CvParam();

        CVParamJmzidml.setAccession(spectraData.getSpectrumIDFormat().getCvParam().getAccession());
        spectrumIDFormatJmzidml.setCvParam(CVParamJmzidml);

        spectraDataJmzidml.setSpectrumIDFormat(spectrumIDFormatJmzidml);
        spectraDataJmzidml.setLocation(spectraData.getLocation());
        spectraDataJmzidml.setId(spectraData.getId());

        return spectraDataJmzidml;
    }
}
