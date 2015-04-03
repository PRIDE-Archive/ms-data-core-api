package uk.ac.ebi.pride.utilities.data.controller;

/**
 * <p>
 * DataAccessException is thrown when there is an error during i/o via data access
 * controller.
 * <p/>
 *
 * @author Yasset Perez-Riverol
 * @author Rui Wang
 */
public class DataAccessException extends RuntimeException {

    /**
     * Create a DataAccessException using an specific text message
     * @param message error message
     */
    public DataAccessException(String message) {
        super(message);
    }

    /**
     * Create a DataAccessException using an specific text message and the cause of the error
     * @param message error message
     * @param cause   Throwable cause of the error
     */
    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}



