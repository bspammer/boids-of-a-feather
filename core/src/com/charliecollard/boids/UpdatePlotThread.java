package com.charliecollard.boids;

import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UpdatePlotThread extends Thread {
    public static UpdatePlotThread mThread;
    private PlotFrame plotFrame;
    public static int correlationInterval = 10;
    public static int correlationNumber = BoidSimulator.simulationWidth/(correlationInterval*2);
    private ArrayList<ArrayList<Float>> correlationLists = new ArrayList<>();
    private List<Vector2> boidPositionList;
    private List<Vector2> boidVelocityList;
    private Vector2 avgVelocity;

    public UpdatePlotThread(PlotFrame plotFrame, List<Vector2> boidPositionList, List<Vector2> boidVelocityList, Vector2 avgVelocity) {
        for (int i = 0; i < correlationNumber; i++) {
            correlationLists.add(new ArrayList<Float>());
        }
        this.plotFrame = plotFrame;
        this.boidPositionList = boidPositionList;
        this.boidVelocityList = boidVelocityList;
        this.avgVelocity = avgVelocity;
        mThread = this;
    }

    @Override
    public void run() {
        if (boidPositionList.size() != boidVelocityList.size()) {
            throw new IllegalStateException("Different length of positions and velocities");
        }
        for (int i = 0; i < boidPositionList.size(); i++) {
            for (int j = 0; j < boidPositionList.size(); j++) {
                float distance = BoidSimulator.wrappingScheme.relativeDisplacement(boidPositionList.get(i), boidPositionList.get(j)).len();
                if (distance < correlationInterval * correlationNumber) {
                    Vector2 boidFluctuation = boidVelocityList.get(i).cpy().sub(avgVelocity);
                    Vector2 otherBoidFluctuation = boidVelocityList.get(j).cpy().sub(avgVelocity);
                    float dotProduct = boidFluctuation.dot(otherBoidFluctuation);
                    int index = (int) Math.floor(distance / correlationInterval);
                    ArrayList<Float> list = correlationLists.get(index);
                    list.add(dotProduct);
                    correlationLists.set(index, list);
                }
            }
        }
        ArrayList<Float> sums = new ArrayList<>();
        for (ArrayList<Float> list : correlationLists) {
            float sum = 0;
            for (Float f : list) {
                sum += f;
            }
            if (list.size() > 0) sum /= list.size();
            sums.add(sum);
        }
        float biggest = Collections.max(sums);
        double[] xs = new double[correlationNumber];
        double[] ys = new double[correlationNumber];
        for (int i = 0; i < correlationNumber; i++) {
            xs[i] = i*correlationInterval;
            ys[i] = sums.get(i) / biggest;
        }
        plotFrame.updateData(xs, ys);
    }
}
