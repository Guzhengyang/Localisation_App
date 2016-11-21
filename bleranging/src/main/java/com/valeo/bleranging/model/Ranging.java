package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.R;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

/**
 * Created by Zhengyang on 29/08/2016
 */
public class Ranging {
    private Instance sample;
    private RandomForest rf;
    private double f = 2.45 * Math.pow(10, 9);
    private double c = 3 * Math.pow(10, 8);
    private double P = -19;
    private double threshold = 0.15;
    private double[] dist = new double[4];

    public Ranging(Context context, double rssi_left, double rssi_middle, double rssi_right, double rssi_back) {
        try {
            this.dist[0] = rssi2dist(rssi_left);
            this.dist[1] = rssi2dist(rssi_middle);
            this.dist[2] = rssi2dist(rssi_right);
            this.dist[3] = rssi2dist(rssi_back);

            rf = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.rf));
            Instances instances = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.sample));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }

    public void set(int index, double rssi) {
        double new_dist = rssi2dist(rssi);
        if (new_dist < this.dist[index])
            this.dist[index] = new_dist;
        else {
            if (new_dist - this.dist[index] > threshold)
                this.dist[index] = this.dist[index] + threshold;
            else
                this.dist[index] = new_dist;
        }
        this.sample.setValue(index, this.dist[index]);
    }

    public int predict2int() {
        try {
            return (int) rf.classifyInstance(sample);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return -1;
    }
}

