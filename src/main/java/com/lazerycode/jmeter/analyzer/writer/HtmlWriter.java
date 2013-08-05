package com.lazerycode.jmeter.analyzer.writer;

/**
 * Writes a complete summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} as a HTML file
 */
public class HtmlWriter extends TextWriterBase {

  private static final String ROOT_TEMPLATE = "html/main.ftl";

  @Override
  public String getFileName() {
    return super.getFileName() + HTML_EXT;
  }

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }

}
