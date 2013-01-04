package com.lazerycode.jmeter.analyzer.config;

import com.lazerycode.jmeter.analyzer.ResultRenderHelper;
import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;
import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.Set;

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

  /**
   * Example from JMeter results file:
   * <httpSample t="1" lt="1" ts="1305278457847" s="false" lb="/sample/url/path.html" rc="404" rm="Not Found" tn="homepage 4-1" dt="" by="0"/>
   *
   * According to the documentation, the two possible node names are
   * {@link #HTTPSAMPLE_ELEMENT_NAME} and {@link #SAMPLE_ELEMENT_NAME}:
   * http://jmeter.apache.org/usermanual/listeners.html
   */
  public static final String HTTPSAMPLE_ELEMENT_NAME = "httpSample";
  public static final String SAMPLE_ELEMENT_NAME = "sample";

  public static final int DEFAULT_MAXSAMPLES = 50000;

  public static final String ISO8601_FORMAT = "yyyyMMdd'T'HHmmssZ";

  /**
   * true, if charts should be generated
   */
  private boolean generateCharts;

  /**
   * true, if response sizes and response durations should be provided for each uri
   */
  private boolean generateCSVs;

  /**
   * Template directory where custom freemarker templates are stored.
   */
  private File templateDirectory;
  private ResultRenderHelper resultRenderHelper;
  private Properties remoteResources;
  private Set<String> sampleNames;
  private LinkedHashMap<String,String> requestGroups;
  private int maxSamples = DEFAULT_MAXSAMPLES;
  private Configuration configuration;
  private File targetDirectory;
  private Log log;

  /**
   * If true, preserve the relative part of the result file's path
   */
  private boolean preserveDirectories;

  /**
   * Clear all fields that are re-assigned during tests
   */
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

  public boolean isGenerateCharts() {
    return generateCharts;
  }

  public void setGenerateCharts(boolean generateCharts) {
    this.generateCharts = generateCharts;
  }

  public boolean isGenerateCSVs() {
    return generateCSVs;
  }

  public void setGenerateCSVs(boolean generateCSVs) {
    this.generateCSVs = generateCSVs;
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

  public boolean isPreserveDirectories() {
    return preserveDirectories;
  }

  public void setPreserveDirectories(boolean preserveDirectories) {
    this.preserveDirectories = preserveDirectories;
  }
  
  public Log getLog() {
    return log;
  }

  public void setLog(Log log) {
    this.log = log;
  }

  public Set<String> getSampleNames() {
    return sampleNames;
  }

  public void setSampleNames(Set<String> sampleNames) {
    this.sampleNames = sampleNames;
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
