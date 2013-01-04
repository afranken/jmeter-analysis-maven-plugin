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
     * Should we process all files found by pattern used in ${source}?
     * Defaults to false for following reasons:
     * - Previously we only processed the first file so default functionality is consistent with previous versions
     * - Processing everything will increase run time, that should be an explicit choice to keep things fast by default
     *
     * @parameter expression="${processAllFilesFound}" default-value="false"
     */
    private boolean processAllFilesFound;

  /**
   * True, if the directory structure relative to {@link #source} should be preserved during output.
   * Defaults to false for backward compatibility
   *
   * @parameter expression="${preserveDirectories}" default-value="false"
   */
  private boolean preserveDirectories;

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
        getLog().info(" ");
        getLog().info("-------------------------------------------------------");
        getLog().info(" A N A L Y S I N G    J M E T E R    R E S U L T S");
        getLog().info("-------------------------------------------------------");
        getLog().info(" ");

        initializeEnvironment();

        try {

            CustomPathMatchingResourcePatternResolver resolver = new CustomPathMatchingResourcePatternResolver();
            Resource[] resultDataFiles = resolver.getResources("file:" + source);
            String rootPath = resolver.getRootDir(source);

            //No JMeter result file found, makes no sense to go on
            if (resultDataFiles.length == 0) {
              throw new MojoExecutionException("Property source not set correctly, no JMeter Result XML file found matching " + source);
            }

            for (int dataFileIdentifier = 0; dataFileIdentifier < resultDataFiles.length; dataFileIdentifier++) {

                //Drop out of the loop after processing first file if we only want to process the first file found.
                if(dataFileIdentifier == 1 && !processAllFilesFound) {
                  break;
                }

                File resultDataFile = resultDataFiles[dataFileIdentifier].getFile();
                getLog().info("Analysing '" + resultDataFile.getName() + "'...");

                Reader resultData;
                if (resultDataFile.getName().endsWith(".gz")) {
                    resultData = new InputStreamReader(new GZIPInputStream(new FileInputStream(resultDataFile)));
                }
                else {
                    resultData = new FileReader(resultDataFile);
                }

                try {

                    String resultDataFileName = resultDataFile.getName();

                    String relativePath = null;
                    if(preserveDirectories) {
                      //get relative path from source pattern to the resultDataFile
                      relativePath = resultDataFile.getAbsolutePath().replace(rootPath,"").replace(resultDataFileName,"");
                    }

                    AnalyzeCommand reportAnalyser = new AnalyzeCommand(relativePath);

                    //do not use file extension
                    reportAnalyser.setSummaryFilename(resultDataFileName.substring(0, resultDataFileName.lastIndexOf(".")));

                    reportAnalyser.analyze(resultData);
                }
                finally {
                    resultData.close();
                }

                getLog().info("Results Generated for '" + resultDataFile.getName() + "'.");
                getLog().info(" ");
            }
        }
        catch (MojoExecutionException mee){
            throw mee;
        }
        catch (MojoFailureException mfe){
            throw mfe;
        }
        catch (Exception e) {
            throw new MojoExecutionException("Error analysing", e);
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
    ENVIRONMENT.setPreserveDirectories(preserveDirectories);
  }

  //--------------------------------------------------------------------------------------------------------------------

  /**
   * needed to call protected method {@link #determineRootDir(String)}
   */
  private class CustomPathMatchingResourcePatternResolver extends PathMatchingResourcePatternResolver {

    /**
     * Redirect to protected method {@link #determineRootDir(String)}
     *
     * @param pattern the location to check
     * @return the part of the location that denotes the root directory
     */
    public String getRootDir(String pattern) {
      return super.determineRootDir(pattern);
    }

  }

}
