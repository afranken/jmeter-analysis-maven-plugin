package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.JMeterResultParser;
import com.lazerycode.jmeter.analyzer.util.FileUtil;
import com.lazerycode.jmeter.analyzer.writer.Writer;
import freemarker.template.TemplateException;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.io.Reader;
import java.util.Map;
import java.util.Properties;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;


/**
 * Analyze JMeter Result files.
 * Delegates output to {@link Writer} instances.
 *
 * @author Dennis Homann, Arne Franken, Peter Kaul
 */
public class ResultAnalyzer {

  private static final String FILENAME_DEFAULT = "summary";
  private String fileName;
  private final String resultDataFileRelativePath;

  public ResultAnalyzer(String resultDataFileRelativePath, String fileName) {
    this.fileName = fileName != null ? fileName : FILENAME_DEFAULT;
    this.resultDataFileRelativePath = resultDataFileRelativePath;
  }

  /**
   * Analyzes a JMeter XML results file
   *
   * @param jmeterResult The jmeter XML result file
   */
  public Map<String, AggregatedResponses> analyze(Reader jmeterResult) throws IOException, TemplateException, SAXException {

    Map<String, AggregatedResponses> testResults = new JMeterResultParser().aggregate(jmeterResult);

    for(Writer writer : ENVIRONMENT.getWriters()) {
      writer.setFileName(fileName);
      writer.setResultDataFileRelativePath(resultDataFileRelativePath);
      writer.write(testResults);
    }

    // --- download resources
    Properties remoteResources = ENVIRONMENT.getRemoteResources();
    if (remoteResources != null) {
      FileUtil.readResources(remoteResources, ENVIRONMENT.getTargetDirectory(),
              resultDataFileRelativePath, testResults.values(),
              ENVIRONMENT.getRemoteResourcesFromUntilDateFormat());
    }

    return testResults;
  }

}
