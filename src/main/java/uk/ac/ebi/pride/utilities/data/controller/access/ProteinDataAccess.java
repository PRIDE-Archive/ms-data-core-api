package uk.ac.ebi.pride.utilities.data.controller.access;

import uk.ac.ebi.pride.utilities.data.core.DBSequence;
import uk.ac.ebi.pride.utilities.data.core.Protein;
import uk.ac.ebi.pride.utilities.data.core.Score;
import uk.ac.ebi.pride.utilities.data.core.SearchDataBase;
import uk.ac.ebi.pride.utilities.term.SearchEngineCvTermReference;
import uk.ac.ebi.pride.utilities.term.SearchEngineScoreCvTermReference;

import java.util.Collection;


/**
 * ProteinDataAccess defines the interface for accessing identification data.
 * <p/>
 * @author Rui Wang
 * @author Yasset Perez-Riverol
 */

public interface ProteinDataAccess {

    /**
     * Whether this controller contains identifications
     *
     * @return boolean  return true if identifications exist
     */
    public boolean hasProtein();

    /**
     * Get a collection of identification ids
     *
     * @return Collection   a string collection of identification ids
     */
    public Collection<Comparable> getProteinIds();


    /**
     * Get the index of identification by its id
     *
     * @param proteinId identification id
     * @return int  identification index
     */
    public int indexOfProtein(Comparable proteinId);

    /**
     * Get a Identification object
     *
     * @param proteinId a string id of Identification
     * @return Identification an Identification object
     */
    public Protein getProteinById(Comparable proteinId);

    /**
     * Get the total number of protein identifications.
     *
     * @return int  the number of protein identifications.
     */
    public int getNumberOfProteins();

    /**
     * This function return the number of an-core proteins in ms-data core api that:
     *    1- Pass the threshold in the protein group.
     *    2- The an-core protein is not a decoy protein.
     * return
     */
    //public int getFinalListOfProteinGroups();

    /**
     *
     */


    /**
     * Get the protein accession of a identification
     *
     * @param proteinId identification id.
     * @return String   protein accession.
     */
    public String getProteinAccession(Comparable proteinId);

    /**
     * Get the protein accession version of a identification
     *
     * @param proteinId identification id.
     * @return String   protein accession version.
     */
    public String getProteinAccessionVersion(Comparable proteinId);

    /**
     * Get the score of a identification.
     *
     * @param proteinId identification id.
     * @return double   score.
     */
    public double getProteinScore(Comparable proteinId);

    /**
     * This function returns the protein coverage for a protein
     * @param proteinId Protein Id
     * @return Value of the protein coverage
     */
    public double getProteinCoverage(Comparable proteinId);

    /**
     * Get protein identification score
     *
     * @param proteinId Protein Id
     * @return Protein Score List
     */
    public Score getProteinScores(Comparable proteinId);

    /**
     * Get the threshold of a identification.
     *
     * @param proteinId identification id.
     * @return double   threshold.
     */
    public double getProteinThreshold(Comparable proteinId);

    /**
     * Get the search database of a identification
     *
     * @param proteinId identification id.
     * @return String   search database.
     */
    public SearchDataBase getSearchDatabase(Comparable proteinId);

    /**
     * Get the search engine of a identification
     *
     * @return a collection of search engine types
     */
    public Collection<SearchEngineCvTermReference> getSearchEngineCvTermReferences();

    /**
     * Get the search database version of a identification
     *
     * @param proteinId identification id
     * @return String  search database version
     */
    public String getSearchDatabaseVersion(Comparable proteinId);

    /**
     * get a list of present protein scores in CV terms
     *
     * @return List of Protein Scores in CvTerm
     */
    public Collection<SearchEngineScoreCvTermReference> getAvailableProteinLevelScores();

    /**
     * Get the sequence of the Identified Protein
     *
     * @param proteinId identification Id
     * @return Sequence Object in the Database
     */
    public DBSequence getProteinSequence(Comparable proteinId);

    /**
     * Whether this controller contains protein sequences
     *
     * @return boolean  return true if identifications exist
     */

    public boolean hasProteinSequence();

    /**
     * This function return true if the controller contains decoy information and false if the controller do not contain decoy information.
     * @return Return true if the file contain Decoy Information
     */
    public boolean hasDecoyInformation();

}



