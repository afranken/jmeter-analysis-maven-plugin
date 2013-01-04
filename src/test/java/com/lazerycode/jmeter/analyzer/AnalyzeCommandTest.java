package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.config.Environment;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import freemarker.template.TemplateException;
import junit.framework.TestCase;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.NullWriter;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests {@link com.lazerycode.jmeter.analyzer.AnalyzeCommand}
 */
public class AnalyzeCommandTest extends TestCase {

  private File workDir;
  private final boolean cleanup = true; // set this to false if you want to test the results manually
  private static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.getDefault());
  private static final String PACKAGE_PATH = "/com/lazerycode/jmeter/analyzer/analyzecommand/";
  
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

  //--------------------------------------------------------------------------------------------------------------------

  /**
   * Tests the text output with only successful samples
   */
  public void testTextOutputSuccess() throws Exception {

    String localPackagePath = "/success/";

    setUpEnvironment(false, false, null, null);

    testOutput(localPackagePath, null);
  }

  /**
   * Tests the text output with a few unsuccessful samples
   */
  public void testTextOutputSomeErrors() throws Exception {

    String localPackagePath = "/someerrors/";

    setUpEnvironment(false,false, null, null);

    testOutput(localPackagePath, null);
  }

  /**
   * Tests the text output with only unsuccessful samples
   */
  public void testTextOnlyErrors() throws Exception {

    String localPackagePath = "/onlyerrors/";

    setUpEnvironment(false, false, null, null);

    testOutput(localPackagePath, null);
  }

  /**
   * Tests the text output with an empty results file
   */
  public void testTextEmptyOutput() throws Exception {

    String localPackagePath = "/empty/";

    setUpEnvironment(false,false, null, null);

    testOutput(localPackagePath, null);
  }

  /**
   * Tests the text output where only "customSample" nodes are processed
   */
  public void testSampleNames() throws Exception {

    String localPackagePath = "/samplenames/";

    setUpEnvironment(false,false, null, null);
    //jmeter-result.jtl does not contain <sample> elements.
    ENVIRONMENT.setSampleNames(Collections.singleton("customSample"));

    testOutput(localPackagePath, null);
  }

  /**
   * Tests that all result files are available
   *
   * Text, HTML, CSVs and Images
   */
  public void testAllFiles() throws Exception {

    String localPackagePath = "/allfiles/";

    LinkedHashMap<String, String> patterns = new LinkedHashMap<java.lang.String, java.lang.String>();
    patterns.put("page", "/main");
    patterns.put("blob", "/main/**");

    setUpEnvironment(true, true, patterns, null);


    List<String> fileNames = new ArrayList<String>();
    fileNames.add("blob-durations-summary.csv");
    fileNames.add("blob-sizes-summary.csv");
    fileNames.add("page-durations-summary.csv");
    fileNames.add("page-sizes-summary.csv");
    //TODO: skip blob comparison for now, doesn't work across all platforms
    //fileNames.add("page-durations-summary.png");
    //fileNames.add("blob-durations-summary.png");

    testOutput(localPackagePath, fileNames);

  }

  /**
   * Test output with custom template
   */
  public void testCustomTemplates() throws Exception {

    String localPackagePath = "/testtemplates/";

    //copy template to file system
    File templateDir = new File(workDir,"text");
    templateDir.mkdir();
    File template = initializeFile(templateDir,"main.ftl");

    InputStream is = getClass().getResourceAsStream(PACKAGE_PATH+"testtemplates/text/main.ftl");
    OutputStream os = new FileOutputStream(template);
    while (is.available() > 0) {
        os.write(is.read());
    }
    os.close();
    is.close();


    setUpEnvironment(false, false, null, workDir);

    testOutput(localPackagePath, null);
  }


  /**
   * tests that file is downloaded and has the right content
   */
  public void testDownload() throws Exception {
    String localPackagePath = "/download/";

    final String start = "20111216T145509+0100";
    final String end = "20111216T145539+0100";
    
    // create a file to be downloaded
    // contains urlencoded timestamps which are formatted as if retrieved from jmeter.xml
    File downloadableFile = initializeFile(workDir, String.format("%s.%s.tmp", toLocal(start), toLocal(end)));
    FileUtils.write(downloadableFile,"contents");

    File downloadablePatternFile = new File(workDir, "_FROM_._TO_.tmp");

    Properties remoteResources = new Properties();
    remoteResources.setProperty(downloadablePatternFile.toURI().toString(), "download.txt");

    setUpEnvironment(false,false, null, null);
    ENVIRONMENT.setRemoteResources(remoteResources);

    testOutput(localPackagePath, null);


    File downloadedFile = new File(workDir, "download.txt");

    assertTrue("file was not successfully downloaded: ",
            downloadedFile.exists());
    assertTrue("file doesn't have the right content: ",
            FileUtils.contentEquals(downloadableFile, downloadedFile));
  }

  //--------------------------------------------------------------------------------------------------------------------

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
    ENVIRONMENT.setLog(new SystemStreamLog());
    ENVIRONMENT.setSampleNames(new HashSet<String>(Arrays.asList(new String[]{Environment.HTTPSAMPLE_ELEMENT_NAME, Environment.SAMPLE_ELEMENT_NAME})));
  }

  /**
   * Output test code that is used by most test methods.
   * {@link com.lazerycode.jmeter.analyzer.config.Environment#ENVIRONMENT} must be reset/initialized before calling this method.
   *
   * @param packagePath path relative to {@link #PACKAGE_PATH}
   * @throws Exception
   */
  private void testOutput(String packagePath, List<String> additionalFiles) throws Exception {

    //1. ---- initialization
    List<String> resultFiles = new ArrayList<String>();
    resultFiles.add("summary.txt");
    resultFiles.add("summary.html");
    if(additionalFiles != null ) {
      resultFiles.addAll(additionalFiles);
    }
    String localPackagePath = PACKAGE_PATH + packagePath;

    //2. ---- run plugin
    Reader data = new InputStreamReader(getClass().getResourceAsStream(localPackagePath+"jmeter-result.jtl"));
    //commandline output does not matter during tests and is routed to a NullWriter
    new LocalAnalyzeCommand(new NullWriter()).analyze(data);
    data.close();

    //3. ---- assert that result files are correct
    for(String resultFile : resultFiles) {

      File actual = new File(workDir+"/"+resultFile);
      File expected = new File(getClass().getResource(localPackagePath+resultFile).getFile());

      assertTrue("Expected file does not exist: ",
              expected.exists());

      //normalize text files
      String actualContent = normalizeFileContents(actual);
      String expectedContent = normalizeFileContents(expected);

      assertThat("lines in TXT file do not match: ",
              actualContent,
              is(equalTo(expectedContent)));
    }

  }

  /**
   * Strip line ends from String contents of a file so that contents can be compared on different platforms.
   *
   * @param file
   * @return normalized String
   * @throws IOException
   */
  private String normalizeFileContents(File file) throws IOException, ParseException {

    String content = FileUtils.readFileToString(file,"UTF-8");

    //replace line endings
    content = content.replaceAll("(\\r\\n|\\r|\\n)", "");

    //replace date with date converted to the local timezone
    Pattern datePattern = Pattern.compile("\\d\\d\\d\\d\\d\\d\\d\\dT\\d\\d\\d\\d\\d\\d+\\d\\d\\d\\d");
    Matcher matcher = datePattern.matcher(content);
    while(matcher.matches()) {
      matcher.replaceFirst(toLocal(matcher.group()));
    }

    return matcher.toString();
  }

  /**
   * Remove all contents (including subdirectories) from given directory
   * @param dir
   */
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

  /**
   * Implementation that supports passing a writer into the object.
   * Per default, {@link AnalyzeCommand} would write output to {@link System#out}
   */
  private class LocalAnalyzeCommand extends AnalyzeCommand {

    private Writer writer;

    LocalAnalyzeCommand(Writer writer) {
      super(null);
      this.writer = writer;
    }

    @Override
    protected void renderTextToStdOut(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {
      resultRenderHelper.renderText(testResults, writer);
    }
  }
}
