package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.config.Environment;
import com.lazerycode.jmeter.analyzer.writer.ChartWriter;
import com.lazerycode.jmeter.analyzer.writer.DetailsToCsvWriter;
import com.lazerycode.jmeter.analyzer.writer.DetailsToHtmlWriter;
import com.lazerycode.jmeter.analyzer.writer.HtmlWriter;
import com.lazerycode.jmeter.analyzer.writer.SummaryTextToFileWriter;
import com.lazerycode.jmeter.analyzer.writer.Writer;

import junit.framework.TestCase;

import org.apache.commons.io.FileUtils;
import org.apache.maven.plugin.logging.SystemStreamLog;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.normalizeFileContents;
import static com.lazerycode.jmeter.analyzer.writer.WriterTestHelper.toLocal;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsEqual.equalTo;
import static org.junit.Assert.assertThat;

/**
 * Tests {@link ResultAnalyzer}
 */
public class ResultAnalyzerTest extends TestCase {

  private File workDir;
  private final boolean cleanup = false; // set this to false if you want to test the results manually
  private static final SimpleDateFormat LOCAL_DATE_FORMAT = new SimpleDateFormat("yyyyMMdd'T'HHmmssZ", Locale.getDefault());
  private static final String PACKAGE_PATH = "/com/lazerycode/jmeter/analyzer/resultanalyzer/";

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

  private void setUpEnvironment(boolean generateCSVs, boolean generateCharts, List<RequestGroup> patterns, File templateDirectory) {
    ENVIRONMENT.clear();
    ENVIRONMENT.setGenerateDetails(generateCSVs);
    ENVIRONMENT.setGenerateCharts(generateCharts);
    ENVIRONMENT.setMaxSamples(1000);
    ENVIRONMENT.setTargetDirectory(workDir);
    ENVIRONMENT.setRequestGroups(patterns);
    ENVIRONMENT.setTemplateDirectory(templateDirectory);
    ENVIRONMENT.initializeFreemarkerConfiguration();
    ENVIRONMENT.setLog(new SystemStreamLog());
    ENVIRONMENT.setSampleNames(new HashSet<String>(Arrays.asList(new String[]{Environment.HTTPSAMPLE_ELEMENT_NAME, Environment.SAMPLE_ELEMENT_NAME})));

    //set up default writers
    List<Writer> writers = new ArrayList<Writer>();
    //do not write text to stdout during tests.
//    writers.add(new SummaryTextToStdOutWriter());
    writers.add(new SummaryTextToFileWriter());
    writers.add(new HtmlWriter());
    if(ENVIRONMENT.isGenerateDetails()) {
      writers.add(new DetailsToCsvWriter());
      writers.add(new DetailsToHtmlWriter());
    }
    if(ENVIRONMENT.isGenerateCharts()) {
      writers.add(new ChartWriter());
    }
    ENVIRONMENT.setWriters(writers);

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
    resultFiles.add("test.txt");
    resultFiles.add("test.html");
    if(additionalFiles != null ) {
      resultFiles.addAll(additionalFiles);
    }
    String localPackagePath = PACKAGE_PATH + packagePath;

    //2. ---- run plugin
    Reader data = new InputStreamReader(getClass().getResourceAsStream(localPackagePath+"test.jtl"));
    //commandline output does not matter during tests and is routed to a NullWriter
    new ResultAnalyzer(null,"test").analyze(data);
    data.close();

    //3. ---- assert that result files are correct
    for(String resultFile : resultFiles) {

      File actual = new File(workDir+"/"+resultFile);
      File expected = new File(getClass().getResource(localPackagePath+resultFile).getFile());

      assertTrue("Expected file "+resultFile+" does not exist: ",
              expected.exists());

      //normalize text files
      String actualContent = normalizeFileContents(actual);
      String expectedContent = normalizeFileContents(expected);

      assertThat("lines in file "+resultFile+" do not match: ",
              actualContent,
              is(equalTo(expectedContent)));
    }

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
}
