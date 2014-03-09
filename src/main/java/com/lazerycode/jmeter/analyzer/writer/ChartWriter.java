package com.lazerycode.jmeter.analyzer.writer;

import static com.lazerycode.jmeter.analyzer.util.FileUtil.urlEncode;
import static org.jfree.chart.ChartUtilities.writeChartAsPNG;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.concurrent.TimeUnit;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.annotations.VisibleForTesting;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Samples;

import freemarker.template.TemplateException;

/**
 * Writes sizes and durations charts as PNG images
 */
public class ChartWriter extends WriterBase {

  private int imageWidth = 800;
  private int imageHeight = 600;

  public ChartWriter(int imageWidth, int imageHeight) {
      this.imageWidth = imageWidth;
      this.imageHeight = imageHeight;
  }

  /**
   * Needed to check if an Instance of ChartWriter is already in the {@link com.lazerycode.jmeter.analyzer.AnalyzeMojo#writers}
   * Since this is more or less a simple PoJo, it is not necessary to make more than a simple instanceof check.
   *
   * @param obj the object to check
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

  //--------------------------------------------------------------------------------------------------------------------

  @VisibleForTesting
  protected OutputStream getOut(File file) throws FileNotFoundException {
    return new BufferedOutputStream(new FileOutputStream(file));
  }

  //====================================================================================================================

  /**
   * Generate Charts
   *
   * @param name                identifier used as part of the name
   * @param aggregatedResponses results to generate CSV from
   * @throws IOException
   */
  private void writeCharts(AggregatedResponses aggregatedResponses, String name) throws IOException, TemplateException {

    File requestChartFile;
    String chartName;

    XYPlot durationPlot = createDurationPlot(aggregatedResponses);
    XYPlot sizePlot = createSizePlot(aggregatedResponses);
    XYPlot activeThreadsPlot = createActiveThreadsPlot(aggregatedResponses);
    XYPlot throughputPlot = createThroughputPlot(aggregatedResponses);

    //durations chart
    requestChartFile = getFile(chartFileName(name, THROUGHPUT));
    chartName = "Throughput (" + name + ")";
    renderChart(chartName, throughputPlot, activeThreadsPlot, requestChartFile);

    //durations chart
    requestChartFile = getFile(chartFileName(name, DURATIONS));
    chartName = "Requests Duration (" + name + ")";
    renderChart(chartName, durationPlot, null, requestChartFile);

    //sizes chart
    requestChartFile = getFile(chartFileName(name, SIZES));
    chartName = "Requests Size (" + name + ")";
    renderChart(chartName, sizePlot, null, requestChartFile);
  }

  private XYPlot createDurationPlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createPlot("Duration / ms");
      Samples durations = aggregatedResponses.getDuration();
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Duration", durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createBarRenderer());
  }

  private XYPlot createSizePlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createPlot("Size / bytes");
      Samples durations = aggregatedResponses.getSize();
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Size", durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createBarRenderer());
  }

  private XYPlot createActiveThreadsPlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createPlot("Thread Count");
      Samples activeThreads = aggregatedResponses.getActiveThreads();
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Threads", activeThreads.getTimestamps(),
              activeThreads.getSamples(), activeThreads.getMinTimestamp())), ChartUtil.createSecondaryLineAndShapeRenderer());
  }

  private XYPlot createThroughputPlot(AggregatedResponses aggregatedResponses) {
      SortedMap<Long, Long> throughput = new TreeMap<Long, Long>();
      for (long timestamp : aggregatedResponses.getActiveThreads().getTimestamps()) {
          long seconds = TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(timestamp));
          Long throughputResult = throughput.get(seconds);
          if (null == throughputResult) {
              throughput.put(seconds, 1L);
          } else {
              throughput.put(seconds, ++throughputResult);
          }
      }
      List<Long> timestamps = new ArrayList<Long>(throughput.keySet());
      List<Long> samples = new ArrayList<Long>(throughput.values());

      long minTimestamp = TimeUnit.SECONDS.toMillis(TimeUnit.MILLISECONDS.toSeconds(aggregatedResponses.getActiveThreads().getMinTimestamp()));

      XYPlot plot = ChartUtil.createPlot("Requests (req/s)");
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, timestamps, samples, minTimestamp)), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Throughput", timestamps, samples, minTimestamp)), ChartUtil.createLineAndShapeRenderer());
  }


  private XYSeries createValuesSeries(String seriesName, List<Long> timestamps, List<Long> samples, long minimumTimestamp) {
      XYSeries series = new XYSeries(seriesName);
      for (int x = 0; x < samples.size(); x++) {
          series.add(timestamps.get(x) - minimumTimestamp, samples.get(x));
      }
      return series;
  }

  private XYSeries createAverageValuesSeries(String seriesName, List<Long> timestamps, List<Long> samples, long minimumTimestamp) {
      XYSeries series = new XYSeries(seriesName);
      long total = 0;
      for (int x = 0; x < samples.size(); x++) {
        long current = samples.get(x);
        long timestamp = timestamps.get(x);

        total += current;
        series.add(timestamp - minimumTimestamp, (total / (x + 1.0)));
      }
      return series;
  }

  /**
   * Renders a single result as a chart
   *
   * @param chartName
   * @param singleValueName
   * @param domainAxisName
   * @param rangeAxisName
   * @param principalDataset
   * @param target
   */
  private void renderChart(String chartName, XYPlot principalPlot, XYPlot secondaryPlot, File target) throws IOException {
    XYPlot result;
    XYBarRenderer renderer = new XYBarRenderer();
    renderer.setShadowVisible(false);

    if (null == secondaryPlot) {
      result = principalPlot;
    } else {
      CombinedDomainXYPlot combineddomainxyplot = ChartUtil.createCombinedDomainPlot();
      combineddomainxyplot.add( principalPlot, 2 );
      combineddomainxyplot.add( secondaryPlot, 1 );
      result = combineddomainxyplot;
    }

    JFreeChart chart = new JFreeChart(chartName, result);
    OutputStream out = getOut(target);

    try {
      writeChartAsPNG(out, chart, imageWidth, imageHeight, null);
    } finally {
      out.close();
    }
  }

  private String chartFileName(String name, String type) throws UnsupportedEncodingException {
      return new StringBuilder(urlEncode(name)).append(type).append(super.getFileName()).append(PNG_EXT).toString();
  }

}
