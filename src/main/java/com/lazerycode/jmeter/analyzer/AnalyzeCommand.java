package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.parser.JMeterResultParser;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import freemarker.template.TemplateException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Properties;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.config.Environment.ISO8601_FORMAT;


/**
 * Command line tool for analyzing jmeter result
 *
 * @see AnalyzeMojo
 *
 * @author Dennis Homann, Arne Franken, Peter Kaul
 */
public class AnalyzeCommand {

  private static final String TOKEN_FROM = "_FROM_";
  private static final String TOKEN_TO = "_TO_";
  private static final int READ_BUFFER = 1024;

  private static final String SIZES_CSV_SUFFIX = "-sizes.csv";
  private static final String DURATIONS_CSV_SUFFIX = "-durations.csv";
  private static final String DURATIONS_PNG_FILE_SUFFIX = "-durations.png";

  private static final String SUMMARY_FILE_NAME = "summary";
  private static final String SUMMARY_TXT_FILE_NAME = SUMMARY_FILE_NAME + ".txt";
  private static final String SUMMARY_HTML_FILE_NAME = SUMMARY_FILE_NAME + ".html";

  protected ResultRenderHelper resultRenderHelper;

  private File targetDirectory;
  private Properties remoteResources;
  private boolean generateCharts;
  private boolean generateCSVs;

  public AnalyzeCommand() {

    this.resultRenderHelper = ENVIRONMENT.getResultRenderHelper();
    this.generateCharts = ENVIRONMENT.isGenerateCharts();
    this.generateCSVs = ENVIRONMENT.isGenerateCSVs();
    this.remoteResources = ENVIRONMENT.getRemoteResources();
    this.targetDirectory = ENVIRONMENT.getTargetDirectory();
  }

  /**
   * Analyzes a JMeter XML results file
   *
   * @param jmeterResult    The jmeter XML result file
   *
   * @throws Exception When a problem occurs
   */
  public void analyze(Reader jmeterResult) throws Exception {

    Map<String, AggregatedResponses> testResults = new JMeterResultParser().aggregate(jmeterResult);

    renderTextToStdOut(testResults);
    renderTextAsFile(testResults);

    renderHTML(testResults);

    if(generateCSVs || generateCharts) {
      // Process every AggregatedResponse
      for (Map.Entry<String, AggregatedResponses> entry : testResults.entrySet()) {

        String name = entry.getKey();
        AggregatedResponses aggregatedResponses = entry.getValue();

        if (generateCSVs) {

          // write durations by uri
          String durationsFilename = urlEncode(name) + DURATIONS_CSV_SUFFIX;
          writeCSVs(durationsFilename,aggregatedResponses.getDurationByUri());

          // write size by uri
          String sizeFilename = urlEncode(name) + SIZES_CSV_SUFFIX;
          writeCSVs(sizeFilename, aggregatedResponses.getSizeByUri());
        }

        if (generateCharts) {
          writeChart(name, aggregatedResponses);
        }

      }
    }


    // --- download resources
    if (remoteResources != null) {
      long[] startEnd = getStartEnd(testResults.values());
      readResources(remoteResources, targetDirectory, startEnd[0], startEnd[1]);
    }
  }


  //--------------------------------------------------------------------------------------------------------------------

  /**
   * Render results as text to System.out
   *
   * @param testResults Map to generate output from
   *
   * @throws IOException
   * @throws TemplateException
   */
  protected void renderTextToStdOut(Map<String, AggregatedResponses> testResults)
          throws IOException, TemplateException {

    PrintWriter out = new PrintWriter(System.out, true);

    resultRenderHelper.renderText(testResults, out);

    out.flush();
    out.close();
  }

  //====================================================================================================================

  /**
   * Render results as text to a file
   *
   * @param testResults Map to generate output from
   *
   * @throws IOException
   * @throws TemplateException
   */
  private void renderTextAsFile(Map<String, AggregatedResponses> testResults)
          throws IOException, TemplateException {

    FileWriter out = new FileWriter(initializeFile(targetDirectory, SUMMARY_TXT_FILE_NAME));
    PrintWriter text = new PrintWriter(out, false);

    resultRenderHelper.renderText(testResults, text);

    text.flush();
    text.close();
    out.close();
  }

  /**
   * Render results as HTML file
   *
   * @param testResults Map to generate output from
   *
   * @throws IOException
   * @throws TemplateException
   */
  private void renderHTML(Map<String, AggregatedResponses> testResults)
          throws IOException, TemplateException {

    FileWriter w = new FileWriter(initializeFile(targetDirectory, SUMMARY_HTML_FILE_NAME));
    PrintWriter html = new PrintWriter(w, false);

    resultRenderHelper.renderHTML(testResults, html);

    html.flush();
    html.close();
    w.close();
  }

  /**
   * Write CSVs with detailed information
   *
   * @param name filename
   * @param data results to generate CSV from
   *
   * @throws IOException
   */
  private void writeCSVs(String name, Map<String, Samples> data)
          throws IOException, TemplateException {

    FileWriter w = new FileWriter(initializeFile(targetDirectory, name));
    PrintWriter durationsCSV = new PrintWriter(w, true);

    resultRenderHelper.writeCSV(data, durationsCSV);

    durationsCSV.flush();
    durationsCSV.close();
    w.close();
  }

  /**
     * Generate Charts
     *
     * @param name identifier used as part of the name
     * @param aggregatedResponses results to generate CSV from
     *
     * @throws IOException
     */
  private void writeChart(String name, AggregatedResponses aggregatedResponses) throws IOException {

    String fileName = urlEncode(name) + DURATIONS_PNG_FILE_SUFFIX;
    File requestChartFile = initializeFile(targetDirectory, fileName);
    Samples aggregatedResult = aggregatedResponses.getDuration();
    ResultRenderHelper.renderChart(name, aggregatedResult, requestChartFile);
  }

  /**
   * URLEncode given String
   * @see URLEncoder#encode(String, String)
   *
   * @param name string to encode
   *
   * @return encoded String
   *
   * @throws UnsupportedEncodingException
   */
  private String urlEncode(String name) throws UnsupportedEncodingException {
    return URLEncoder.encode(name, "ISO-8859-1");
  }

  /**
   * Get start/end timestamp from a list of results
   *
   * @param testResults The results
   * @return an array containing 2 elements: [startTimeStamp, endTimeStamp]
   */
  private long[] getStartEnd(Collection<AggregatedResponses> testResults) {

    // compute min max
    long from = Long.MAX_VALUE;
    long to = 0;
    for (AggregatedResponses entry : testResults) {

      if (from > entry.getStart()) {
        from = entry.getStart();
      }
      if (to < entry.getEnd()) {
        to = entry.getEnd();
      }
    }

    return new long[]{from, to};
  }

  /**
   * Create and return file of given name in given directory
   */
  private File initializeFile(File dir, String name) throws IOException {
    File result = new File(dir, name);

    if (!result.getParentFile().mkdirs() && !result.getParentFile().exists()) {
      throw new IOException("Cannot create directories: " + result.getParentFile().getAbsolutePath());
    }

    if (result.exists() && !result.delete()) {
      throw new IOException("Failed to delete file: " + result.getAbsolutePath());
    }

    if (!result.createNewFile()) {
      throw new IOException("Failed to create file: " + result.getAbsolutePath());
    }

    return result;
  }


  /**
   * Reads in a set of remote resources
   *
   * @param remoteResources The resources as mapping URL to file name
   * @param targetDir       The dir to store the resources
   * @param start           start timestamp to send
   * @param end             end timestamp to send
   * @throws IOException If reading fails
   */
  private void readResources(Properties remoteResources, File targetDir, long start, long end) throws IOException {

    SimpleDateFormat dateFormat = new SimpleDateFormat(ISO8601_FORMAT);
    String fromString = urlEncode(dateFormat.format(new Date(start)));
    String endString = urlEncode(dateFormat.format(new Date(end)));

    for (String url : remoteResources.stringPropertyNames()) {

      String fullUrl = url.replace(TOKEN_FROM, fromString);
      fullUrl = fullUrl.replace(TOKEN_TO, endString);

      String fileName = remoteResources.getProperty(url);

      File file = initializeFile(targetDir, fileName);
      read(fullUrl, file);
    }
  }


  /**
   * Reads a remote resource and stores it as a file
   *
   * @param url    URL to read from
   * @param target target file to write the URL contents to
   * @throws java.io.IOException if the URL cannot be read or the file cannot be written
   */
  private void read(String url, File target) throws IOException {
    try {
      URLConnection connection = new URL(url).openConnection();

      InputStream in = connection.getInputStream();
      try {
        OutputStream out = new FileOutputStream(target);
        try {
          byte[] buffer = new byte[READ_BUFFER];
          int len;
          while ((len = in.read(buffer)) > -1) {
            out.write(buffer, 0, len);
          }
        }
        finally {
          out.close();
        }
      }
      finally {
        in.close();
      }
    }
    catch (IOException e) {
      throw new IOException("Error writing " + url + " to " + target, e);
    }
  }
}
