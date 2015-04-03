package uk.ac.ebi.pride.utilities.data.io.file;


import uk.ac.ebi.pride.tools.jmzreader.JMzReaderException;
import uk.ac.ebi.pride.tools.jmzreader.model.Spectrum;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLFile;
import uk.ac.ebi.pride.tools.mzxml_parser.MzXMLParsingException;
import uk.ac.ebi.pride.tools.mzxml_parser.mzxml.model.*;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.Duration;
import java.util.ArrayList;
import java.util.List;

/**
 * The MzXML Unmarshaller Adaptor provides the methods to retrieve the information form
 * the mzxml_parser library.
 * <p/>
 * @author Yasset Perez-Riverol
 * Date: 2/27/12
 * Time: 2:27 PM
 */
public class MzXmlUnmarshallerAdaptor {

    private MzXMLFile unmarshaller = null;

    public MzXmlUnmarshallerAdaptor(MzXMLFile um) {
        this.unmarshaller = um;
    }

    public List<String> getSpectrumIds() {
        return unmarshaller.getSpectraIds();
    }

    public Spectrum getSpectrumById(String id) throws JMzReaderException {
        return unmarshaller.getSpectrumById(id);
    }

    public List<Operator> getPersonContacts() throws MzXMLParsingException {
        List<Operator> operators = null;
        List<MsInstrument> msInstruments = unmarshaller.getMsInstrument();
        if (msInstruments != null && !msInstruments.isEmpty()) {
            operators = new ArrayList<Operator>();
            for (MsInstrument msInstrument : msInstruments) {
                operators.add(msInstrument.getOperator());
            }
        }
        return operators;
    }

    public List<Software> getSoftwares() throws MzXMLParsingException {
        List<Software> softwares = null;
        List<MsInstrument> msInstruments = unmarshaller.getMsInstrument();
        List<DataProcessing> dataProcessings = unmarshaller.getDataProcessing();
        if ((msInstruments != null && !msInstruments.isEmpty())) {
            softwares = new ArrayList<Software>();
            for (MsInstrument msInstrument : msInstruments) {
                softwares.add(msInstrument.getSoftware());
            }
        }
        if ((dataProcessings != null && dataProcessings.isEmpty())) {
            if (softwares == null) softwares = new ArrayList<Software>();
            for (DataProcessing dataProcessing : dataProcessings) {
                softwares.add(dataProcessing.getSoftware());
            }
        }
        return softwares;
    }

    public List<ParentFile> getParentFiles() throws MzXMLParsingException {
        return unmarshaller.getParentFile();
    }

    public List<MsInstrument> getMsInstruments() throws MzXMLParsingException {
        return unmarshaller.getMsInstrument();
    }

    public List<DataProcessing> getDataProcessing() throws MzXMLParsingException {
        return unmarshaller.getDataProcessing();
    }

    public Duration getStartDate() throws DatatypeConfigurationException {
        String startDateStr = unmarshaller.getRunAttributes().get("startTime");
        Duration duration = null;
        if (startDateStr != null) {
            duration = javax.xml.datatype.DatatypeFactory.newInstance().newDuration(startDateStr);
        }
        return duration;
    }

}