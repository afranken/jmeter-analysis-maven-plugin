package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import com.lazerycode.jmeter.analyzer.util.TemplateUtil;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.*;

/**
 * Writes detailed performance data per called URI as a HTML file
 */
public class DetailsToHtmlWriter extends AbstractWriter {

  /**
   * Needed to check if an Instance of DetailsToHtmlWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   *
   * @param obj the object to check
   * @return true of obj is an instance of DetailsToHtmlWriter.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DetailsToHtmlWriter;
  }

  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    // Process every AggregatedResponse
    for (Map.Entry<String, AggregatedResponses> entry : testResults.entrySet()) {

      String name = entry.getKey();
      AggregatedResponses aggregatedResponses = entry.getValue();

      writeHTMLs(aggregatedResponses, name);
    }
  }

  //====================================================================================================================

  /**
   * Write HTMLs with detailed information
   *
   * @param name                filename
   * @param aggregatedResponses results to generate CSV from
   * @throws java.io.IOException
   */
  private void writeHTMLs(AggregatedResponses aggregatedResponses, String name) throws IOException, TemplateException {

    String durationsFilename;
    Map<String, Samples> data;


    durationsFilename = urlEncode(name) + durationsHtmlSuffix;
    data = aggregatedResponses.getDurationByUri();
    writeHTML(durationsFilename, data);

    durationsFilename = urlEncode(name) + sizesHtmlSuffix;
    data = aggregatedResponses.getSizeByUri();
    writeHTML(durationsFilename, data);

  }

  /**
   * Write samples data to file
   *
   * @param name filename
   * @param data results to generate HTML from
   * @throws java.io.IOException
   */
  private void writeHTML(String name, Map<String, Samples> data) throws IOException, TemplateException {

    FileWriter w = new FileWriter(initializeFile(getTargetDirectory(), name, getResultDataFileRelativePath()));
    PrintWriter printWriter = new PrintWriter(w, true);

    writeDetailHTML(data, "detailhtml/main.ftl", printWriter);

    printWriter.flush();
    printWriter.close();
    w.close();

  }

  /**
   * Writes {@link Samples} per uri to a HTML file
   *
   * @param testResults Mapping uri -&gt; samples
   * @throws IOException If writing fails
   */
  private void writeDetailHTML(Map<String, Samples> testResults, String template,
                               PrintWriter out) throws IOException, TemplateException {

    Map<String, Object> rootMap = TemplateUtil.getRootMap(testResults);

    Template root = TemplateUtil.getTemplate(template);

    // Merge data-model with template
    root.process(rootMap, out);
  }

}
