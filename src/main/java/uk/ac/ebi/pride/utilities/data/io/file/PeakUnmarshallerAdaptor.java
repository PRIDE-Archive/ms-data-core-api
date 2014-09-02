package uk.ac.ebi.pride.utilities.data.io.file;

import uk.ac.ebi.pride.tools.jmzreader.JMzReader;
import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
//Todo: this about the possibility to move jmzReader to utilities

import java.util.List;

/**
 * Retrieve the information from different pure file formats using the JMzReader
 * library.
 *
 * @author ypriverol
 * Date: 3/15/12
 * Time: 10:30 PM
 */
public class PeakUnmarshallerAdaptor {

    private JMzReader unmarshaller = null;

    public PeakUnmarshallerAdaptor(JMzReader um) {
        this.unmarshaller = um;
    }

    public List<String> getSpectrumIds() {
        return unmarshaller.getSpectraIds();
    }

    public Spectrum getSpectrumById(String id) throws JMzReaderException{
        return unmarshaller.getSpectrumById(id);
    }


}
