package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.R;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import weka.classifiers.functions.Logistic;
import weka.classifiers.trees.RandomForest;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;
import weka.core.converters.ConverterUtils;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {

    public String[] classes;
    public List<Integer> predictions = new ArrayList<>();
    public double[] proba_sum;
    public double[] distribution = new double[5];
    public double[] dist = new double[8];
    public double threshold_prob = 0.8;
    public int prediction_old = -1;
    private Instance sample;
    private RandomForest rf;
    private Logistic logistic;
    private double f = 2.45 * Math.pow(10, 9);
    private double c = 3 * Math.pow(10, 8);
    private double P = -30;
    private double threshold_dist = 0.25;
    private int n_vote = 3;
    private String model = "rf";
    private List<Double> probas_left = new ArrayList<>();
    private List<Double> probas_right = new ArrayList<>();
    private List<Double> probas_back = new ArrayList<>();
    private List<Double> probas_lock = new ArrayList<>();
    private List<Double> probas_start = new ArrayList<>();


    public Ranging(Context context, double[] rssi) {
        try {
            logistic = (Logistic) SerializationHelper.read(context.getResources().openRawResource(R.raw.logistic));
            classes = (String[]) SerializationHelper.read(context.getResources().openRawResource(R.raw.classes));
            rf = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(R.raw.rf));
            Instances instances = ConverterUtils.DataSource.read(context.getResources().openRawResource(R.raw.sample));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);
        } catch (Exception e) {
            e.printStackTrace();
        }

        for (int i = 0; i < rssi.length; i++) {
            dist[i] = rssi2dist(rssi[i]);
            sample.setValue(i, dist[i]);
        }

        proba_sum = new double[classes.length];
        predictions.add(predict2int());
        add_probs();

    }

    public double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }

    public void set_rssi(double[] rssi) {
        double dist_new;
        for (int i = 0; i < rssi.length; i++) {
            if (prediction_old != -1) {
                if (this.classes[prediction_old].equals("lock")) {
                    rssi[i] -= 2;
                }
            }
            dist_new = rssi2dist(rssi[i] - 2);
            dist[i] = correct_unilateral(i, dist_new);
            sample.setValue(i, dist[i]);
        }
        if (predictions.size() == n_vote) {
            predictions.remove(0);
            probas_left.remove(0);
            probas_right.remove(0);
            probas_back.remove(0);
            probas_start.remove(0);
            probas_lock.remove(0);
        }
        predictions.add(predict2int());
        add_probs();
    }

    public double correct_unilateral(int index, double dist_new) {
        double dist_correted;
        if (dist_new < dist[index])
            dist_correted = dist_new;
        else
            dist_correted = Math.min(dist_new - dist[index], threshold_dist) + dist[index];
        return dist_correted;
    }

    public double correct_bilateral(int index, double dist_new) {
        double dist_corrected;
        if (dist_new - dist[index] > threshold_dist)
            dist_corrected = dist[index] + threshold_dist;
        else if (dist_new - dist[index] < -threshold_dist)
            dist_corrected = dist[index] - threshold_dist;
        else
            dist_corrected = dist_new;
        return dist_corrected;
    }

    public int predict2int() {
        int result = -1;
        try {
            switch (model) {
                case "rf":
                    result = (int) rf.classifyInstance(sample);
                    break;
                case "logistic":
                    result = (int) logistic.classifyInstance(sample);
                    break;
                default:
                    break;
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public void add_probs() {
        try {
            switch (model) {
                case "rf":
                    distribution = rf.distributionForInstance(sample);
                    break;
                case "logistic":
                    distribution = logistic.distributionForInstance(sample);
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < classes.length; i++) {
            switch (classes[i]) {
                case "start":
                    probas_start.add(distribution[i]);
                    break;
                case "left":
                    probas_left.add(distribution[i]);
                    break;
                case "right":
                    probas_right.add(distribution[i]);
                    break;
                case "back":
                    probas_back.add(distribution[i]);
                    break;
                case "lock":
                    probas_lock.add(distribution[i]);
                    break;
                default:
                    break;
            }
        }


    }

    public String predict2str() {
        int result = predict2int();
        if (result != -1)
            return classes[result];
        return null;
    }

    public int vote2int() {
        int result = most(predictions);
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

    public String vote2str() {
        int result = vote2int();
        if (result != -1)
            return classes[result];
        return null;
    }

    public double sum(List<Double> list) {
        double result = 0;
        for (int i = 0; i < list.size(); i++) {
            result += list.get(i);
        }
        return result;
    }

    public int vote2int_proba() {
        for (int i = 0; i < classes.length; i++) {
            switch (classes[i]) {
                case "start":
                    proba_sum[i] = sum(probas_start);
                    break;
                case "left":
                    proba_sum[i] = sum(probas_left);
                    break;
                case "right":
                    proba_sum[i] = sum(probas_right);
                    break;
                case "back":
                    proba_sum[i] = sum(probas_back);
                    break;
                case "lock":
                    proba_sum[i] = sum(probas_lock);
                    break;
                default:
                    break;
            }
        }
        return max(proba_sum);
    }

    public String vote2str_proba() {
        int result = vote2int_proba();
        if (result != -1)
            return classes[result];
        return null;
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

    public int max(double[] list) {
        int index = 0;
        double max = list[index];
        for (int i = 1; i < list.length; i++) {
            if (list[i] > max) {
                max = list[i];
                index = i;
            }
        }
        return index;
    }

    public String printDist() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < dist.length; i++) {
            sb.append(String.format(Locale.FRANCE, "%.2f", dist[i])).append(" ");
        }
        sb.append("\n");
        return sb.toString();
    }
}
