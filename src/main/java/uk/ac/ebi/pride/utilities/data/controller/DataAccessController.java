package uk.ac.ebi.pride.utilities.data.controller;


import uk.ac.ebi.pride.utilities.data.controller.access.*;

import java.util.Collection;


/**
 * <p>
 * DataAccessController is an aggregate interface for data access.
 * It extends a list of interfaces, also added methods for accessing metadata.
 * Setting the state of the data access controller.
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public interface DataAccessController
        extends MetaDataAccess, MzGraphDataAccess,
                ProteinDataAccess, ProteinGroupDataAccess,
                PeptideDataAccess, QuantDataAccess {

    /**
     * ContentCategory defines the type of content a data access controller should have.
     * Note: this does not automatically mean they must have them.
     */
    public enum ContentCategory {
        SPECTRUM,
        CHROMATOGRAM,
        PROTEIN,
        PROTEIN_GROUPS,
        PEPTIDE,
        QUANTIFICATION,
        SAMPLE,
        PROTOCOL,
        SOFTWARE,
        INSTRUMENT,
        STUDY_VARIABLE,
        DATA_PROCESSING
    }


    /**
     * Type indicates the I/O of the data source.
     * There are two types of data access controller at the moment:
     * <p/>
     * DATABASE means connection to a database
     * XML_FILE means reading the data from a file.
     */
    public enum Type { DATABASE, XML_FILE, PEAK_FILE, MZIDENTML, MZTAB}

    /**
     * Get the unique id represent the uniqueness of the data source
     *
     * @return String    uid
     */
    public String getUid();

    /**
     * Get the display name for this controller, for GUI
     *
     * @return String the name of this DataAccessController
     */
    public String getName();

    /**
     * Get the type of database access controller.
     *
     * @return DataAccessController.Type controller type.
     */
    public Type getType();

    /**
     * Return a collection of content categories
     *
     * @return Collection<ContentCategory>  a collection of content categories.
     */
    public Collection<ContentCategory> getContentCategories();

    /**
     * Get the original data source object
     *
     * @return Object   data source object
     */
    public Object getSource();

    /**
     * shutdown this controller, release all the resources.
     */
    public void close();

}

