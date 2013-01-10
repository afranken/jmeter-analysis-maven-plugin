package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.util.TemplateUtil;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.initializeFile;
import static com.lazerycode.jmeter.analyzer.util.FileUtil.urlEncode;


/**
 * Writes detailed performance data per called URI as a CSV file
 */
public class DetailsToCsvWriter extends AbstractWriter {

  /**
   * Needed to check if an Instance of DetailsToCsvWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   *
   * @param obj the object to check
   * @return true of obj is an instance of DetailsToCsvWriter.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof DetailsToCsvWriter;
  }

  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    // Process every AggregatedResponse
    for (Map.Entry<String, AggregatedResponses> entry : testResults.entrySet()) {

      String name = entry.getKey();
      AggregatedResponses aggregatedResponses = entry.getValue();

      writeCVSs(aggregatedResponses, name);
    }
  }

  //====================================================================================================================

  /**
   * Write CSVs with detailed information
   *
   * @param name                filename
   * @param aggregatedResponses results to generate CSV from
   * @throws IOException
   */
  private void writeCVSs(AggregatedResponses aggregatedResponses, String name) throws IOException, TemplateException {

    String durationsFilename;
    Map<String, Samples> data;


    durationsFilename = urlEncode(name) + durationsCsvSuffix;
    data = aggregatedResponses.getDurationByUri();
    writeCsv(durationsFilename, data);

    durationsFilename = urlEncode(name) + sizesCsvSuffix;
    data = aggregatedResponses.getSizeByUri();
    writeCsv(durationsFilename, data);

  }

  /**
   * Write samples data to file
   *
   * @param name filename
   * @param data results to generate CSV from
   * @throws IOException
   */
  private void writeCsv(String name, Map<String, Samples> data) throws IOException, TemplateException {

    FileWriter w = new FileWriter(initializeFile(getTargetDirectory(), name, getResultDataFileRelativePath()));
    PrintWriter printWriter = new PrintWriter(w, true);

    writeDetailCSV(data, "csv/main.ftl", printWriter);

    printWriter.flush();
    printWriter.close();
    w.close();

  }

  /**
   * Writes {@link Samples} per uri to a CSV file
   *
   * @param testResults Mapping uri -&gt; samples
   * @throws IOException If writing fails
   */
  private void writeDetailCSV(Map<String, Samples> testResults, String template,
                              PrintWriter out) throws IOException, TemplateException {

    Map<String, Object> rootMap = TemplateUtil.getRootMap(testResults);

    Template root = TemplateUtil.getTemplate(template);

    // Merge data-model with template
    root.process(rootMap, out);
  }

}
