package com.lazerycode.jmeter.analyzer.writer;

import com.google.common.annotations.VisibleForTesting;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Writes a summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} to System.outj.
 */
public class SummaryTextToStdOutWriter extends TextWriterBase {

  private static final String ROOT_TEMPLATE = "text/main.ftl";

  /**
   * Render results as text to System.out
   *
   * @param testResults Map to generate output from
   * @throws IOException
   * @throws TemplateException
   */
  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    java.io.Writer out = getWriter();

    renderText(testResults, getRootTemplate(), out);

    out.flush();
  }

  //--------------------------------------------------------------------------------------------------------------------

  @Override
  protected String getRootTemplate() {
    return ROOT_TEMPLATE;
  }

  @VisibleForTesting
  protected java.io.Writer getWriter() throws IOException {
    return new PrintWriter(System.out, true);
  }

}
