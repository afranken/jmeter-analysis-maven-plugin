package com.lazerycode.jmeter.analyzer.config;

/**
 * Configuration POJO containing a name and pattern tuple.
 */
public class RequestGroup {

  private String name;
  private String pattern;

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public String getPattern() {
    return pattern;
  }

  public void setPattern(String pattern) {
    this.pattern = pattern;
  }
}
