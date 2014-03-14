package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.config.Environment;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.JMeterResultParser;

import junit.framework.TestCase;

import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Tests {@link JMeterResultParser}
 */
public class JMeterResultParserTest extends TestCase {

  public void setUp() {
    ENVIRONMENT.clear();
    ENVIRONMENT.setLog(new SystemStreamLog());
    ENVIRONMENT.setSampleNames(new HashSet<String>(Arrays.asList(new String[]{Environment.HTTPSAMPLE_ELEMENT_NAME, Environment.SAMPLE_ELEMENT_NAME})));
  }

  public void testSuccess() throws Exception {

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-success.xml")));

    assertEquals("size", 1, result.size());

    AggregatedResponses r = result.get("warmup");

    // test requests
    assertEquals("samples", 10, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 10, r.getDuration().getSuccessCount());
    assertEquals("failure", 0, r.getDuration().getErrorsCount());
  }

  /**
   * Nodenames can be {@link Environment#HTTPSAMPLE_ELEMENT_NAME} or {@link Environment#SAMPLE_ELEMENT_NAME}
   */
  public void testDifferentNodeNames() throws Exception {

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-differentNodeNames.xml")));

    assertEquals("size", 1, result.size());

    AggregatedResponses r = result.get("warmup");

    // test requests
    assertEquals("samples", 3, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 3, r.getDuration().getSuccessCount());
    assertEquals("failure", 0, r.getDuration().getErrorsCount());
  }

  public void testSomeErrors() throws Exception {

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-someErrors.xml")));

    assertEquals("size", 1, result.size());

    AggregatedResponses r = result.get("warmup");

    // test requests
    assertEquals("samples", 1, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 1, r.getDuration().getSuccessCount());
    assertEquals("failure", 2, r.getDuration().getErrorsCount());
  }

  public void testOnlyErrors() throws Exception {

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-onlyErrors.xml")));

    assertEquals("size", 1, result.size());

    AggregatedResponses r = result.get("warmup");

    // test requests
    assertEquals("samples", 0, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 0, r.getDuration().getSuccessCount());
    assertEquals("failure", 3, r.getDuration().getErrorsCount());
  }

  public void testEmptyResultsFile() throws Exception {

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-empty.xml")));

    assertEquals("size", 0, result.size());
  }

  public void testPatternSuccess() throws Exception {
    List<RequestGroup> requestGroups = new ArrayList<RequestGroup>();
    RequestGroup requestGroup1 = new RequestGroup();
    requestGroup1.setName("PATTERN_NAME");
    requestGroup1.setPattern("main");
    requestGroups.add(requestGroup1);
    ENVIRONMENT.setRequestGroups(requestGroups);

    JMeterResultParser a = new JMeterResultParser();
    Map<String, AggregatedResponses> result = a.aggregate(new InputStreamReader(getClass().getResourceAsStream("JMeterResultParserTest-patternSuccess.xml")));

    assertEquals("size", 2, result.size());

    AggregatedResponses r = result.get("PATTERN_NAME");

    // test requests
    assertEquals("samples", 6, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 6, r.getDuration().getSuccessCount());
    assertEquals("failure", 0, r.getDuration().getErrorsCount());

    r = result.get("default");

    // test requests
    assertEquals("samples", 4, r.getDuration().getStoredSamplesCount());
    assertEquals("success", 4, r.getDuration().getSuccessCount());
    assertEquals("failure", 0, r.getDuration().getErrorsCount());
  }

  // TODO: more tests


}
