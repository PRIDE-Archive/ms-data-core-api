package uk.ac.ebi.pride.utilities.data.controller;

/**
 * DataAccessMode is mainly to aid caching in data access controller, it can be use to
 * cache the different objects of the source in memory or from the source.
 * There are two modes at the moment:
 * <p/>
 * 1. CACHE_ONLY means only retrieve information from the cache.
 * <p/>
 * 2. CACHE_AND_SOURCE means retrieve information from cache first, if didn't find anything,
 * then read from data source directly.
 * <p/>
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public enum DataAccessMode { CACHE_ONLY, CACHE_AND_SOURCE, }



