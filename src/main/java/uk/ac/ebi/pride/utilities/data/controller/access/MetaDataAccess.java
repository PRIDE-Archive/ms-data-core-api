package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.core.ExperimentMetaData;
import uk.ac.ebi.pride.utilities.data.core.IdentificationMetaData;
import uk.ac.ebi.pride.utilities.data.core.MzGraphMetaData;

/**
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public interface MetaDataAccess {

    /**
     * Get a meta object
     *
     * @return MetaData meta data object
     */
    public ExperimentMetaData getExperimentMetaData();

    /**
     * Get the MetaData Information for Identification Object.
     *
     * @return IdentificationMetaData
     */
    public IdentificationMetaData getIdentificationMetaData();

    /**
     * Get MetaData Information for Spectrum Experiment Object
     *
     * @return MzGraphMetaData
     */
    public MzGraphMetaData getMzGraphMetaData();

    /**
     * This function give the user the possibility to know if the controller contains
     * MetaData. The metaData could be ExperimentMetadata, MzGraphMetaData, IdentificationMetaData
     *
     * @return boolean
     */
    public boolean hasMetaDataInformation();
}
