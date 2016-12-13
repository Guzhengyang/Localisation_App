package com.valeo.bleranging.model;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

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

import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.MODEL_LOGISTIC;
import static com.valeo.bleranging.model.connectedcar.ConnectedCarFactory.MODEL_RF;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    public static final String PREDICTION_START = "start";
    public static final String PREDICTION_LOCK = "lock";
    public static final String PREDICTION_LEFT = "left";
    public static final String PREDICTION_RIGHT = "right";
    public static final String PREDICTION_BACK = "back";
    private static final double POWER_0 = -30;
    private static final double THRESHOLD_PROB = 0.8;
    private static final int N_VOTE = 3;
    private static final double FREQUENCY = 2.45 * Math.pow(10, 9);
    private static final double LIGHT_SPEED = 3 * Math.pow(10, 8);
    private static final double THRESHOLD_DIST = 0.25;
    public String[] classes;
    private List<Integer> predictions = new ArrayList<>();
    private double[] proba_sum;
    private double[] distribution = new double[5];
    private double[] dist;
    private int prediction_old = -1;
    private Instance sample;
    private RandomForest rf;
    private Logistic logistic;

    private List<Double> probas_left = new ArrayList<>();
    private List<Double> probas_right = new ArrayList<>();
    private List<Double> probas_back = new ArrayList<>();
    private List<Double> probas_lock = new ArrayList<>();
    private List<Double> probas_start = new ArrayList<>();


    public Ranging(Context context, double[] rssi) {
        try {
            int rawLogistic;
            int rawClasses;
            int rawRf;
            int rawSample;
            if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
                rawLogistic = R.raw.four_lmrt_logistic;
                rawClasses = R.raw.four_lmrt_classes;
                rawRf = R.raw.four_lmrt_rf;
                rawSample = R.raw.four_lmrt_sample;
            } else {
                rawLogistic = R.raw.eight_flfrlmrtrlrr_logistic;
                rawClasses = R.raw.eight_flfrlmrtrlrr_classes;
                rawRf = R.raw.eight_flfrlmrtrlrr_rf;
                rawSample = R.raw.eight_flfrlmrtrlrr_sample;
            }
            logistic = (Logistic) SerializationHelper.read(context.getResources().openRawResource(rawLogistic));
            classes = (String[]) SerializationHelper.read(context.getResources().openRawResource(rawClasses));
            rf = (RandomForest) SerializationHelper.read(context.getResources().openRawResource(rawRf));
            Instances instances = ConverterUtils.DataSource.read(context.getResources().openRawResource(rawSample));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        dist = new double[rssi.length];
        for (int i = 0; i < rssi.length; i++) {
            dist[i] = rssi2dist(rssi[i]);
            sample.setValue(i, dist[i]);
        }

        proba_sum = new double[classes.length];
        predictions.add(predict2int());
        add_probs();

    }

    private double rssi2dist(double rssi) {
        return LIGHT_SPEED / FREQUENCY / 4 / Math.PI * Math.pow(10, -(rssi - POWER_0) / 20);
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
        if (predictions.size() == N_VOTE) {
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

    private double correct_unilateral(int index, double dist_new) {
        double dist_correted;
        if (dist_new < dist[index])
            dist_correted = dist_new;
        else
            dist_correted = Math.min(dist_new - dist[index], THRESHOLD_DIST) + dist[index];
        return dist_correted;
    }

    public double correct_bilateral(int index, double dist_new) {
        double dist_corrected;
        if (dist_new - dist[index] > THRESHOLD_DIST) {
            dist_corrected = dist[index] + THRESHOLD_DIST;
        } else if (dist_new - dist[index] < -THRESHOLD_DIST) {
            dist_corrected = dist[index] - THRESHOLD_DIST;
        } else {
            dist_corrected = dist_new;
        }
        return dist_corrected;
    }

    private int predict2int() {
        int result = -1;
        try {
            switch (SdkPreferencesHelper.getInstance().getMachineLearningModel()) {
                case MODEL_RF:
                    result = (int) rf.classifyInstance(sample);
                    break;
                case MODEL_LOGISTIC:
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

    private void add_probs() {
        try {
            switch (SdkPreferencesHelper.getInstance().getMachineLearningModel()) {
                case MODEL_RF:
                    distribution = rf.distributionForInstance(sample);
                    break;
                case MODEL_LOGISTIC:
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
                case PREDICTION_START:
                    probas_start.add(distribution[i]);
                    break;
                case PREDICTION_LEFT:
                    probas_left.add(distribution[i]);
                    break;
                case PREDICTION_RIGHT:
                    probas_right.add(distribution[i]);
                    break;
                case PREDICTION_BACK:
                    probas_back.add(distribution[i]);
                    break;
                case PREDICTION_LOCK:
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

    private int vote2int() {
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
            if (distribution[vote2int()] > THRESHOLD_PROB) {
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

    private double sum(List<Double> list) {
        double result = 0;
        for (int i = 0; i < list.size(); i++) {
            result += list.get(i);
        }
        return result;
    }

    private int vote2int_proba() {
        for (int i = 0; i < classes.length; i++) {
            switch (classes[i]) {
                case PREDICTION_START:
                    proba_sum[i] = sum(probas_start);
                    break;
                case PREDICTION_LEFT:
                    proba_sum[i] = sum(probas_left);
                    break;
                case PREDICTION_RIGHT:
                    proba_sum[i] = sum(probas_right);
                    break;
                case PREDICTION_BACK:
                    proba_sum[i] = sum(probas_back);
                    break;
                case PREDICTION_LOCK:
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

    private int max(double[] list) {
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
