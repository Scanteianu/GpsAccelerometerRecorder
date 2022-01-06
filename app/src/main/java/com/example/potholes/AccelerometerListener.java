package com.example.potholes;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AccelerometerListener implements SensorEventListener {
    public double lastX=0;
    public double lastY=0;
    public double lastZ=0;
    public double lastLinear = 0;
    public double[] getAvgStdMax(){
        List<Double> asList;
        synchronized (queue){
            asList = new ArrayList<>(queue.size());
            for(Double d:queue){
                asList.add(d);
            }
            queue.clear();
        }
        double average = computeAvg(asList);
        double std = computeStd(asList, average);
        double max = computeMax(asList);
        double[] avgStdMax = {average,std,max};
        return avgStdMax;

    }
    double computeMax(List<Double> list){
        if(list.size()==0){
            return 0;
        }
        double currentMax = list.get(0);
        for(Double d: list){
            currentMax=Math.max(currentMax,d);
        }
        return currentMax;
    }
    double computeAvg(List<Double> list){
        double sum = 0.0;
        int length = list.size();

        for(double num : list) {
            sum += num;
        }

        double mean = sum/length;
        return mean;
    }
    public static double computeStd(List<Double> list, double mean)
    {
        //stolen from internet, then modified; prob wrong
        double standardDeviation = 0.0;
        for(double num: list) {
            standardDeviation += Math.pow(num - mean, 2);
        }

        return Math.sqrt(standardDeviation/list.size());
    }
    BlockingQueue<Double> queue = new LinkedBlockingQueue<>();
    float getAvgAccel(){
        return 0;
    }
    float getStDevAccel(){
        return 0;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        lastX=sensorEvent.values[0];
        lastY=sensorEvent.values[1];
        lastZ=sensorEvent.values[2];
        lastLinear = Math.sqrt(lastX*lastX+lastY*lastY+lastZ*lastZ);
        queue.add(lastLinear);
    }


    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
