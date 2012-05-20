package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;
import junit.framework.TestCase;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Tests {@link com.lazerycode.jmeter.analyzer.AnalyzeCommand}
 */
public class AnalyzeCommandTest extends TestCase {

  private File workDir;
  private final boolean cleanup = true; // set this to false if you want to test the results manually
  private static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.getDefault());
  
  @Override
  protected void setUp() throws Exception {

    workDir = new File(new File(System.getProperty("java.io.tmpdir")), getClass().getName());
    workDir.mkdirs();
    cleanDir(workDir);
  }


  @Override
  protected void tearDown() throws Exception {

    if( cleanup ) {
      cleanDir(workDir);
    }
  }

  private void setUpEnvironment(boolean generateCSVs, boolean generateCharts, LinkedHashMap<String, String> patterns, File templateDirectory) {
    ENVIRONMENT.clear();
    ENVIRONMENT.setGenerateCSVs(generateCSVs);
    ENVIRONMENT.setGenerateCharts(generateCharts);
    ENVIRONMENT.setMaxSamples(1000);
    ENVIRONMENT.setTargetDirectory(workDir);
    ENVIRONMENT.setRequestGroups(patterns);
    ENVIRONMENT.setTemplateDirectory(templateDirectory);
    ENVIRONMENT.initializeFreemarkerConfiguration();
    ENVIRONMENT.setResultRenderHelper(new ResultRenderHelper());
  }

  /**
   * Tests the text output
   */
  public void testTextOutput() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-jmeter.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();

    String CRLF = System.getProperty("line.separator");
    
    
    // the output will be in local time zone of the test, so we'll parse it first, then convert back to string
    Date start = parseDate("20111216T145509+0100");
    Date end = parseDate("20111216T145539+0100");

    String textOutput = writer.toString();
    String expected = 
            "warmup" +CRLF+
            "  time: " + toLocal(start) + " - " + toLocal(end) + CRLF+
            "  total duration:       30" + CRLF+
            "  requests:             36049" +CRLF+
            "  requests per second:  1201" +CRLF+
            "  response duration (ms)" +CRLF+
            "    min:                0" + CRLF+
            "    average:            0" + CRLF+
            "    max:                1352" +CRLF+
            "    standard deviation: 7" +CRLF+
            "    quantiles (ms)" +CRLF+
            "         10%        0" +CRLF+
            "         20%        0" +CRLF+
            "         30%        0" +CRLF+
            "         40%        0" +CRLF+
            "         50%        1" +CRLF+
            "         60%        1" +CRLF+
            "         70%        1" +CRLF+
            "         80%        1" +CRLF+
            "         90%        1" +CRLF+
            "         99%        6" +CRLF+
            "       99.9%       19" +CRLF+
            "      100.0%     1352 (max. value)" +CRLF+
            "  response size (bytes)" +CRLF+
            "    total:              750210890" +CRLF+
            "    min:                20480" +CRLF+
            "    average:            20810" +CRLF+
            "    max:                53890" +CRLF+
            "    standard deviation: 3308" +CRLF+
            "  response status codes" +CRLF+
            "    200:               36049 (100%)"+CRLF;
    
    assertEquals("text", expected, textOutput);
  }

  /**
   * Tests the text output with a few unsuccessful samples
   */
  public void testTextOutputSomeErrors() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-someErrors.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();

    String CRLF = System.getProperty("line.separator");


    // the output will be in local time zone of the test, so we'll parse it first, then convert back to string
    Date start = parseDate("20111216T145509+0100");
    Date end = parseDate("20111216T145539+0100");

    String textOutput = writer.toString();
    String expected =
            "warmup" +CRLF+
            "  time: " + toLocal(start) + " - " + toLocal(end) + CRLF+
            "  total duration:       30" + CRLF+
            "  requests:             36034" +CRLF+
            "  requests per second:  1201" +CRLF+
            "  response duration (ms)" +CRLF+
            "    min:                0" + CRLF+
            "    average:            0" + CRLF+
            "    max:                1352" +CRLF+
            "    standard deviation: 7" +CRLF+
            "    quantiles (ms)" +CRLF+
            "         10%        0" +CRLF+
            "         20%        0" +CRLF+
            "         30%        0" +CRLF+
            "         40%        0" +CRLF+
            "         50%        1" +CRLF+
            "         60%        1" +CRLF+
            "         70%        1" +CRLF+
            "         80%        1" +CRLF+
            "         90%        1" +CRLF+
            "         99%        6" +CRLF+
            "       99.9%       19" +CRLF+
            "      100.0%     1352 (max. value)" +CRLF+
            "  response size (bytes)" +CRLF+
            "    total:              749903690" +CRLF+
            "    min:                20480" +CRLF+
            "    average:            20811" +CRLF+
            "    max:                53890" +CRLF+
            "    standard deviation: 3308" +CRLF+
            "  response status codes" +CRLF+
            "    200:               36034 (99.96%)"+CRLF+
            "    403:                   9 (0.02%)"+CRLF+
            "    404:                   6 (0.02%)"+CRLF;

    assertEquals("text", expected, textOutput);
  }

  /**
   * Tests the text output with only unsuccessful samples
   */
  public void testTextOnlyErrors() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-onlyErrors.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();

    String CRLF = System.getProperty("line.separator");


    // the output will be in local time zone of the test, so we'll parse it first, then convert back to string
    Date start = parseDate("20111216T145509+0100");
    Date end = parseDate("20111216T145539+0100");

    String textOutput = writer.toString();

    String expected =
            "warmup" +CRLF+
            "  time: " + toLocal(start) + " - " + toLocal(end) + CRLF+
            "  total duration:       30" + CRLF+
            "  requests:             0" +CRLF+
            "  requests per second:  0" +CRLF+
            "  errors:               100%" +CRLF;

    assertEquals("text", expected, textOutput);
  }

  /**
   * Tests the text output with an empty resultsfile
   */
  public void testTextEmptyOutput() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-empty.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();


    String textOutput = writer.toString();
    String expected = "";

    assertEquals("text", expected, textOutput);
  }

  /**
   * Tests that all result files are available
   *
   * Text, HTML, CSVs and Images
   */
  public void testAllFiles() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-success.xml"));
    LinkedHashMap<String, String> patterns = new LinkedHashMap<java.lang.String, java.lang.String>();
    patterns.put("page", "/main");
    patterns.put("blob", "/main/**");
    Writer writer = new StringWriter();

    setUpEnvironment(true,true, patterns, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();


    List<String> expectedFiles = Arrays.asList(
            "blob-durations.csv", "blob-durations.png", "blob-sizes.csv",
            "page-durations.csv", "page-durations.png", "page-sizes.csv",
            "summary.html", "summary.txt");
    List<String> actualFiles = Arrays.asList(workDir.list());
    Collections.sort(expectedFiles);
    Collections.sort(actualFiles);
    assertEquals("files", expectedFiles, actualFiles );
  }

  /**
   * Tests that certain result files are available
   *
   * No CSVs nor images here
   */
  public void testSomeFiles() throws Exception {

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-success.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();

    List<String> expectedFiles = Arrays.asList("summary.html", "summary.txt");
    List<String> actualFiles = Arrays.asList(workDir.list());
    Collections.sort(expectedFiles);
    Collections.sort(actualFiles);

    assertEquals("files", expectedFiles, actualFiles);
  }

  public void testCustomTemplates() throws Exception {

    //copy template to file system
    File templateDir = new File(workDir,"text");
    templateDir.mkdir();
    File template = initializeFile(templateDir,"main.ftl");

    InputStream is = getClass().getResourceAsStream("/com/lazerycode/jmeter/analyzer/testtemplates/text/main.ftl");
    OutputStream os = new FileOutputStream(template);
    while (is.available() > 0) {
        os.write(is.read());
    }
    os.close();
    is.close();

    //run plugin
    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-jmeter.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false, false, null, workDir);

    new LocalAnalyzeCommand(writer).analyze(data);

    writer.flush();
    writer.close();
    data.close();

    String textOutput = writer.toString();
    String expected = "this is a custom template";

    assertEquals("text", expected, textOutput);
  }


  public void testDownload() throws Exception {
    final String start = "20111216T145509+0100";
    final String end = "20111216T145539+0100";
    
    // create a file to be downloaded
    // contains urlencoded timestamps which are retrieved from jmeter.xml
    File downloadableFile = new File(workDir, String.format("%s.%s.tmp", toLocal(start), toLocal(end)));
    downloadableFile.createNewFile();

    File downloadablePatternFile = new File(workDir, "_FROM_._TO_.tmp");

    Properties remoteResources = new Properties();
    remoteResources.setProperty(downloadablePatternFile.toURI().toString(), "download.txt");

    Reader data = new InputStreamReader(getClass().getResourceAsStream("AnalyzeCommandTest-jmeter.xml"));
    Writer writer = new StringWriter();

    setUpEnvironment(false,false, null, null);

    new LocalAnalyzeCommand(writer).analyze(data);


  }

  //--------------------------------------------------------------------------------------------------------------------

  private void cleanDir(File dir) {

    for( File file : dir.listFiles() ) {
      if(file.isDirectory()) {
        //recurse into directory
        cleanDir(file);
      }

      file.delete();
    }
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

  private static Date parseDate(String dateString) throws ParseException {
    return LOCAL_DATE_FORMAT.parse(dateString);
  }
  
  private static String toLocal(Date date) {
    return LOCAL_DATE_FORMAT.format(date);
  }
  
  private static String toLocal(String dateString) throws ParseException {
    return toLocal(parseDate(dateString));
  }

  //====================================================================================================================

  private class LocalAnalyzeCommand extends AnalyzeCommand {

    private Writer writer;

    LocalAnalyzeCommand(Writer writer) {
      this.writer = writer;
    }


    @Override
    protected void renderTextToStdOut(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {
      resultRenderHelper.renderText(testResults, writer);
    }
  }
}
