package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.config.Environment;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.zip.GZIPInputStream;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Analyzes JMeter XML test report file and generates a report
 *
 * @goal analyze
 *
 * @author Dennis Homann, Arne Franken, Peter Kaul
 */
@SuppressWarnings({"UnusedDeclaration", "FieldCanBeLocal", "JavaDoc"}) // Mojos get their fields set via reflection
public class AnalyzeMojo extends AbstractMojo {
  
  /**
   * The JMeter XML result file to analyze.
   * May be GZiped, must end in .gz then.
   *
   * @parameter expression="${source}"
   * @required
   */
  private File source;

  /**
   * Directory to store result files in
   *
   * @parameter expression="${targetDirectory}" default-value="${project.build.directory}"
   * @required
   */
  private File targetDirectory;

  /**
   * Maximum number of samples to keep (in main memory)
   *
   * @parameter expression="${maxSamples}" default-value="50000"
   */
  private int maxSamples = Environment.DEFAULT_MAXSAMPLES;

  /**
   * True, if CSV files with detailed information for each request should be generated
   *
   * @parameter expression="${details}" default-value="true"
   */
  private boolean details;

  /**
   * True, if charts should be generated
   *
   * @parameter expression="${charts}" default-value="true"
   */
  private boolean charts;

  /**
   * Request groups as a mapping from "group name" to "ant pattern".
   * A request uri that matches an ant pattern will be associated with the group name.
   * Request details, charts and CSV files are generated per requestGroup.
   *
   * If not set, the threadgroup name of the request will be used.
   *
   * @parameter
   */
  @SuppressWarnings("all") // avoid "Loose coupling" violation. LinkedHashMap is used to keep order
  private LinkedHashMap<String,String> requestGroups;

  /**
   * Like 'requestGroups' but formatted as a string "name1=pattern1,name2=pattern2,..."
   * requestGroups and requestGroupsString are mutually exclusive.
   *
   * @parameter expression="${requestGroups}"
   */
  private String requestGroupsString;

  /**
   * URLs of resources to be downloaded and to be stored in target directory.
   * Mapping from URL to filename.
   * URL may contain placeholders _FROM_ and _TO_. Those placeholders will be replaced by ISO8601 formatted timestamps
   * (e.g. 20120116T163600%2B0100) that are extracted from the JMeter result file (min/max time)
   *
   * @parameter
   */
  private Properties remoteResources;

  /**
   * Template directory where custom freemarker templates are stored.
   * Freemarker templates are used for all generated output. (CSV files, HTML files, console output)
   *
   * Templates must be stored in one of the following three subfolders of the templateDirectory:
   *
   * csv
   * html
   * text
   *
   * The entry template must be called "main.ftl".
   *
   * For example,
   * <templateDirectory>/text/main.ftl will be used for generating the console output.
   *
   * @parameter
   */
  private File templateDirectory;

  @Override
  public void execute() throws MojoExecutionException, MojoFailureException {

    assertRequestGroupsInitialization();

    initializeEnvironment();

    try {

      Reader data;
      if(source.getName().endsWith(".gz")) {
        data = new InputStreamReader(new GZIPInputStream(new FileInputStream(source)));
      }
      else {
        data = new FileReader(source);
      }

      try {
        new AnalyzeCommand().analyze(data);
      }
      finally {
        data.close();
      }

    }
    catch (Exception e) {
      throw new MojoExecutionException("Error analyzing", e);
    }

  }

  //====================================================================================================================

  private void initializeEnvironment() {
    ENVIRONMENT.setCharts(charts);
    ENVIRONMENT.setDetails(details);
    ENVIRONMENT.setMaxSamples(maxSamples);
    ENVIRONMENT.setRemoteResources(remoteResources);
    ENVIRONMENT.setRequestGroups(requestGroups);
    ENVIRONMENT.setTemplateDirectory(templateDirectory);
    ENVIRONMENT.setTargetDirectory(targetDirectory);
    ENVIRONMENT.initializeFreemarkerConfiguration();
    ENVIRONMENT.setResultRenderHelper(new ResultRenderHelper());
  }

  /**
   * Make sure that #requestGroups is filled properly
   *
   * @throws MojoFailureException
   */
  private void assertRequestGroupsInitialization() throws MojoFailureException {

    if( requestGroups != null && requestGroupsString != null ) {
          throw new MojoFailureException("'requestGroups' and 'requestGroupsString' must not be defined at the same time.");
        }

    if( requestGroups == null && requestGroupsString != null ) {

      // parse requestGroupsString into requestGroups
      requestGroups = new LinkedHashMap<String, String>();
      StringTokenizer tok = new StringTokenizer(requestGroupsString, ",");
      while( tok.hasMoreElements() ) {

        String entry = tok.nextToken().trim();
        int commaIndex = entry.indexOf('=');
        if( commaIndex == -1 ) {
          throw new MojoFailureException("Invalid format for 'requestGroupsString'. Expecting 'name=pattern,name=pattern,...' but got "+requestGroupsString);
        }

        String key = entry.substring(0, commaIndex).trim();
        String value = entry.substring(commaIndex+1).trim();
        requestGroups.put(key, value);
      }

      getLog().debug("Parsed "+requestGroupsString+" into "+requestGroups);
    }

  }


}
