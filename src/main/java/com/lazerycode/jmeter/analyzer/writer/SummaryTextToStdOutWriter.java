package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

/**
 * Writes a summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} to System.outj.
 */
public class SummaryTextToStdOutWriter extends AbstractTextWriter {

  /**
   * Render results as text to System.out
   *
   * @param testResults Map to generate output from
   * @throws IOException
   * @throws TemplateException
   */
  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    PrintWriter out = new PrintWriter(System.out, true);

    renderText(testResults, "text/main.ftl", out);

    out.flush();

  }

}
