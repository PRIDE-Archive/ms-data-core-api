package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

/**
 * Class to store the ASQL schema datatype information about fields.
 */
public class AsqlTriple {

  private Utility.AsqlDataType asqlDataType;
  private String asqlName;
  private String asqlDesc;

  /**
   * Default constructor.
   */
  AsqlTriple() {
  }

  /**
   * Constructor with all the supplied variables to hold BED data type information.
   *
   * @param asqlDataType the field's data type
   * @param asqlName the field's name
   * @param asqlDesc the field's description
   */
  public AsqlTriple(Utility.AsqlDataType asqlDataType, String asqlName, String asqlDesc) {
    this.asqlDataType = asqlDataType;
    this.asqlName = asqlName;
    this.asqlDesc = asqlDesc;
  }

  /**
   * Gets the data type.
   * @return the data type.
   */
  public Utility.AsqlDataType getAsqlDataType() {
    return asqlDataType;
  }

  /**
   * Sets the data type.
   * @param asqlDataType the data type.
   */
  public void setAsqlDataType(Utility.AsqlDataType asqlDataType) {
    this.asqlDataType = asqlDataType;
  }

  /**
   * Gets the name.
   * @return the name.
   */
  public String getAsqlName() {
    return asqlName;
  }

  /**
   * Sets the name.
   * @param asqlName the name.
   */
  public void setAsqlName(String asqlName) {
    this.asqlName = asqlName;
  }

  /**
   * Gets the description.
   * @return the description.
   */
  public String getAsqlDesc() {
    return asqlDesc;
  }

  /**
   * Sets the description.
   * @param asqlDesc the description.
   */
  public void setAsqlDesc(String asqlDesc) {
    this.asqlDesc = asqlDesc;
  }
}
