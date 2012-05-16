package com.lazerycode.jmeter.analyzer.config;

import com.lazerycode.jmeter.analyzer.ResultRenderHelper;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Properties;

/**
 * Stores information used by several objects
 *
 * @author Arne Franken
 */
public enum Environment {

  /**
   * This is the only instance of this enum.
   */
  ENVIRONMENT;

  public static final int DEFAULT_MAXSAMPLES = 50000;

  public static final String ISO8601_FORMAT = "yyyyMMdd'T'HHmmssZ";

  /**
   * true, if charts should be generated
   */
  private boolean charts;

  /**
   * true, if response sizes and response durations should be provided for each uri
   */
  private boolean details;

  /**
   * Template directory where custom freemarker templates are stored.
   */
  private File templateDirectory;
  private ResultRenderHelper resultRenderHelper;
  private Properties remoteResources;
  private LinkedHashMap<String,String> requestGroups;
  private int maxSamples;
  private Configuration configuration;
  private File targetDirectory;

  public void clear() {
    this.templateDirectory = null;
    this.resultRenderHelper = null;
    this.remoteResources = null;
    this.requestGroups = null;
    this.maxSamples = 0;
    this.configuration = null;
    this.targetDirectory = null;
  }

  public File getTargetDirectory() {
    return targetDirectory;
  }

  public void setTargetDirectory(File targetDirectory) {
    this.targetDirectory = targetDirectory;
  }

  public boolean isCharts() {
    return charts;
  }

  public void setCharts(boolean charts) {
    this.charts = charts;
  }

  public boolean isDetails() {
    return details;
  }

  public void setDetails(boolean details) {
    this.details = details;
  }

  public File getTemplateDirectory() {
    return templateDirectory;
  }

  public void setTemplateDirectory(File templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  public ResultRenderHelper getResultRenderHelper() {
    return resultRenderHelper;
  }

  public void setResultRenderHelper(ResultRenderHelper resultRenderHelper) {
    this.resultRenderHelper = resultRenderHelper;
  }

  public Properties getRemoteResources() {
    return remoteResources;
  }

  public void setRemoteResources(Properties remoteResources) {
    this.remoteResources = remoteResources;
  }

  public LinkedHashMap<String, String> getRequestGroups() {
    return requestGroups;
  }

  public void setRequestGroups(LinkedHashMap<String, String> requestGroups) {
    this.requestGroups = requestGroups;
  }

  public int getMaxSamples() {
    return maxSamples;
  }

  public void setMaxSamples(int maxSamples) {
    this.maxSamples = maxSamples;
  }

  public Configuration getConfiguration() {
    return configuration;
  }

  /**
   * Initialize Freemarker Configuration
   */
  public void initializeFreemarkerConfiguration() {

    configuration = new Configuration();

    //make maps work in Freemarker when map key is not a String
    BeansWrapper beansWrapper = BeansWrapper.getDefaultInstance();
    beansWrapper.setSimpleMapWrapper(true);
    configuration.setObjectWrapper(beansWrapper);

    //make sure that numbers are not formatted as 1,000 but as 1000 instead
    configuration.setNumberFormat("computer");

    //TODO: make configurable?
    configuration.setDateFormat(ISO8601_FORMAT);

    configuration.setAutoFlush(true);
  }

}
