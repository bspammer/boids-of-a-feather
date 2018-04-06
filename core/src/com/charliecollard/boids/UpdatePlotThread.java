package com.charliecollard.boids;

import java.util.List;

public class UpdatePlotThread extends Thread {
    private List<Float> distances;
    private PlotFrame plotFrame;
    public UpdatePlotThread(PlotFrame plotFrame, List<Float> distances) {
        this.distances = distances;
        this.plotFrame = plotFrame;
    }

    @Override
    public void run() {
        plotFrame.updateData(distances);
    }
}
