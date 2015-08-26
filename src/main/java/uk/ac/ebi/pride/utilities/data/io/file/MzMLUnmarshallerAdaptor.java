package uk.ac.ebi.pride.utilities.data.io.file;

//~--- non-JDK imports --------------------------------------------------------

import uk.ac.ebi.jmzml.model.mzml.*;
import uk.ac.ebi.jmzml.xml.io.MzMLUnmarshaller;

import java.io.File;
import java.util.*;

//~--- JDK imports ------------------------------------------------------------

/**
 * MzMLUnmarshallerHelper provides a list of convenient methods to access mzML files
 * <p/>
 * @author Rui Wang, Yasset Perez-Riverol
 * Date: 17-May-2010
 * Time: 10:37:45
 */
public class MzMLUnmarshallerAdaptor extends MzMLUnmarshaller {


    public MzMLUnmarshallerAdaptor(File mzMLFile) {
        super(mzMLFile);
    }

    public CVList getCVList() {
        return (CVList) unmarshalFromXpath("/mzML/cvList", CVList.class);
    }

    public FileDescription getFileDescription() {
        return (FileDescription) unmarshalFromXpath("/mzML/fileDescription", FileDescription.class);
    }

    public ReferenceableParamGroupList getReferenceableParamGroupList() {
        return (ReferenceableParamGroupList) unmarshalFromXpath("/mzML/referenceableParamGroupList", ReferenceableParamGroupList.class);
    }

    public SampleList getSampleList() {
        return (SampleList) unmarshalFromXpath("/mzML/sampleList", SampleList.class);
    }

    public SoftwareList getSoftwares() {
        return (SoftwareList) unmarshalFromXpath("/mzML/softwareList", SoftwareList.class);
    }

    public ScanSettingsList getScanSettingsList() {
        return (ScanSettingsList) unmarshalFromXpath("/mzML/scanSettingsList", ScanSettingsList.class);
    }

    public InstrumentConfigurationList getInstrumentConfigurationList() {
        return (InstrumentConfigurationList) unmarshalFromXpath("/mzML/instrumentConfigurationList", InstrumentConfigurationList.class);
    }

    public DataProcessingList getDataProcessingList() {
        return (DataProcessingList) unmarshalFromXpath("/mzML/dataProcessingList", DataProcessingList.class);
    }

    public Set<String> getSpectrumIds() {
        return getSpectrumIDs();
    }

    public Set<String> getChromatogramIds() {
        return getChromatogramIDs();
    }

    public Date getCreationDate() {
        Map<String, String> runAttributes = getSingleElementAttributes("/mzML/run");
        String startTimeStamp = runAttributes.get("startTimeStamp");
        Date dateCreation = null;

        if (startTimeStamp != null) {
            Calendar calendar = javax.xml.bind.DatatypeConverter.parseDateTime(startTimeStamp);
            dateCreation = calendar.getTime();
        }
        return dateCreation;
    }

    public List<Precursor> getPrecursors(){
        return null;
    }
}



