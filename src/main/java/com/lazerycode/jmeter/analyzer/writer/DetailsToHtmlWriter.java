package com.lazerycode.jmeter.analyzer.writer;

/**
 * Writes detailed performance data per called URI as a HTML file
 */
public class DetailsToHtmlWriter extends DetailsWriterBase {

  private static final String ROOT_TEMPLATE = "detailhtml/main.ftl";

  /**
   * Needed to check if an Instance of DetailsToHtmlWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   * @see com.lazerycode.jmeter.analyzer.AnalyzeMojo#initializeEnvironment()
   *
   * @param obj the object to check
   * @return true of obj is an instance of DetailsToHtmlWriter.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DetailsToHtmlWriter;
  }

  //--------------------------------------------------------------------------------------------------------------------

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }

  @Override
  protected String getDurationsSuffix() {
    return DURATIONS + super.getFileName() + HTML_EXT;
  }

  @Override
  protected String getSizesSuffix() {
    return SIZES + super.getFileName() + HTML_EXT;
  }
}
