package com.lazerycode.jmeter.analyzer.writer;

import static com.lazerycode.jmeter.analyzer.config.Environment.*;
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

import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;

import com.google.common.annotations.VisibleForTesting;
import com.lazerycode.jmeter.analyzer.ConfigurationCharts;
import com.lazerycode.jmeter.analyzer.parser.AggregatedResponses;
import com.lazerycode.jmeter.analyzer.statistics.Quantile;
import com.lazerycode.jmeter.analyzer.statistics.Samples;

import freemarker.template.TemplateException;

/**
 * Writes sizes and durations charts as PNG images
 */
public class ChartWriter extends WriterBase {

  // QUANTILE use for percentiles chart
  private static final int Q = 1000;
  // Must be > 0
  private final static long SECONDS_ROUND = 10L;

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

    // Create Plot
    XYPlot durationPlot = createDurationPlot(aggregatedResponses);
    XYPlot sizePlot = createSizePlot(aggregatedResponses);
    XYPlot activeThreadsPlot = createActiveThreadsPlot(aggregatedResponses);
    XYPlot throughputPlot = createThroughputPlot(aggregatedResponses);
    XYPlot responseTimesPlot = createResponseTimesPlot(aggregatedResponses);

    // Create Combined Plot
    CombinedDomainXYPlot combinedThroughputActiveThreadsPlot = ChartUtil.createCombinedDomainDatePlot();
    combinedThroughputActiveThreadsPlot.add( throughputPlot, 2 );
    combinedThroughputActiveThreadsPlot.add( activeThreadsPlot, 1 );

    //throughput chart
    requestChartFile = getFile(chartFileName(name, THROUGHPUT));
    chartName = "Throughput (" + name + ")";
    renderChart(chartName, combinedThroughputActiveThreadsPlot, requestChartFile);

    //response times chart
    requestChartFile = getFile(chartFileName(name, RESPONSE_TIMES));
    chartName = "Response times (" + name + ")";
    renderChart(chartName, responseTimesPlot, requestChartFile);

    //durations chart
    requestChartFile = getFile(chartFileName(name, DURATIONS));
    chartName = "Requests Duration (" + name + ")";
    renderChart(chartName, durationPlot, requestChartFile);

    //sizes chart
    requestChartFile = getFile(chartFileName(name, SIZES));
    chartName = "Requests Size (" + name + ")";
    renderChart(chartName, sizePlot, requestChartFile);
  }

  private XYPlot createDurationPlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createDatePlot("Duration (ms)");
      Samples durations = aggregatedResponses.getDuration();
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Duration", durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createBarRenderer());
  }

  private XYPlot createSizePlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createDatePlot("Size (bytes)");
      Samples durations = aggregatedResponses.getSize();
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Size", durations.getTimestamps(),
              durations.getSamples(), durations.getMinTimestamp())), ChartUtil.createBarRenderer());
  }

  private XYPlot createActiveThreadsPlot(AggregatedResponses aggregatedResponses) {
      XYPlot plot = ChartUtil.createDatePlot("Thread Count");
      Samples activeThreads = aggregatedResponses.getActiveThreads();
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Threads", activeThreads.getTimestamps(),
              activeThreads.getSamples(), activeThreads.getMinTimestamp())), ChartUtil.createSecondaryLineAndShapeRenderer());
  }

  private XYPlot createThroughputPlot(AggregatedResponses aggregatedResponses) {
      SortedMap<Long, Long> throughput = new TreeMap<Long, Long>();
      for (long timestamp : aggregatedResponses.getActiveThreads().getTimestamps()) {
          long timestampRound = convert(timestamp);
          Long throughputResult = throughput.get(timestampRound);
          // Add SECONDS_ROUND to get request by second.
          if (null == throughputResult) {
              throughput.put(timestampRound, SECONDS_ROUND);
          } else {
              throughputResult += SECONDS_ROUND;
              throughput.put(timestampRound, throughputResult);
          }
      }
      List<Long> timestamps = new ArrayList<Long>(throughput.keySet());
      List<Long> samples = new ArrayList<Long>(throughput.values());

      long minTimestamp = convert(aggregatedResponses.getActiveThreads().getMinTimestamp());

      XYPlot plot = ChartUtil.createDatePlot("Requests (req/s)");
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createAverageValuesSeries(AVERAGE, timestamps, samples, minTimestamp)), ChartUtil.createLineAndShapeRenderer());
      return ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
              createValuesSeries("Throughput", timestamps, samples, minTimestamp)), ChartUtil.createLineAndShapeRenderer());
  }

  private XYPlot createResponseTimesPlot(AggregatedResponses aggregatedResponses) {
      Samples durations = aggregatedResponses.getDuration();

      // Distribution
      double[] values = new double[durations.getSamples().size()];
      for (int i = 0; i < durations.getSamples().size(); i++) {
          values[i] = durations.getSamples().get(i);
      }

      HistogramDataset histogramdataset = new HistogramDataset();
      histogramdataset.addSeries("Request Count", values, 40);

      // Percentiles
      List<Double> y = new ArrayList<Double>(Q + 1);
      List<Long> x = new ArrayList<Long>(Q + 1);
      Quantile quantile = durations.getQuantiles(Q);
      // Add zero value
      y.add(0D);
      x.add(0L);
      for (int i = 1; i < Q; i++) {
          y.add(((double) i) / 10);
          x.add(quantile.getQuantile(i));
      }
      // Add max value
      y.add(100D);
      x.add(durations.getMax());

      XYPlot plot = ChartUtil.createResponseTimesPlot("Response Times (ms)");
      ChartUtil.addDatasetRender(plot, new XYSeriesCollection(
                  createValuesSeries("Percentiles", x, y, 0)), ChartUtil.createLineAndShapeRenderer());
      ChartUtil.addDatasetRender(plot, histogramdataset, ChartUtil.createBarRenderer());
      plot.mapDatasetToRangeAxis(1, 1);
      return plot;
  }


  private XYSeries createValuesSeries(String seriesName, List<? extends Number> x, List<? extends Number> y, long minimumTimestamp) {
      XYSeries series = new XYSeries(seriesName);
      for (int i = 0; i < y.size(); i++) {
          series.add((x.get(i).doubleValue() - minimumTimestamp), y.get(i));
      }
      return series;
  }

  private XYSeries createAverageValuesSeries(String seriesName, List<Long> x, List<Long> y, long minimumTimestamp) {
      XYSeries series = new XYSeries(seriesName);
      long total = 0;
      for (int i = 0; i < y.size(); i++) {
        long current = y.get(i);
        long timestamp = x.get(i);

        total += current;
        series.add((timestamp - minimumTimestamp), (total / (i + 1.0)));
      }
      return series;
  }

  /**
   * Renders a single result as a chart
   *
   * @param singleValueName
   * @param domainAxisName
   * @param rangeAxisName
   * @param principalDataset
   * @param target
   */
  private void renderChart(String name, XYPlot plot, File target) throws IOException {
    OutputStream out = getOut(target);
    try {
      ConfigurationCharts configurationCharts = ENVIRONMENT.getConfigurationCharts();
      writeChartAsPNG(out, ChartUtil.createJFreeChart(name, plot, configurationCharts.getHeight()),
              configurationCharts.getWidth(), configurationCharts.getHeight(), null);
    } finally {
      out.close();
    }
  }

  private String chartFileName(String name, String type) throws UnsupportedEncodingException {
      return new StringBuilder(urlEncode(name)).append(type).append(super.getFileName()).append(PNG_EXT).toString();
  }

  private long convert(long timestamp) {
      long ratio = 1000 / SECONDS_ROUND;
      return (timestamp / ratio ) * ratio;
  }

}
