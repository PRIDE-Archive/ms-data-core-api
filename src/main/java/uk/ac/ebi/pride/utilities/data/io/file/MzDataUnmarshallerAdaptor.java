package uk.ac.ebi.pride.utilities.data.io.file;

import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzdata_parser.MzDataFile;
import uk.ac.ebi.pride.tools.mzdata_parser.mzdata.model.*;

import java.util.List;

/**
 * The UnmarshallerAdaptor for MzData files retrieve the mzData objects
 * from the mzdata_parser.
 *
 * @author Yasset Perez-Riverol
 * Date: 3/15/12
 * Time: 8:26 AM
 */
public class MzDataUnmarshallerAdaptor {

    private MzDataFile unmarshaller = null;

    public MzDataUnmarshallerAdaptor(MzDataFile um) {
        this.unmarshaller = um;
    }

    public List<String> getSpectrumIds() {
        return unmarshaller.getSpectraIds();
    }

    public Spectrum getSpectrumById(String id) throws JMzReaderException {
        return unmarshaller.getSpectrumById(id);
    }

    public List<Person> getPersonContacts() throws JMzReaderException {
        return unmarshaller.getDescription().getAdmin().getContact();
    }

    public Software getSoftware() throws JMzReaderException {
        return unmarshaller.getDescription().getDataProcessing().getSoftware();
    }

    public SourceFile getSourceFiles() throws JMzReaderException {
        return unmarshaller.getDescription().getAdmin().getSourceFile();
    }

    public InstrumentDescription getInstrument() throws JMzReaderException {
        return unmarshaller.getDescription().getInstrument();
    }

    public List<CvLookup> getCvLookups() throws JMzReaderException {
        return unmarshaller.getCvLookups();
    }

    public Admin getAdmin() throws JMzReaderException{
        return unmarshaller.getDescription().getAdmin();
    }

    public DataProcessing getDataProcessing() throws JMzReaderException{
        return unmarshaller.getDescription().getDataProcessing();
    }

    public String getIdMzData(){
        return unmarshaller.getMzDataAttributes().get("accessionNumber");
    }

    public String getVersionMzData(){
        return unmarshaller.getMzDataAttributes().get("version");
    }







}
