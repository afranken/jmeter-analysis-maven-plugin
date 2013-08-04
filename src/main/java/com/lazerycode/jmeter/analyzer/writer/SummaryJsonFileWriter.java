package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.initializeFile;

/**
 * Writes a summary for all discovered / configured
 * {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroups} to a JSON file.
 */
public class SummaryJsonFileWriter extends AbstractTextWriter {

  private String jsonFilename = getFileName() + ".json";

  /**
   * Render results as text to a file
   *
   * @param testResults Map to generate output from
   * @throws java.io.IOException
   * @throws freemarker.template.TemplateException
   */
  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {
    FileWriter out = new FileWriter(initializeFile(getTargetDirectory(), jsonFilename, getResultDataFileRelativePath()));
    PrintWriter text = new PrintWriter(out, false);

    renderText(testResults, "json/main.ftl", text);

    text.flush();
    text.close();
    out.close();
  }
}
