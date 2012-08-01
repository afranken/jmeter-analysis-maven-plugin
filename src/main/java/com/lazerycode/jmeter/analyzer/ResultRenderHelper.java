package com.lazerycode.jmeter.analyzer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import org.jfree.chart.ChartUtilities;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.config.Environment.ENVIRONMENT;

/**
 * Utility for rendering results
 *
 * @author Arne Franken, Peter Kaul
 */
public class ResultRenderHelper {

  /**
   * Quantiles resolution is 1000 so that we can get 99.9 percent
   */
  private static final int Q_QUANTILES = 1000;
  /**
   * Use this value to get the Quantile for 99.9 percent
   */
  private static final int K_99_PONT_9_PERCENT = 999;
  /**
   * Use this value to get the Quantile for 99 percent
   */
  private static final int K_99_PERCENT = 990;

  private static final int PERCENT_100 = 100;

  private static final int IMAGE_WIDTH = 800;
  private static final int IMAGE_HEIGHT = 600;

  private Configuration configuration;
  private File templateDirectory;
  private boolean generateCharts;
  private boolean generateCSVs;

  /**
   * Constructor.
   * Fields are read from {@link com.lazerycode.jmeter.analyzer.config.Environment}
   */
  public ResultRenderHelper() {

    this.templateDirectory = ENVIRONMENT.getTemplateDirectory();
    this.generateCharts = ENVIRONMENT.isGenerateCharts();
    this.generateCSVs = ENVIRONMENT.isGenerateCSVs();
    this.configuration = ENVIRONMENT.getConfiguration();
  }

  /**
   * Render given {@link AggregatedResponses testResults} as text
   *
   * @param testResults results to render
   * @param out output to write to
   * @throws IOException
   * @throws TemplateException
   */
  public void renderText(Map<String, AggregatedResponses> testResults, Writer out) throws IOException, TemplateException {

    Map<String,Object> rootMap = getRootMap(testResults);

    Template root = getTemplate("text/main.ftl");

    // Merge data-model with template
    root.process(rootMap, out);
  }

  /**
   * Render given {@link AggregatedResponses testResults} as HTML
   *
   * @param testResults results to render
   * @param out output to write to
   * @throws IOException
   * @throws TemplateException
   */
  public void renderHTML(Map<String, AggregatedResponses> testResults, PrintWriter out, String summaryFileName) throws IOException, TemplateException {

    Map<String,Object> rootMap = getRootMap(testResults);
    rootMap.put("SUMMARY_FILE_NAME", summaryFileName);

    Template root = getTemplate("html/main.ftl");

    // Merge data-model with template
    root.process(rootMap, out);
  }

  /**
   * Writes {@link Samples} per uri to a CSV file
   *
   * @param testResults Mapping uri -&gt; samples
   *
   * @throws IOException If writing fails
   */
  public void writeCSV(Map<String, Samples> testResults, PrintWriter out) throws IOException, TemplateException {

    Map<String,Object> rootMap = getRootMap(testResults);

    Template root = getTemplate("csv/main.ftl");

    // Merge data-model with template
    root.process(rootMap, out);
  }


  /**
   * Renders a single result as a chart
   */
  public static void renderChart(String name, Samples source, File target) throws IOException {

    XYSeries duration = new XYSeries("Duration");
    XYSeries average = new XYSeries("Average");

    long minimumTimestamp = source.getMinTimestamp();

    List<Long> samples = source.getSamples();
    List<Long> timestamps = source.getTimestamps();
    long total = 0;
    for( int x=0; x<samples.size(); x++ ) {

      long current = samples.get(x);
      long timestamp = timestamps.get(x);
      duration.add(timestamp-minimumTimestamp, samples.get(x));

      total += current;
      average.add(timestamp-minimumTimestamp, (int) (total / (x+1)));
    }

    XYSeriesCollection durations = new XYSeriesCollection();
    durations.addSeries(duration);
    XYSeriesCollection averages = new XYSeriesCollection();
    averages.addSeries(average);


    XYPlot plot = new XYPlot();
    NumberAxis domainAxis = new NumberAxis("Time / ms");
    domainAxis.setAutoRangeIncludesZero(false);
    plot.setDomainAxis(domainAxis);

    plot.setRangeAxis(new NumberAxis("Duration / ms"));

    XYBarRenderer renderer1 = new XYBarRenderer();
    renderer1.setShadowVisible(false);
    plot.setDataset(1, durations);
    plot.setRenderer(1, renderer1);


    XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
    renderer2.setBaseShapesVisible(false);
    plot.setDataset(0, averages);
    plot.setRenderer(0, renderer2);

    JFreeChart chart = new JFreeChart("Requests Duration ("+name+")", plot);

    ChartUtilities.saveChartAsPNG(target, chart, IMAGE_WIDTH, IMAGE_HEIGHT, null);
  }


  //====================================================================================================================

  /**
   * Create rootMap with all necessary parameters/objects for Freemarker rendering
   *
   * @param self main object that will be accessed from Freemarker template
   *
   * @return populated map
   */
  private Map<String,Object> getRootMap(Object self) {

    Map<String,Object> rootMap = new HashMap<String,Object>();
    rootMap.put("self", self);
    rootMap.put("Q_QUANTILES", Q_QUANTILES);
    rootMap.put("K_99_PERCENT", K_99_PERCENT);
    rootMap.put("K_99_PONT_9_PERCENT", K_99_PONT_9_PERCENT);
    rootMap.put("PERCENT_100", PERCENT_100);
    rootMap.put("DETAILS", generateCSVs);
    rootMap.put("CHARTS", generateCharts);

    return rootMap;

  }

  /**
   * Try to load template from custom location.
   * Load bundled template from classpath in case no custom template is available or an error occurs
   *
   * @param templateName name of the template
   *
   * @return the template
   *
   * @throws IOException
   */
  private Template getTemplate(String templateName) throws IOException {

    Template template = null;

    if(templateDirectory != null && templateDirectory.isDirectory()) {
      if(new File(templateDirectory,templateName).exists()) {
        //load template from custom location
        configuration.setDirectoryForTemplateLoading(templateDirectory);
        template = configuration.getTemplate(templateName);
      }
    }

    if(template == null) {
      //custom location not configured. Load from classpath.
      configuration.setClassForTemplateLoading(ResultRenderHelper.class, "templates");
      template = configuration.getTemplate(templateName);
    }

    return template;
  }

}
