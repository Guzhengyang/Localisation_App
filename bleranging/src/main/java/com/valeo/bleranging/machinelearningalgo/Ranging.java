package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

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
    private static final double THRESHOLD_PROB = 0.7;
    private static final int N_VOTE = 3;
    private static final double THRESHOLD_PROB_EAR = 0.6;
    private static final int N_VOTE_EAR = 3;
    private static final double THRESHOLD_PROB_NEAR_FAR = 0.6;
    private static final int N_VOTE_NEAR_FAR = 3;
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P = -30;
    private static final double THRESHOLD_DIST_AWAY = 0.3;
    private double[] rssi;

    private List<Integer> predictions = new ArrayList<>();
    private double[] distribution;
    private double[] distance;
    private int prediction_old = -1;
    private Instance sample;
    private RandomForest rf;
    private String[] classes;

    private List<Integer> predictions_ear = new ArrayList<>();
    private double[] distribution_ear;
    private double[] distance_ear;
    private int prediction_old_ear = -1;
    private Instance sample_ear;
    private RandomForest rf_ear;
    private String[] classes_ear;

    private List<Integer> predictions_near_far = new ArrayList<>();
    private double[] distribution_near_far;
    private double[] distance_near_far;
    private int prediction_old_near_far = -1;
    private Instance sample_near_far;
    private RandomForest rf_near_far;
    private String[] classes_near_far;

    Ranging(Context context, double[] rssi) {
        try {
            classes = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes));
            rf = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf));
            Instances instances = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);

            classes_ear = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes)); //TODO change file
            rf_ear = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf));
            Instances instances_ear = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample));
            instances_ear.setClassIndex(instances_ear.numAttributes() - 1);
            sample_ear = instances_ear.instance(0);

            classes_near_far = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_classes_near_far));
            rf_near_far = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_rf_near_far));
            Instances instances_near_far = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.eight_flfrlmrtrlrr_sample_near_far));
            instances_near_far.setClassIndex(instances_near_far.numAttributes() - 1);
            sample_near_far = instances_near_far.instance(0);

        } catch (Exception e) {
            e.printStackTrace();
        }
        this.rssi = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.distance_ear = new double[rssi.length];
        this.distance_near_far = new double[rssi.length];
        this.distribution = new double[classes.length];
        this.distribution_ear = new double[classes_ear.length];
        this.distribution_near_far = new double[classes_near_far.length];

        for (int i = 0; i < rssi.length; i++) {
            this.rssi[i] = rssi[i] - SdkPreferencesHelper.getInstance().getOffsetSmartphone();

            distance[i] = rssi2dist(this.rssi[i]);
            sample.setValue(i, distance[i]);

            distance_ear[i] = rssi2dist(rssi[i]);
            sample_ear.setValue(i, distance_ear[i]);

            distance_near_far[i] = rssi2dist(rssi[i]);
            sample_near_far.setValue(i, distance_near_far[i]);
        }
        predict();
    }

    private void predict() {
        int result = 0, result_ear = 0, result_near_far = 0;
        try {
            result = (int) rf.classifyInstance(sample);
            distribution = rf.distributionForInstance(sample);

            result_ear = (int) rf_ear.classifyInstance(sample_ear);
            distribution_ear = rf_ear.distributionForInstance(sample_ear);

            result_near_far = (int) rf_near_far.classifyInstance(sample_near_far);
            distribution_near_far = rf_near_far.distributionForInstance(sample_near_far);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            predictions.add(result);
            predictions_ear.add(result_ear);
            predictions_near_far.add(result_near_far);
        }
    }

    private double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }

    public void set_rssi(double[] rssi) {
        double dist_new;
        double dist_new_ear;
        double dist_new_near_far;
        for (int i = 0; i < rssi.length; i++) {
            this.rssi[i] = rssi[i] - SdkPreferencesHelper.getInstance().getOffsetSmartphone();
            if (prediction_old != -1) {
                if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                    this.rssi[i] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
                }
//                l, m, r, t, fl, fr, rl, rr
                // Add hysteresis to all left sided trx
                if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) & (i == 0 | i == 4 | i == 6)) {
                    this.rssi[i] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                }
                // Add hysteresis to all right sided trx
                if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT) & (i == 2 | i == 6 | i == 7)) {
                    this.rssi[i] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                }
//                if (this.classes[prediction_old].equals(Ranging.BACK) & (i == 6 | i == 7)){
//                    this.rssi[i] += OFFSET_HYSTERIS_UNLOCK;
//                }
//                if (this.classes[prediction_old].equals(Ranging.FRONT) & (i == 4 | i == 5)){
//                    this.rssi[i] += OFFSET_HYSTERIS_UNLOCK;
//                }
            }
            dist_new = rssi2dist(this.rssi[i]);
            distance[i] = correct_unilateral(distance[i], dist_new);
            sample.setValue(i, distance[i]);

            dist_new_ear = rssi2dist(rssi[i]);
            distance_ear[i] = correct_unilateral(distance_ear[i], dist_new_ear);
            sample_ear.setValue(i, distance_ear[i]);

            dist_new_near_far = rssi2dist(rssi[i]);
            distance_near_far[i] = correct_unilateral(distance_near_far[i], dist_new_near_far);
            sample_near_far.setValue(i, distance_near_far[i]);
        }
        if (predictions.size() == N_VOTE) {
            predictions.remove(0);
        }
        if (predictions_ear.size() == N_VOTE_EAR) {
            predictions_ear.remove(0);
        }
        if (predictions_near_far.size() == N_VOTE_NEAR_FAR) {
            predictions_near_far.remove(0);
        }
        predict();
    }

    private double correct_unilateral(double dist_old, double dist_new) {
        double dist_correted;
        if (dist_new < dist_old)
            dist_correted = dist_new;
        else
            dist_correted = Math.min(dist_new - dist_old, THRESHOLD_DIST_AWAY) + dist_old;
        return dist_correted;
    }

    private int internalCalculatePrediction(int prediction_old, List<Integer> predictions,
                                            double[] distribution, double threshold_prob) {
        int prediction;
        if (prediction_old == -1) {
            prediction = most(predictions);
        } else {
            if (distribution[most(predictions)] > threshold_prob) {
                prediction = most(predictions);
            } else {
                prediction = prediction_old;
            }
        }
        return prediction;
    }

    void calculatePrediction() {
        prediction_old = internalCalculatePrediction(prediction_old,
                predictions, distribution, THRESHOLD_PROB);
        prediction_old_ear = internalCalculatePrediction(prediction_old_ear,
                predictions_ear, distribution_ear, THRESHOLD_PROB_EAR);
        prediction_old_near_far = internalCalculatePrediction(prediction_old_near_far,
                predictions_near_far, distribution_near_far, THRESHOLD_PROB_NEAR_FAR);
    }

    String getPrediction() {
        return classes[prediction_old];
    }

    String getPrediction_ear() {
        return classes[prediction_old_ear];
    }

    String getPrediction_near_far() {
        return classes[prediction_old_near_far];
    }

    private synchronized Integer most(final List<Integer> list) {
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

    private String printDistance(double[] distance) {
        StringBuilder sb = new StringBuilder();
        if (distance == null) {
            return null;
        } else {
            for (int i = 0; i < distance.length; i++) {
                sb.append(String.format(Locale.FRANCE, "%.2f", distance[i])).append(" ");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    private String printDistribution(String[] classes, double[] distribution) {
        StringBuilder sb = new StringBuilder();
        if (distribution == null) {
            return null;
        } else {
            for (int i = 0; i < distribution.length; i++) {
                sb.append(classes[i]).append(": ").append(distribution[i]).append(" \n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    String printDist() {
        return printDistance(distance) +
                printDistance(distance_near_far);
    }

    String printDebug() {
        return printDistribution(classes, distribution) +
                printDistribution(classes_near_far, distribution_near_far);
    }
}
