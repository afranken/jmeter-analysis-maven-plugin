package com.lazerycode.jmeter.analyzer.writer;

/**
 * Writes a summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} to a TXT file.
 */
public class SummaryTextToFileWriter extends TextWriterBase {

  private static final String ROOT_TEMPLATE = "text/main.ftl";

  @Override
  public String getFileName() {
    return super.getFileName() + TXT_EXT;
  }

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }
}
