/**
 *
 */
package com.lazerycode.jmeter.analyzer.writer;

import java.awt.Color;
import java.text.SimpleDateFormat;
import java.util.TimeZone;

import org.jfree.chart.axis.DateAxis;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CombinedDomainXYPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBarRenderer;
import org.jfree.chart.renderer.xy.XYItemRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.xy.XYDataset;

/**
 *
 */
public final class ChartUtil {

    private static final String DATE_FORMAT = "HH:mm:ss";

    private ChartUtil() {

    }

    public static XYPlot createPlot(String rangeAxisName) {
        XYPlot plot = new XYPlot();
        plot.setDomainAxis(createDateAxis());
        NumberAxis rangeAxis = new NumberAxis(rangeAxisName);
        plot.setRangeAxis(rangeAxis);
        return plot;
    }

    public static CombinedDomainXYPlot createCombinedDomainPlot() {
        CombinedDomainXYPlot combineddomainxyplot = new CombinedDomainXYPlot(createDateAxis());
        combineddomainxyplot.setGap(10d);
        combineddomainxyplot.setOrientation(PlotOrientation.VERTICAL);
        return combineddomainxyplot;
    }

    private static ValueAxis createDateAxis() {
        DateAxis timeAxis = new DateAxis("Timestamp (" + DATE_FORMAT + ")");
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        timeAxis.setDateFormatOverride(sdf);
        timeAxis.setLowerMargin(0.02D);
        timeAxis.setUpperMargin(0.02D);
        timeAxis.setAutoRange(true);
        return timeAxis;
    }

    public static XYPlot addDatasetRender(XYPlot plot, XYDataset dataset, XYItemRenderer renderer) {
        int index = plot.getDatasetCount();
        plot.setDataset(index, dataset);
        plot.setRenderer(index, renderer);
        return plot;
    }

    public static XYItemRenderer createLineAndShapeRenderer() {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setBaseShapesVisible(false);
        return renderer;
    }

    public static XYItemRenderer createSecondaryLineAndShapeRenderer() {
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        renderer.setSeriesPaint(0, Color.GRAY);
        renderer.setBaseShapesVisible(false);
        return renderer;
    }

    public static XYItemRenderer createBarRenderer() {
        XYBarRenderer renderer = new XYBarRenderer();
        renderer.setShadowVisible(false);
        return renderer;
    }

}
