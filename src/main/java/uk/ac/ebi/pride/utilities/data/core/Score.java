package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.MapUtils;
import uk.ac.ebi.pride.utilities.engine.SearchEngineType;
import uk.ac.ebi.pride.utilities.term.CvTermReference;

import java.util.*;

/**
 * Score stores a number of scores from a list of search engines.
 * <p/>
 * @author rwang, ypriverol
 * Date: Dec 2, 2010
 * Time: 9:02:05 AM
 */
public class Score {

    private final Map<SearchEngineType, Map<CvTermReference, Number>> scores;

    public Score() {
        scores = new HashMap<SearchEngineType, Map<CvTermReference, Number>>();
    }

    public Score(Map<SearchEngineType, Map<CvTermReference, Number>> scores) {
        this.scores = MapUtils.createMapFromMap(scores);
    }

    /**
     * Get scores for search engine
     *
     * @param se search engine
     * @return peptide score map
     */
    public Map<CvTermReference, Number> getScores(SearchEngineType se) {
        return scores.get(se);
    }


    /**
     * Scan for scores using a given score cv term
     *
     * @param scoreCvTerm score cv term
     * @return a collection of values
     */
    public List<Number> getScores(CvTermReference scoreCvTerm) {
        List<Number> values = new ArrayList<Number>();

        for (Map<CvTermReference, Number> cvTermReferenceNumberMap : scores.values()) {
            if (cvTermReferenceNumberMap.containsKey(scoreCvTerm)) {
                values.add(cvTermReferenceNumberMap.get(scoreCvTerm));
            }
        }

        return values;
    }

    /**
     * Get peptide score for a specified search engine and cv term reference
     *
     * @param se  search engine
     * @param ref cv term reference
     * @return Number  peptide score
     */
    public Number getScore(SearchEngineType se, CvTermReference ref) {
        Map<CvTermReference, Number> scoreMap = scores.get(se);

        return (scoreMap == null) ? null : scoreMap.get(ref);
    }

    /**
     * Get all peptide scores, this will produce a sequential order list
     *
     * @return List<Number>    a list of scores
     */
    public List<Number> getAllScoreValues() {
        List<Number> scoreList = new ArrayList<Number>();

        for (Map<CvTermReference, Number> numberMap : scores.values()) {
            scoreList.addAll(numberMap.values());
        }

        return scoreList;
    }

    /**
     * Get all the search engine types within this peptide
     *
     * @return List<SearchEngineType>  a list of search engine types
     */
    public List<SearchEngineType> getSearchEngineTypes() {
        return new ArrayList<SearchEngineType>(scores.keySet());
    }

    /**
     * Add a new peptide score
     *
     * @param se  search engine
     * @param ref cv term reference for the score type
     * @param num peptide score
     */
    public void addScore(SearchEngineType se, CvTermReference ref, Number num) {

        // create a new if the search engine doesn't exist
        if(se != null){
            Map<CvTermReference, Number> scoreMap = scores.get(se);

            if (scoreMap == null) {
                scoreMap = new LinkedHashMap<CvTermReference, Number>();
                scores.put(se, scoreMap);

                // for each cv term
                List<CvTermReference> cvTerms = se.getSearchEngineScores();

                for (CvTermReference cvTerm : cvTerms) {
                    scoreMap.put(cvTerm, null);
                }
            }
            // add the score
            scoreMap.put(ref, num);
        }
    }

    /**
     * Remove all the scores assigned to the input search engine.
     *
     * @param se search engine
     */
    public void removeScore(SearchEngineType se) {
        scores.remove(se);
    }

    /**
     * Remove peptide score with specified search engine and cv term reference
     *
     * @param se  search engine
     * @param ref cv term reference
     */
    public void removeScore(SearchEngineType se, CvTermReference ref) {
        Map<CvTermReference, Number> scoreMap = scores.get(se);

        if (scoreMap != null) {
            scoreMap.remove(ref);
        }
    }

    /**
     * Get the Default score for Search Engine
     * <p/>
     * * @return score
     */
    public double getDefaultScore() {
        Object[] scoresArray = scores.values().toArray();
        Object[] scoresArrayValue = ((Map<CvTermReference, Number>) scoresArray[0]).values().toArray();
        double scoreValue = -1;
        for (Object aScoresArrayValue : scoresArrayValue) {
            if (aScoresArrayValue != null) {
                scoreValue = ((Double) aScoresArrayValue);
                return scoreValue;
            }
        }
        return scoreValue;
    }

    /**
     * Get Default Search Engine for Scores.
     *
     * @return Default Search Engine for the Scores
     */
    public SearchEngineType getDefaultSearchEngine() {
        Object[] searchengines = scores.keySet().toArray();
        return (SearchEngineType) searchengines[0];
    }

    public List<CvTermReference> getCvTermReferenceWithValues() {
        List<CvTermReference> listReference = new ArrayList<CvTermReference>();
        for (Map<CvTermReference, Number> numberMap : scores.values()) {
            Iterator iterator = numberMap.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry mapEntry = (Map.Entry) iterator.next();
                if (mapEntry.getValue() != null) {
                    listReference.add((CvTermReference) mapEntry.getKey());
                }
            }
        }
        return listReference;
    }

    /**
     * Retrieve the value of one score for an specific search engine term
     * @param accession Specific CV Accession for Search Engine Score
     * @return The value of the score
     */
    public Number getValueBySearchEngineScoreTerm(String accession){
        List<SearchEngineType> searchEngines = getSearchEngineTypes();
        for(SearchEngineType searchEngineType: searchEngines){
            Map<CvTermReference, Number> mapScore = getScores(searchEngineType);
            for(CvTermReference ref: mapScore.keySet())
                if(ref.getAccession().equalsIgnoreCase(accession))
                    return mapScore.get(ref);
        }
        return null;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Score)) return false;

        Score score = (Score) o;

        return scores.equals(score.scores);

    }

    @Override
    public int hashCode() {
        return scores.hashCode();
    }
}



