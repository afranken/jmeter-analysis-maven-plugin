package com.lazerycode.jmeter.analyzer.parser;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Counts http response status codes
 *
 * @author Arne Franken, Peter Kaul
 */
public class StatusCodes {

  /**
   * Http status code that is the first error status
   */
  public static final int HTTPCODE_ERROR = 400;

  /**
   * pseudo http error code that represents a TCP connection error
   */
  public static final int HTTPCODE_CONNECTIONERROR = 599;

  private final Map<Integer, Long> statusCodes = new HashMap<Integer, Long>();

  /**
   * Register statusCode or increment if statusCode is already registered
   *
   * @param code the statusCode
   */
  public void increment(int code) {

    Long result = statusCodes.get(code);
    if( result == null ) {
      statusCodes.put(code, 1L);
    }
    else {
      statusCodes.put(code, result+1);
    }
  }

  /**
   * @return a mapping from statusCode to count
   */
  public Map<Integer, Long> getCodes() {
    return Collections.unmodifiableMap(statusCodes);
  }
}
