package com.charliecollard.boids;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.ValueMarker;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.Value;
import org.jfree.data.xy.DefaultXYDataset;
import org.jfree.data.xy.XYDataset;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PlotFrame extends ApplicationFrame {
    private DefaultXYDataset dataset;
    private ChartPanel chartPanel;
    private List<Integer> buckets = new ArrayList<>();

    public PlotFrame(String title) {
        super(title);
        for (int i=0; i<100; i++) {
            buckets.add(i*10);
        }

        dataset = new DefaultXYDataset();
        JFreeChart chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);
        UIUtils.positionFrameOnScreen(this, 0,0);
        this.pack();
    }

    private static JFreeChart createChart(XYDataset dataset) {
        JFreeChart chart = ChartFactory.createScatterPlot(
                "Correlations against distance",
                "Distance",
                "Correlation",
                dataset
        );

        chart.setBackgroundPaint(Color.WHITE);
        XYPlot plot = chart.getXYPlot();
        plot.setForegroundAlpha(1f);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.GRAY);
        plot.getDomainAxis().setRange(0, BoidSimulator.simulationWidth/2);
        plot.getRangeAxis().setAutoRange(true);
        ValueMarker marker = new ValueMarker(0);
        marker.setPaint(Color.black);
        plot.addDomainMarker(marker);
        plot.addRangeMarker(marker);

        return chart;
    }

    public void updateData(double[] xs, double[] ys) {
        chartPanel.setVisible(false);
        dataset.removeSeries(0);
        dataset.addSeries(0, new double[][] {xs, ys});
        chartPanel.setVisible(true);
    }

    @Override
    public String toString() {
        return "Correlation plot";
    }
}
