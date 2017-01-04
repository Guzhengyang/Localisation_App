package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    public static final String PREDICTION_START = "start";
    public static final String PREDICTION_LOCK = "lock";
    public static final String PREDICTION_TRUNK = "trunk";
    public static final String PREDICTION_LEFT = "left";
    public static final String PREDICTION_RIGHT = "right";
    public static final String PREDICTION_BACK = "back";
    public static final String PREDICTION_FRONT = "front";
    public static final String PREDICTION_OUTDOOR = "outdoor";
    public static final String PREDICTION_INDOOR = "indoor";
    public double[] distribution;
    public double[] distribution_indoor;
    public double[] distribution_near_far;
    public String[] classes;
    public String[] classes_indoor;
    public String[] classes_near_far;
    private List<Integer> predictions = new ArrayList<>();
    private List<Integer> predictions_near_far = new ArrayList<>();
    private List<Integer> predictions_indoor = new ArrayList<>();
    private double[] dist;
    private double[] rssi;
    private double threshold_prob = 0.75;
    private int prediction_old = -1;
    private int prediction_old_indoor = -1;
    private int prediction_old_near_far = -1;
    private Instance sample;
    private RandomForest rf;
    private Instance sample_indoor;
    private RandomForest rf_indoor;
    private Instance sample_near_far;
    private RandomForest rf_near_far;
    private double f = 2.45 * Math.pow(10, 9);
    private double c = 3 * Math.pow(10, 8);
    private double P = -30;
    private double threshold_dist_away = 0.25;
    private double threshold_dist_near = 0.5;
    private double threshold_rssi = 20;
    private int n_vote = 3;
    private int n_vote_indoor = 5;
    private int n_vote_near_far = 5;
    private int OFFSET_A5 = 5;
    private int OFFSET_HYSTERIS = 2;
    private int INDEX_TRUNK = 3;


    public Ranging(Context context, double[] rssi) {
        try {
            classes = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes));
            rf = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf));
            Instances instances = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);

            classes_indoor = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes_indoor));
            rf_indoor = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf_indoor));
            Instances instances_indoor = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample_indoor));
            instances_indoor.setClassIndex(instances_indoor.numAttributes() - 1);
            sample_indoor = instances_indoor.instance(0);

            classes_near_far = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes_near_far));
            rf_near_far = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf_near_far));
            Instances instances_near_far = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample_near_far));
            instances_near_far.setClassIndex(instances_near_far.numAttributes() - 1);
            sample_near_far = instances_near_far.instance(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.dist = new double[rssi.length];
        this.rssi = new double[rssi.length];
        this.distribution = new double[classes.length];
        this.distribution_indoor = new double[classes_indoor.length];
        this.distribution_near_far = new double[classes_near_far.length];

        for (int i = 0; i < rssi.length; i++) {
            this.rssi[i] = rssi[i] - OFFSET_A5;
            dist[i] = rssi2dist(this.rssi[i]);
            sample.setValue(i, dist[i]);
            sample_indoor.setValue(i, rssi[i]);
            sample_near_far.setValue(i, rssi[i]);
        }

        predictions.add(predict2int());
        predictions_indoor.add(predict2int_indoor());
        predictions_near_far.add(predict2int_near_far());

    }


    public double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }


    public void set_rssi(double[] rssi) {
        double dist_new;
        for (int i = 0; i < rssi.length; i++) {
//            if (rssi[i] - this.rssi[i] < threshold_rssi)
            this.rssi[i] = rssi[i] - OFFSET_A5;

            if (prediction_old != -1) {
                if (this.classes[prediction_old].equals(PREDICTION_LOCK)) {
                    this.rssi[i] -= OFFSET_HYSTERIS;
                }
            }

            dist_new = rssi2dist(this.rssi[i]);
            dist[i] = correct_unilateral(i, dist_new);
            sample.setValue(i, dist[i]);

            sample_indoor.setValue(i, rssi[i]);
            sample_near_far.setValue(i, rssi[i]);
        }
        if (predictions.size() == n_vote) {
            predictions.remove(0);
        }
        predictions.add(predict2int());

        if (predictions_indoor.size() == n_vote_indoor) {
            predictions_indoor.remove(0);
        }
        predictions_indoor.add(predict2int_indoor());

        if (predictions_near_far.size() == n_vote_near_far) {
            predictions_near_far.remove(0);
        }
        predictions_near_far.add(predict2int_near_far());
    }


    public double correct_unilateral(int index, double dist_new) {
        double dist_correted;
        if (dist_new < dist[index])
            dist_correted = dist_new;
        else
            dist_correted = Math.min(dist_new - dist[index], threshold_dist_away) + dist[index];
        return dist_correted;
    }


    public double correct_bilateral(int index, double dist_new) {
        double dist_correted;
        if (dist_new > dist[index]) {
            dist_correted = dist[index] + Math.min(dist_new - dist[index], threshold_dist_away);
        } else {
            dist_correted = dist[index] - Math.min(dist[index] - dist_new, threshold_dist_near);
        }
        return dist_correted;
    }


    public int predict2int() {
        int result = -1;
        try {
            result = (int) rf.classifyInstance(sample);
            distribution = rf.distributionForInstance(sample);
        } catch (Exception e) {

        }
        return result;
    }


    public int predict2int_indoor() {
        int result = -1;
        try {
            result = (int) rf_indoor.classifyInstance(sample_indoor);
            distribution_indoor = rf_indoor.distributionForInstance(sample_indoor);
        } catch (Exception e) {

        }
        return result;
    }


    public int predict2int_near_far() {
        int result = -1;
        try {
            result = (int) rf_near_far.classifyInstance(sample_near_far);
            distribution_near_far = rf.distributionForInstance(sample_near_far);
        } catch (Exception e) {

        }
        return result;
    }


    public int vote2int() {
        int result = most(predictions);
        if (result != -1)
            return result;
        return -1;
    }


    public int vote2int_indoor() {
        int result = most(predictions_indoor);
        if (result != -1)
            return result;
        return -1;
    }


    public int vote2int_near_far() {
        int result = most(predictions_near_far);
        if (result != -1)
            return result;
        return -1;
    }


    public int getPrediction() {
        int prediction;
        if (prediction_old == -1) {
            prediction = vote2int();
            prediction_old = prediction;
        } else {
            if (distribution[vote2int()] > threshold_prob) {
                prediction = vote2int();
                prediction_old = prediction;
            } else {
                prediction = prediction_old;
            }
        }
        return prediction;
    }


    public String getPrediction_indoor() {
        int prediction;
        if (prediction_old_indoor == -1) {
            prediction = vote2int_indoor();
            prediction_old_indoor = prediction;
        } else {
            if (distribution_indoor[vote2int_indoor()] > threshold_prob) {
                prediction = vote2int_indoor();
                prediction_old_indoor = prediction;
            } else {
                prediction = prediction_old_indoor;
            }
        }
        if (classes[getPrediction()].equals(PREDICTION_START)) {
            return classes_indoor[prediction];
        } else {
            return PREDICTION_OUTDOOR;
        }
    }


    public String getPrediction_near_far() {
        int prediction;
        if (prediction_old_near_far == -1) {
            prediction = vote2int_near_far();
            prediction_old_near_far = prediction;
        } else {
            if (distribution_near_far[vote2int_near_far()] > threshold_prob) {
                prediction = vote2int_near_far();
                prediction_old_near_far = prediction;
            } else {
                prediction = prediction_old_near_far;
            }
        }
        if (classes[getPrediction()].equals(PREDICTION_START)) {
            return PREDICTION_INDOOR;
        } else {
            return classes_near_far[prediction];
        }
    }


    public synchronized Integer most(final List<Integer> list) {
        if (list.size() == 0) {
            return -1;
        }
        Map<Integer, Integer> map = new HashMap<>();
        for (Integer t : list) {
            Integer val = map.get(t);
            map.put(t, val == null ? 1 : val + 1);
        }
        Map.Entry<Integer, Integer> max = null;
        for (Map.Entry<Integer, Integer> e : map.entrySet()) {
            if (max == null || e.getValue() >= max.getValue()) {
                max = e;
            }
        }
        return max == null ? -1 : max.getKey();
    }

    public String printDist() {
        StringBuilder sb = new StringBuilder();
        if (dist == null) {
            return null;
        } else {
            for (int i = 0; i < dist.length; i++) {
                sb.append(String.format(Locale.FRANCE, "%.2f", dist[i])).append(" ");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

}
