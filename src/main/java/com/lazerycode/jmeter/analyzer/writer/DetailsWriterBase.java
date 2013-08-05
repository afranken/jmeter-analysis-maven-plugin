package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.urlEncode;

/**
 * Abstract super class of {@link Writer} implementations that write only detail data.
 */
public abstract class DetailsWriterBase extends TextWriterBase {

  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    // Process every AggregatedResponse
    for (Map.Entry<String, AggregatedResponses> entry : testResults.entrySet()) {

      String name = entry.getKey();
      AggregatedResponses aggregatedResponses = entry.getValue();

      write(aggregatedResponses, name);
    }
  }

  //--------------------------------------------------------------------------------------------------------------------

  protected abstract String getDurationsSuffix();
  protected abstract String getSizesSuffix();

  //====================================================================================================================

  /**
   * Write CSVs with detailed information
   *
   * @param name                filename
   * @param aggregatedResponses results to generate CSV from
   * @throws IOException
   */
  private void write(AggregatedResponses aggregatedResponses, String name) throws IOException, TemplateException {

    String durationsFilename;
    Map<String, Samples> data;


    durationsFilename = urlEncode(name) + getDurationsSuffix();
    data = aggregatedResponses.getDurationByUri();
    write(durationsFilename, data);

    durationsFilename = urlEncode(name) + getSizesSuffix();
    data = aggregatedResponses.getSizeByUri();
    write(durationsFilename, data);

  }

  /**
   * Write samples data to file
   *
   * @param name filename
   * @param data results to generate CSV from
   * @throws IOException
   */
  private void write(String name, Map<String, Samples> data) throws IOException, TemplateException {
    java.io.Writer w = getWriter(getFile(name));

    renderText(data, getRootTemplate(), w);

    w.flush();
    w.close();
  }

}
