package com.lazerycode.jmeter.analyzer.config;

import com.lazerycode.jmeter.analyzer.CheckResult;
import com.lazerycode.jmeter.analyzer.ConfigurationCharts;
import com.lazerycode.jmeter.analyzer.RequestGroup;
import com.lazerycode.jmeter.analyzer.writer.Writer;

import freemarker.ext.beans.BeansWrapper;
import freemarker.template.Configuration;

import org.apache.maven.plugin.logging.Log;

import java.io.File;
import java.util.List;
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
  private boolean generateDetails;

  /**
   * Template directory where custom freemarker templates are stored.
   */
  private File templateDirectory;
  private Properties remoteResources;
  private String remoteResourcesFromUntilDateFormat;
  private Set<String> sampleNames;
  private List<RequestGroup> requestGroups;
  private int maxSamples = DEFAULT_MAXSAMPLES;
  private Configuration configuration;
  private File targetDirectory;
  private Log log;
  private List<Writer> writers;
  private ConfigurationCharts configurationCharts = new ConfigurationCharts();
  private CheckResult checkResult = new CheckResult();

  /**
   * If true, preserve the relative part of the result file's path
   */
  private boolean preserveDirectories;

  /**
   * Clear all fields that are re-assigned during tests
   */
  public void clear() {
    this.templateDirectory = null;
    this.remoteResources = null;
    this.requestGroups = null;
    this.maxSamples = DEFAULT_MAXSAMPLES;
    this.configuration = null;
    this.targetDirectory = null;
    this.remoteResourcesFromUntilDateFormat = ISO8601_FORMAT;
    this.configurationCharts = new ConfigurationCharts();
    this.checkResult = new CheckResult();
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

  public boolean isGenerateDetails() {
    return generateDetails;
  }

  public void setGenerateDetails(boolean generateDetails) {
    this.generateDetails = generateDetails;
  }

  public File getTemplateDirectory() {
    return templateDirectory;
  }

  public void setTemplateDirectory(File templateDirectory) {
    this.templateDirectory = templateDirectory;
  }

  public Properties getRemoteResources() {
    return remoteResources;
  }

  public void setRemoteResources(Properties remoteResources) {
    this.remoteResources = remoteResources;
  }

  public List<RequestGroup> getRequestGroups() {
    return requestGroups;
  }

  public void setRequestGroups(List<RequestGroup> requestGroups) {
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

  public List<Writer> getWriters() {
    return writers;
  }

  public void setWriters(List<Writer> writers) {
    this.writers = writers;
  }

  public ConfigurationCharts getConfigurationCharts() {
    return configurationCharts;
  }

  public void setConfigurationCharts(ConfigurationCharts configurationCharts) {
    if (null == configurationCharts) {
      configurationCharts = new ConfigurationCharts();
    }
    this.configurationCharts = configurationCharts;
  }

  public CheckResult getCheckResult() {
    return checkResult;
  }

  public void setCheckResult(CheckResult checkResult) {
      if (null == checkResult) {
          checkResult = new CheckResult();
      }
      this.checkResult = checkResult;
  }

  public String getRemoteResourcesFromUntilDateFormat() {
	return remoteResourcesFromUntilDateFormat;
  }

  public void setRemoteResourcesFromUntilDateFormat(
		String remoteResourcesFromUntilDateFormat) {
	this.remoteResourcesFromUntilDateFormat = remoteResourcesFromUntilDateFormat;
  }

}
