package com.lazerycode.jmeter.analyzer.writer;

/**
 * Writes detailed performance data per called URI as a CSV file
 */
public class DetailsToCsvWriter extends DetailsWriterBase {

  private static final String ROOT_TEMPLATE = "csv/main.ftl";

  /**
   * Needed to check if an Instance of DetailsToCsvWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   * @see com.lazerycode.jmeter.analyzer.AnalyzeMojo#initializeEnvironment()
   *
   * @param obj the object to check
   * @return true of obj is an instance of DetailsToCsvWriter.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DetailsToCsvWriter;
  }

  //--------------------------------------------------------------------------------------------------------------------

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }

  @Override
  protected String getDurationsSuffix() {
    return DURATIONS + super.getFileName() + CSV_EXT;
  }

  @Override
  protected String getSizesSuffix() {
    return SIZES + super.getFileName() + CSV_EXT;
  }
}
