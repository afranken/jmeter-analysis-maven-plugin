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
  private LinkedHashMap<String,String> requestGroups;
  private int maxSamples = DEFAULT_MAXSAMPLES;
  private Configuration configuration;
  private File targetDirectory;

	private boolean parseOnlyHttpSamples;
	/**
	 * If true, we should preserve the relative part ot result file's path
	 */
	private boolean preserveOutputDirStructure;

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

	public boolean isPreserveOutputDirStructure() {
		return preserveOutputDirStructure;
	}

	public void setPreserveOutputDirStructure(boolean preserveOutputDirStructure) {
		this.preserveOutputDirStructure = preserveOutputDirStructure;
	}

	public boolean isParseOnlyHttpSamples() {
		return parseOnlyHttpSamples;
	}

	public void setParseOnlyHttpSamples(boolean parseOnlyHttpSamples) {
		this.parseOnlyHttpSamples = parseOnlyHttpSamples;
	}

}
