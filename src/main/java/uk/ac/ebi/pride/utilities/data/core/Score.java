package uk.ac.ebi.pride.utilities.data.core;

import uk.ac.ebi.pride.utilities.data.utils.MapUtils;
import uk.ac.ebi.pride.utilities.term.SearchEngineCvTermReference;
import uk.ac.ebi.pride.utilities.term.SearchEngineScoreCvTermReference;

import java.util.*;

/**
 * Score stores a number of scores from a list of search engines.
 * <p/>
 * @author Rui Wang, Yasset Perez-Riverol
 * Date: Dec 2, 2010
 * Time: 9:02:05 AM
 */
public class Score {

    private final Map<SearchEngineCvTermReference, Map<SearchEngineScoreCvTermReference, Number>> scores;

    public Score() {
        scores = new HashMap<>();
    }

    public Score(Map<SearchEngineCvTermReference, Map<SearchEngineScoreCvTermReference, Number>> scores) {
        this.scores = MapUtils.createMapFromMap(scores);
    }

    /**
     * Get scores for search engine
     *
     * @param se search engine
     * @return peptide score map
     */
    public Map<SearchEngineScoreCvTermReference, Number> getScores(SearchEngineCvTermReference se) {
        return scores.get(se);
    }


    /**
     * Scan for scores using a given score cv term
     *
     * @param scoreCvTerm score cv term
     * @return a collection of values
     */
    public List<Number> getScores(SearchEngineScoreCvTermReference scoreCvTerm) {
        List<Number> values = new ArrayList<>();

        for (Map<SearchEngineScoreCvTermReference, Number> cvTermReferenceNumberMap : scores.values()) {
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
    public Number getScore(SearchEngineCvTermReference se, SearchEngineScoreCvTermReference ref) {
        Map<SearchEngineScoreCvTermReference, Number> scoreMap = scores.get(se);

        return (scoreMap == null) ? null : scoreMap.get(ref);
    }

    /**
     * Get all peptide scores, this will produce a sequential order list
     *
     * @return List<Number>    a list of scores
     */
    public List<Number> getAllScoreValues() {
        List<Number> scoreList = new ArrayList<>();

        for (Map<SearchEngineScoreCvTermReference, Number> numberMap : scores.values()) {
            scoreList.addAll(numberMap.values());
        }

        return scoreList;
    }

    /**
     * Get all the search engine types within this peptide
     *
     * @return List<SearchEngineCvTermReference>  a list of search engine types
     */
    public List<SearchEngineCvTermReference> getSearchEngineCvTermReferences() {
        return new ArrayList<>(scores.keySet());
    }

    /**
     * Add a new peptide score
     *
     * @param se  search engine
     * @param ref cv term reference for the score type
     * @param num peptide score
     */
    public void addScore(SearchEngineCvTermReference se, SearchEngineScoreCvTermReference ref, Number num) {

        // create a new if the search engine doesn't exist
        Map<SearchEngineScoreCvTermReference, Number> scoreMap;
        SearchEngineCvTermReference aux = null;

        if(se != null) {
            aux = se;
        }
        else {
            if(ref != null){
                aux = ref.getSearchEngineParam();
            }
        }

        assert aux != null; //Or the searchEngine is not define
        scoreMap = scores.get(aux);

        if (scoreMap == null) {
            scoreMap = new LinkedHashMap<>();
            scores.put(se, scoreMap);
        }
        // add the score
        scoreMap.put(ref, num);


    }


    /**
     * Remove all the scores assigned to the input search engine.
     *
     * @param se search engine
     */
    public void removeScore(SearchEngineCvTermReference se) {
        scores.remove(se);
    }

    /**
     * Remove peptide score with specified search engine and cv term reference
     *
     * @param se  search engine
     * @param ref cv term reference
     */
    public void removeScore(SearchEngineCvTermReference se, SearchEngineScoreCvTermReference ref) {
        Map<SearchEngineScoreCvTermReference, Number> scoreMap = scores.get(se);

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
        Object[] scoresArrayValue = ((Map<SearchEngineScoreCvTermReference, Number>) scoresArray[0]).values().toArray();
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
    public SearchEngineCvTermReference getDefaultSearchEngine() {
        Object[] searchengines = scores.keySet().toArray();
        return (SearchEngineCvTermReference) searchengines[0];
    }


    public List<SearchEngineScoreCvTermReference> getSearchEngineScoreCvTermReferenceWithValues() {
        List<SearchEngineScoreCvTermReference> listReference = new ArrayList<>();
        for (Map<SearchEngineScoreCvTermReference, Number> numberMap : scores.values()) {
            for (Map.Entry mapEntry : numberMap.entrySet()){
                if (mapEntry.getValue() != null) {
                    listReference.add((SearchEngineScoreCvTermReference) mapEntry.getKey());
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
        List<SearchEngineCvTermReference> searchEngines = getSearchEngineCvTermReferences();
        for(SearchEngineCvTermReference searchEngineCvTermReference: searchEngines){
            Map<SearchEngineScoreCvTermReference, Number> mapScore = getScores(searchEngineCvTermReference);
            for(SearchEngineScoreCvTermReference ref: mapScore.keySet())
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



