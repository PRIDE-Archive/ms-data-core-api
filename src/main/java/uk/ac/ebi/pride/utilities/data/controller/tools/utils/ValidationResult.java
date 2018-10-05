package uk.ac.ebi.pride.utilities.data.controller.tools.utils;

/**
 * Records an in-depth validation result, containing an AssayFileSummary and Report object.
 */
public class ValidationResult {

  private AssayFileSummary assayFileSummary;
  private Report report;

  /**
   * Constructor, sets the assayfilesummary and report objects.
   * @param assayFileSummary the summary of the assay file
   * @param report the report overview of the validation
   */
  public ValidationResult(AssayFileSummary assayFileSummary, Report report) {
    this.assayFileSummary = assayFileSummary;
    this.report = report;
  }

  /**
   * Gets report.
   *
   * @return Value of report.
   */
  public Report getReport() {
    return report;
  }

  /**
   * Sets new report.
   *
   * @param report New value of report.
   */
  public void setReport(Report report) {
    this.report = report;
  }

  /**
   * Gets assayFileSummary.
   *
   * @return Value of assayFileSummary.
   */
  public AssayFileSummary getAssayFileSummary() {
    return assayFileSummary;
  }

  /**
   * Sets new assayFileSummary.
   *
   * @param assayFileSummary New value of assayFileSummary.
   */
  public void setAssayFileSummary(AssayFileSummary assayFileSummary) {
    this.assayFileSummary = assayFileSummary;
  }
}