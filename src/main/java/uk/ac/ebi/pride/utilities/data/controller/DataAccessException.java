package uk.ac.ebi.pride.utilities.data.controller;

/**
 * DataAccessException is thrown when there is an error during i/o via data access controller
 * <p/>
 * @author ypriverol
 * @author rwang
 */
public class DataAccessException extends RuntimeException {

    public DataAccessException(String message) {
        super(message);
    }

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}



