package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.util.TemplateUtil;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.initializeFile;

/**
 * Writes a complete summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} as a HTML file
 */
public class HtmlWriter extends AbstractWriter {

  /**
   * Render results as HTML file
   *
   * @param testResults Map to generate output from
   * @throws IOException
   * @throws TemplateException
   */
  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {
    FileWriter w = new FileWriter(initializeFile(getTargetDirectory(), summaryHtmlFileName, getResultDataFileRelativePath()));
    PrintWriter html = new PrintWriter(w, false);

    renderHTML(testResults, "html/main.ftl", html, this.fileName);

    html.flush();
    html.close();
    w.close();
  }

  //====================================================================================================================

  /**
   * Render given {@link AggregatedResponses testResults} as HTML
   *
   * @param testResults results to render
   * @param out         output to write to
   * @throws IOException
   * @throws TemplateException
   */
  private void renderHTML(Map<String, AggregatedResponses> testResults, String template, PrintWriter out, String summaryFileName) throws IOException, TemplateException {

    Map<String, Object> rootMap = TemplateUtil.getRootMap(testResults);
    rootMap.put("SUMMARY_FILE_NAME", summaryFileName);

    Template root = TemplateUtil.getTemplate(template);

    // Merge data-model with template
    root.process(rootMap, out);
  }
}
