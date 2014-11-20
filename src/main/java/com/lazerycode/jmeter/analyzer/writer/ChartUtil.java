/**
 *
 */
package com.lazerycode.jmeter.analyzer.writer;

import java.awt.Color;
import java.awt.Font;
import java.awt.GradientPaint;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;
import org.jfree.ui.HorizontalAlignment;
import org.jfree.ui.RectangleEdge;
import org.jfree.ui.RectangleInsets;

/**
 *
 */
public final class ChartUtil {

    private static final String DATE_FORMAT = "HH:mm:ss";

    private ChartUtil() {

    }

    //////////////////
    // Create Plot
    //////////////////

    public static XYPlot createResponseTimesPlot(String domainAxisName) {
        XYPlot plot = createPlot(createNumberAxis(domainAxisName), createPercentileAxis("Percentiles (%)"), createNumberAxis("Count"));
        plot.setOrientation(PlotOrientation.HORIZONTAL);
        return plot;
    }

    public static XYPlot createDatePlot(String rangeAxisName) {
        return createPlot(createDateAxis(), createNumberAxis(rangeAxisName));
    }

    public static CombinedDomainXYPlot createCombinedDomainDatePlot() {
        return createCombinedDomainPlot(createDateAxis());
    }

    private static CombinedDomainXYPlot createCombinedDomainPlot(ValueAxis domainAxis) {
        CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot(domainAxis);
        combineddomainxyplot.setGap(10d);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);
        return defaultPlot(combineddomainxyplot);
    }

    private static XYPlot createPlot(ValueAxis domainAxis, ValueAxis... rangesAxis) {
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(domainAxis);
        int i = 0;
        for (ValueAxis rangeAxis : rangesAxis) {
            plot.setRangeAxis(i++, rangeAxis);
        }
        return defaultPlot(plot);
    }

    private static <T extends XYPlot> T defaultPlot(T plot) {
        plot.setOutlineVisible(false);
        plot.setInsets(new RectangleInsets(5, 5, 5, 30));
        plot.setBackgroundAlpha(0.0f);
        return plot;
    }

    //////////////////
    // Create Axis
    //////////////////

    private static ValueAxis createDateAxis() {
        DateAxis timeAxis = new DateAxis("Timestamp (" + DATE_FORMAT + ")");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeAxis.setDateFormatOverride(sdf);
        timeAxis.setLowerMargin(0D);
        timeAxis.setUpperMargin(0D);
        timeAxis.setAutoRange(true);
        timeAxis.setStandardTickUnits(DateAxis.createStandardDateTickUnits());
        return defaultAxis(timeAxis);
    }

    private static ValueAxis createPercentileAxis(String axisName) {
        ValueAxis valueAxis = createNumberAxis(axisName);
        valueAxis.setRange(0D, 100.1D);
        return valueAxis;
    }

    private static NumberAxis createNumberAxis(String axisName) {
        NumberAxis numberAxis = new NumberAxis(axisName);
        numberAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        return defaultAxis(numberAxis);
    }

    private static <T extends ValueAxis> T defaultAxis(T axis) {
        axis.setLabelFont(new Font("SansSerif", Font.PLAIN | Font.ITALIC, 12));
        return axis;
    }

    //////////////////
    // Create renderer
    //////////////////

    public static XYLineAndShapeRenderer createLineAndShapeRenderer() {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer(true, false);
        return renderer;
    }

    public static XYLineAndShapeRenderer createSecondaryLineAndShapeRenderer() {
        XYLineAndShapeRenderer renderer = createLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.GRAY);
        return renderer;
    }

    public static XYBarRenderer createBarRenderer() {
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        return renderer;
    }

    public static XYPlot addDatasetRender(XYPlot plot, XYDataset dataset, XYItemRenderer renderer) {
        int index = plot.getDatasetCount();
        // Bug jfreechart. When we create a XYPlot without dataset, one null dataset is added to list.
        if (index == 1 && plot.getDataset(0) == null) {
            index--;
        }
        plot.setDataset(index, dataset);
        plot.setRenderer(index, renderer);
        return plot;
    }

    //////////////////
    // Create chart
    //////////////////

    public static JFreeChart createJFreeChart(String title, XYPlot result, int imageHeight) {
        JFreeChart chart = new JFreeChart(title, result);
        chart.getLegend().setPosition(RectangleEdge.TOP);
        chart.getLegend().setHorizontalAlignment(HorizontalAlignment.LEFT);
        chart.getLegend().setFrame(BlockBorder.NONE);
        chart.setBackgroundPaint(new GradientPaint(0, 0, Color.white, 0, imageHeight, new Color(229, 236, 246)));
        chart.getTitle().setFont(new Font("SansSerif", Font.PLAIN | Font.BOLD, 16));
        chart.getTitle().setPaint(Color.GRAY);
        return chart;
    }
}
