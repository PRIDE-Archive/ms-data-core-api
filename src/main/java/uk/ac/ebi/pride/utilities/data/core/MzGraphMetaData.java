package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.CollectionUtils;

import java.util.List;

/**
 * This class storage the information related with metadata at the spectrum level. the object that are support by this
 * class are PRIDE Object, mzML, mgf, etc.
 * @author Yasset Perez-Riverol
 * Date: 15/08/11
 * Time: 11:35
 */
public class MzGraphMetaData extends IdentifiableParamGroup {

    /**
     * List and descriptions of data processing applied to this data, this structure is used by mzML
     * files to represent each procedure applied to the data by steps. Each DataProcessing step have the
     * following structure:
     * - id
     * - name
     * - Map of Software and ParamGroup.
     */
    private final List<DataProcessing> dataProcessings;

    /**
     * list and descriptions of instruments settings
     */
    private final List<InstrumentConfiguration> instrumentConfigurations;

    /**
     * list and descriptions of the acquisition settings applied prior to the start of data acquisition.
     */
    private final List<ScanSetting> scanSettings;

    public MzGraphMetaData(Comparable id, String name, List<ScanSetting> scanSettings,
                           List<InstrumentConfiguration> instrumentConfigurations,
                           List<DataProcessing> dataProcessings) {
        this(null, id, name, scanSettings, instrumentConfigurations, dataProcessings);
    }

    /**
     * @param params                   ParamGroup of MzGraphMetaData
     * @param id                       Generic Id of MzGraphMetaData
     * @param name                     Generic Name
     * @param scanSettings             Scan Settings
     * @param instrumentConfigurations Instrument Configurations
     * @param dataProcessings          Data Processing List
     */
    public MzGraphMetaData(ParamGroup params, Comparable id, String name, List<ScanSetting> scanSettings,
                           List<InstrumentConfiguration> instrumentConfigurations,
                           List<DataProcessing> dataProcessings) {
        super(params, id, name);
        this.scanSettings = CollectionUtils.createListFromList(scanSettings);
        this.instrumentConfigurations = CollectionUtils.createListFromList(instrumentConfigurations);
        this.dataProcessings = CollectionUtils.createListFromList(dataProcessings);
    }

    public ParamGroup getFileContent() {
        return new ParamGroup(this.getCvParams(), this.getUserParams());
    }

    public void setFileContent(ParamGroup fileContent) {
        this.setCvParams(fileContent.getCvParams());
        this.setUserParams(fileContent.getUserParams());
    }

    public List<ScanSetting> getScanSettings() {
        return scanSettings;
    }

    public void setScanSettings(List<ScanSetting> scanSettings) {
        CollectionUtils.replaceValuesInCollection(scanSettings, this.scanSettings);
    }

    public List<InstrumentConfiguration> getInstrumentConfigurations() {
        return instrumentConfigurations;
    }

    public void setInstrumentConfigurations(List<InstrumentConfiguration> instrumentConfigurations) {
        CollectionUtils.replaceValuesInCollection(instrumentConfigurations, this.instrumentConfigurations);
    }

    public List<DataProcessing> getDataProcessings() {
        return dataProcessings;
    }

    public void setDataProcessings(List<DataProcessing> dataProcessings) {
        CollectionUtils.replaceValuesInCollection(dataProcessings, this.dataProcessings);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof MzGraphMetaData)) return false;
        if (!super.equals(o)) return false;

        MzGraphMetaData that = (MzGraphMetaData) o;

        return dataProcessings.equals(that.dataProcessings) && instrumentConfigurations.equals(that.instrumentConfigurations) && scanSettings.equals(that.scanSettings);

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + dataProcessings.hashCode();
        result = 31 * result + instrumentConfigurations.hashCode();
        result = 31 * result + scanSettings.hashCode();
        return result;
    }
}



