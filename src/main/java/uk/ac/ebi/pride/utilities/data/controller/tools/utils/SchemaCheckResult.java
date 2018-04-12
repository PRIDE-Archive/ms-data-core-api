package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

import java.util.List;

/**
 * Records if a schema check succeeded, or if it failed and its associated error messages.
 */
public class SchemaCheckResult {
  private boolean validAgainstSchema;
  private List<String> errorMessages;

  /**
   * Constructor, sets the check check result, and any error messages.
   * @param validAgainstSchema true if the schema check was OK, false otherwise
   * @param errorMessages any errors when checked against the schema
   */
  public SchemaCheckResult(boolean validAgainstSchema, List<String> errorMessages) {
    this.validAgainstSchema = validAgainstSchema;
    this.errorMessages = errorMessages;
  }

  /**
   * Sets new validAgainstSchema.
   *
   * @param validAgainstSchema New value of validAgainstSchema.
   */
  public void setValidAgainstSchema(boolean validAgainstSchema) {
    this.validAgainstSchema = validAgainstSchema;
  }

  /**
   * Sets new errorMessages.
   *
   * @param errorMessages New value of errorMessages.
   */
  public void setErrorMessages(List<String> errorMessages) {
    this.errorMessages = errorMessages;
  }

  /**
   * Gets errorMessages.
   *
   * @return Value of errorMessages.
   */
  public List<String> getErrorMessages() {
    return errorMessages;
  }

  /**
   * Gets validAgainstSchema.
   *
   * @return Value of validAgainstSchema.
   */
  public boolean isValidAgainstSchema() {
    return validAgainstSchema;
  }
}