package com.charliecollard.boids;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.ui.ApplicationFrame;
import org.jfree.chart.ui.UIUtils;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.util.*;
import java.util.List;

public class PlotFrame extends ApplicationFrame {
    DefaultCategoryDataset dataset;
    JFreeChart chart;
    ChartPanel chartPanel;
    List<Integer> buckets = new ArrayList<>();

    public PlotFrame(String title) {
        super(title);
        for (int i=0; i<100; i++) {
            buckets.add(i*10);
        }

        dataset = createDataset();
        chart = createChart(dataset);
        chartPanel = new ChartPanel(chart);
        chartPanel.setPreferredSize(new Dimension(500, 270));
        setContentPane(chartPanel);
        UIUtils.positionFrameOnScreen(this, 0.08, 0.3);
        this.pack();
    }

    private static DefaultCategoryDataset createDataset() {
        return new DefaultCategoryDataset();
    }

    private static JFreeChart createChart(CategoryDataset dataset) {
        JFreeChart chart = ChartFactory.createAreaChart(
                "Separation Distribution",
                "Distance",
                "Density",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
        chart.removeLegend();
        chart.setBackgroundPaint(Color.WHITE);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setForegroundAlpha(1f);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setDomainGridlinePaint(Color.WHITE);
        plot.setRangeGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.WHITE);
        ValueAxis rangeAxis = plot.getRangeAxis();
        rangeAxis.setRange(0, 1000);
        rangeAxis.setMinorTickMarksVisible(false);

        return chart;
    }

    public void updateData(List<Float> distances) {
        chartPanel.setVisible(false);
        dataset.clear();

        for (int i = 0; i < buckets.size(); i++) {
            int bucketMax = buckets.get(i);
            int total = 0;

            List<Integer> toRemove = new ArrayList<>();
            for (int j = distances.size() - 1; j >= 0; j--) {
                float distance = distances.get(j);
                if (distance < bucketMax) {
                    total += 1;
                    toRemove.add(j);
                }
            }
            for (int remove : toRemove) {
                distances.remove(remove);
            }
            dataset.addValue((Number) total, 0, i);
        }
        chartPanel.setVisible(true);
    }

    @Override
    public String toString() {
        return "Separation Distribution Plot";
    }
}
