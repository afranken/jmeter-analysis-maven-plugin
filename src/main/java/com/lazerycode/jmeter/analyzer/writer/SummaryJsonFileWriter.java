package com.lazerycode.jmeter.analyzer.writer;

/**
 * Writes a summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} to a JSON file.
 */
public class SummaryJsonFileWriter extends TextWriterBase {

  private static final String ROOT_TEMPLATE = "json/main.ftl";

  @Override
  public String getFileName() {
    return super.getFileName() + JSON_EXT;
  }

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }
}
