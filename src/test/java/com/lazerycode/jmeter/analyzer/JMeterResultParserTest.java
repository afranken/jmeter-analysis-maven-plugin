package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.JMeterResultParser;
import junit.framework.TestCase;

import java.io.InputStreamReader;
import java.util.Map;

/**
 * Tests {@link JMeterResultParser}
 */
public class JMeterResultParserTest extends TestCase {

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

  // TODO: more tests


}
