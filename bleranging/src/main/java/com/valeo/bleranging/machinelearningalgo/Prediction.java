package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.BleRangingHelper;
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

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class Prediction {
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P = -30;
    private static final double THRESHOLD_DIST_AWAY = 0.3;
    private List<Integer> predictions = new ArrayList<>();
    private double[] distribution;
    private double[] distance;
    private int prediction_old = -1;
    private Instance sample;
    private RandomForest rf;
    private String[] classes;
    private Context mContext;

    public Prediction(Context context, int classesId, int rfId, int sampleId) {
        this.mContext = context;
        try {
            classes = (String[]) SerializationHelper.read(mContext.getResources().openRawResource(classesId));
            rf = (RandomForest) SerializationHelper.read(mContext.getResources().openRawResource(rfId));
            Instances instances = ConverterUtils.DataSource.read(mContext.getResources().openRawResource(sampleId));
            instances.setClassIndex(instances.numAttributes() - 1);
            sample = instances.instance(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void initTab(int rssiLength) {
        this.distance = new double[rssiLength];
        this.distribution = new double[classes.length];
    }

    public void init(int index, double rssi) {
        distance[index] = rssi2dist(rssi);
        sample.setValue(index, distance[index]);
    }

    public void setRssi(int index, double rssi) {
        if (prediction_old != -1) {
            // trx order : l, m, r, t, fl, fr, rl, rr
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                rssi -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }
            // Add hysteresis to all left sided trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT)
                    && (index == 0 | index == 4 | index == 6)) {
                rssi += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }
            // Add hysteresis to all right sided trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)
                    && (index == 2 | index == 5 | index == 7)) {
                rssi += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }
        }
        double dist_new = rssi2dist(rssi);
        distance[index] = correctUnilateral(distance[index], dist_new);
        sample.setValue(index, distance[index]);
    }

    public void finalizeSetRssi(int nVote) {
        if (predictions.size() == nVote) {
            predictions.remove(0);
        }
        predict();
    }

    public void predict() {
        int result = 0;
        try {
            result = (int) rf.classifyInstance(sample);
            distribution = rf.distributionForInstance(sample);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            predictions.add(result);
        }
    }

    private double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }

    private double correctUnilateral(double dist_old, double dist_new) {
        double dist_correted;
        if (dist_new < dist_old)
            dist_correted = dist_new;
        else
            dist_correted = Math.min(dist_new - dist_old, THRESHOLD_DIST_AWAY) + dist_old;
        return dist_correted;
    }

    public int calculatePrediction(double threshold_prob) {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return prediction_old;
        }
        int temp_prediction = most(predictions);
        if (distribution[temp_prediction] > threshold_prob) {
            prediction_old = temp_prediction;
        }
        return prediction_old;
    }

    public String getPrediction() {
        if (prediction_old != -1) {
            return classes[prediction_old];
        }
        return PREDICTION_UNKNOWN;
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

    public String printDebug(String title) {
        StringBuilder sb = new StringBuilder();
        if (distance == null) {
            return null;
        } else if (distribution == null) {
            return null;
        } else {
            sb.append(title).append(" ");
            for (double aDistance : distance) {
                sb.append(String.format(Locale.FRANCE, "%.2f", aDistance)).append(" ");
            }
            sb.append("\n");
            for (int i = 0; i < distribution.length; i++) {
                sb.append(classes[i]).append(": ").append(distribution[i]).append(" \n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }
}
