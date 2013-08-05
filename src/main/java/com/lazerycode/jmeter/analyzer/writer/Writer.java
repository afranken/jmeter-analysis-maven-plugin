package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;

import java.io.IOException;
import java.util.Map;

/**
 * Implementations of this interface will be called by
 * {@link com.lazerycode.jmeter.analyzer.ResultAnalyzer ResultAnalyzer} for each
 * discovered / configured {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#requestGroups RequestGroup} in
 * each discovered results file.
 *
 * Custom implementations should extend {@link WriterBase} to make use of it's convenience methods.
 */
public interface Writer {

  /**
   * Write test results to desired output. See {@link WriterBase},
   * {@link com.lazerycode.jmeter.analyzer.util.TemplateUtil TemplateUtil}
   * and {@link com.lazerycode.jmeter.analyzer.util.FileUtil FileUtil} for convenience methods.
   *
   * @param testResults the results to generate output for
   * @throws IOException
   * @throws TemplateException
   */
  void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException;

  /**
   * The relative path below {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#targetDirectory} to create
   * the files in if {@link com.lazerycode.jmeter.analyzer.config.Environment#preserveDirectories} is true
   *
   * @return the relative path
   */
  String getResultDataFileRelativePath();

  /**
   * Setter is called by {@link com.lazerycode.jmeter.analyzer.ResultAnalyzer ResultAnalyzer} before calling
   * {@link #write(java.util.Map)}.
   */
  void setResultDataFileRelativePath(String resultDataFileRelativePath);

  /**
   * @return The file name of the analyzed results file.
   */
  String getFileName();

  /**
   * Setter is called by {@link com.lazerycode.jmeter.analyzer.ResultAnalyzer ResultAnalyzer} before calling
   * {@link #write(java.util.Map)}.
   */
  void setFileName(String fileName);
}
