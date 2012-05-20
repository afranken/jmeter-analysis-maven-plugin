package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.config.Environment;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.LinkedHashMap;
import java.util.Properties;
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
   * An AntPath-Style pattern matching a JMeter XML result file to analyze. Must be a fully qualified path.
   * File may be GZiped, must end in .gz then.
   *
   * @parameter expression="${source}"
   * @required
   */
  private String source;

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
   * @parameter expression="${generateCSVs}" default-value="true"
   */
  private boolean generateCSVs;

  /**
   * True, if charts should be generated
   *
   * @parameter expression="${generateCharts}" default-value="true"
   */
  private boolean generateCharts;

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

    initializeEnvironment();

    try {

      PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

      Resource[] resources = resolver.getResources("file:"+source);

      if(resources.length == 0) {
        //no JMeter result file found, makes no sense to go on
        throw new IllegalArgumentException("Property source not set correctly, no JMeter Result XML file found matching "+source);
      }

      //get first file for now.
      //TODO: what to do if there are more files matching the pattern?
      File resource = resources[0].getFile();

      Reader data;
      if(resource.getName().endsWith(".gz")) {
        data = new InputStreamReader(new GZIPInputStream(new FileInputStream(resource)));
      }
      else {
        data = new FileReader(resource);
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
    ENVIRONMENT.setGenerateCharts(generateCharts);
    ENVIRONMENT.setGenerateCSVs(generateCSVs);
    ENVIRONMENT.setMaxSamples(maxSamples);
    ENVIRONMENT.setRemoteResources(remoteResources);
    ENVIRONMENT.setRequestGroups(requestGroups);
    ENVIRONMENT.setTemplateDirectory(templateDirectory);
    ENVIRONMENT.setTargetDirectory(targetDirectory);
    ENVIRONMENT.initializeFreemarkerConfiguration();
    ENVIRONMENT.setResultRenderHelper(new ResultRenderHelper());
  }

}
