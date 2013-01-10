package com.lazerycode.jmeter.analyzer.writer;

import java.io.File;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Abstract implementation of a writer providing convenience methods.
 * Custom {@link Writer} implementations should extend this class.
 */
public abstract class AbstractWriter implements Writer {

  private static final String CSV_EXT = ".csv";
  private static final String TXT_EXT = ".txt";
  private static final String PNG_EXT = ".png";
  private static final String HTML_EXT = ".html";
  private static final String DURATIONS = "-durations-";
  private static final String SIZES = "-sizes-";

  protected String fileName = "summary";
  protected String summaryTxtFileName = fileName + TXT_EXT;
  protected String summaryHtmlFileName = fileName + HTML_EXT;

  protected String sizesCsvSuffix = SIZES + fileName + CSV_EXT;
  protected String sizesHtmlSuffix = SIZES + fileName + HTML_EXT;
  protected String sizesPngFileSuffix = SIZES + fileName + PNG_EXT;
  protected String durationsCsvSuffix = DURATIONS + fileName + CSV_EXT;
  protected String durationsHtmlSuffix = DURATIONS + fileName + HTML_EXT;
  protected String durationsPngFileSuffix = DURATIONS + fileName + PNG_EXT;

  private String resultDataFileRelativePath;

  public String getFileName() {
    return fileName;
  }

  public void setFileName(String fileName) {

    this.fileName = fileName;
    this.summaryTxtFileName = this.fileName + TXT_EXT;
    this.summaryHtmlFileName = this.fileName + HTML_EXT;
    this.sizesCsvSuffix = SIZES + this.fileName + CSV_EXT;
    this.sizesHtmlSuffix = SIZES + this.fileName + HTML_EXT;
    this.sizesPngFileSuffix = SIZES + this.fileName + PNG_EXT;
    this.durationsCsvSuffix = DURATIONS + this.fileName + CSV_EXT;
    this.durationsHtmlSuffix = DURATIONS + this.fileName + HTML_EXT;
    this.durationsPngFileSuffix = DURATIONS + this.fileName + PNG_EXT;
  }

  protected File getTargetDirectory() {
    return ENVIRONMENT.getTargetDirectory();
  }

  public String getResultDataFileRelativePath() {
    return resultDataFileRelativePath;
  }

  public void setResultDataFileRelativePath(String resultDataFileRelativePath) {
    this.resultDataFileRelativePath = resultDataFileRelativePath;
  }

}
