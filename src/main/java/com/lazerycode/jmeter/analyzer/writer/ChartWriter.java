package com.lazerycode.jmeter.analyzer.writer;

import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;
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
import java.util.List;
import java.util.Map;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.*;

/**
 * Writes sizes and durations charts as PNG images
 */
public class ChartWriter extends AbstractWriter {

  private static final int IMAGE_WIDTH = 800;
  private static final int IMAGE_HEIGHT = 600;

  /**
   * Needed to check if an Instance of ChartWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   *
   * @param obj the object to check
   *
   * @return true of obj is an instance of ChartWriter.
   */
  @Override
  public boolean equals(Object obj) {
    return obj instanceof ChartWriter;
  }

  @Override
  public void write(Map<String, AggregatedResponses> testResults) throws IOException, TemplateException {

    // Process every AggregatedResponse
    for (Map.Entry<String, AggregatedResponses> entry : testResults.entrySet()) {

      String name = entry.getKey();
      AggregatedResponses aggregatedResponses = entry.getValue();

      writeCharts(aggregatedResponses, name);
    }
  }

  //====================================================================================================================

  /**
   * Generate Charts
   *
   * @param name identifier used as part of the name
   * @param aggregatedResponses results to generate CSV from
   *
   * @throws IOException
   */
  private void writeCharts(AggregatedResponses aggregatedResponses, String name) throws IOException, TemplateException {

    String fileName;
    File requestChartFile;
    Samples aggregatedResult;
    String chartName;

    //durations chart
    fileName = urlEncode(name) + durationsPngFileSuffix;
    requestChartFile = initializeFile(getTargetDirectory(), fileName, getResultDataFileRelativePath());
    aggregatedResult = aggregatedResponses.getDuration();
    chartName = "Requests Duration ("+name+")";
    renderChart(chartName, "Duration", "Time / ms", "Duration / ms", aggregatedResult, requestChartFile);

    //sizes chart
    fileName = urlEncode(name) + sizesPngFileSuffix;
    requestChartFile = initializeFile(getTargetDirectory(), fileName, getResultDataFileRelativePath());
    aggregatedResult = aggregatedResponses.getSize();
    chartName = "Requests Size ("+name+")";
    renderChart(chartName, "Size", "Time / ms", "Size / bytes", aggregatedResult, requestChartFile);
  }

  /**
   * Renders a single result as a chart
   *
   * @param chartName
   * @param singleValueName
   * @param domainAxisName
   * @param rangeAxisName
   * @param source
   * @param target
   *
   */
  private static void renderChart(String chartName, String singleValueName, String domainAxisName, String rangeAxisName,
                                 Samples source, File target) throws IOException {

    XYSeries singleValue = new XYSeries(singleValueName);
    XYSeries average = new XYSeries("Average");

    long minimumTimestamp = source.getMinTimestamp();

    List<Long> samples = source.getSamples();
    List<Long> timestamps = source.getTimestamps();

    long total = 0;
    for( int x=0; x<samples.size(); x++ ) {

      long current = samples.get(x);
      long timestamp = timestamps.get(x);
      singleValue.add(timestamp - minimumTimestamp, samples.get(x));

      total += current;
      average.add(timestamp-minimumTimestamp, (int) (total / (x+1)));
    }

    XYSeriesCollection singleValues = new XYSeriesCollection();
    singleValues.addSeries(singleValue);
    XYSeriesCollection averages = new XYSeriesCollection();
    averages.addSeries(average);

    XYPlot plot = new XYPlot();
    NumberAxis domainAxis = new NumberAxis(domainAxisName);
    domainAxis.setAutoRangeIncludesZero(false);
    plot.setDomainAxis(domainAxis);
    NumberAxis rangeAxis = new NumberAxis(rangeAxisName);
    plot.setRangeAxis(rangeAxis);

    XYBarRenderer renderer1 = new XYBarRenderer();
    renderer1.setShadowVisible(false);
    plot.setDataset(1, singleValues);
    plot.setRenderer(1, renderer1);

    XYLineAndShapeRenderer renderer2 = new XYLineAndShapeRenderer();
    renderer2.setBaseShapesVisible(false);
    plot.setDataset(0, averages);
    plot.setRenderer(0, renderer2);

    JFreeChart chart = new JFreeChart(chartName, plot);

    ChartUtilities.saveChartAsPNG(target, chart, IMAGE_WIDTH, IMAGE_HEIGHT, null);
  }

}
