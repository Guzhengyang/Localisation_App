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

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_START;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class Prediction {
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P = -30;
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private static final double THRESHOLD_PROB_NO_PSU = 0.6;
    private static final double THRESHOLD_PROB_UIR = 0.9;
    private static final double THRESHOLD_PROB_START = 0.8;
    private List<Integer> predictions = new ArrayList<>();
    private double[] distribution;
    private double[] distance;
    private double[] rssiModified;
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

    public void init(double[] rssi, int offset) {
        this.rssiModified = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.distribution = new double[classes.length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssiModified[i] = rssi[i] - offset;
            distance[i] = rssi2dist(rssi[i]);
            sample.setValue(i, distance[i]);
        }
    }

    public void setRssi(int index, double rssi, int offset, double threshold, boolean comValid) {
        this.rssiModified[index] = rssi - offset;
        if (prediction_old != -1) {
            // trx order : l, m, r, t, fl, fr, rl, rr
            // Add hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                rssiModified[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }

            // Add hysteresis to all left sided trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT)
                    && (index == 0 | index == 4 | index == 6)) {
                rssiModified[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }
            // Add hysteresis to all right sided trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)
                    && (index == 2 | index == 5 | index == 7)) {
                rssiModified[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }
        }
        double dist_new = rssi2dist(rssiModified[index]);
        if (comValid) {
            distance[index] = dist_new;
        } else {
            distance[index] = correctDistUnilateral(distance[index], dist_new, threshold);
        }
        sample.setValue(index, distance[index]);
    }

    public void clearPredictions() {
        for (int i = 0; i < predictions.size(); i++) {
            predictions.remove(i);
        }
    }


    public void predict(int nVote) {
        int result = 0;
        try {
            result = (int) rf.classifyInstance(sample);
            distribution = rf.distributionForInstance(sample);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (predictions.size() == nVote) {
                predictions.remove(0);
            }
            predictions.add(result);
            if (prediction_old == -1) {
                prediction_old = result;
            }
        }
    }


    private double rssi2dist(double rssi) {
        return c / f / 4 / Math.PI * Math.pow(10, -(rssi - P) / 20);
    }

    private double correctDistUnilateral(double dist_old, double dist_new, double threshold) {
        double dist_correted;
        if (dist_new < dist_old) {
            dist_correted = dist_new;
        } else {
            dist_correted = Math.min(dist_new - dist_old, threshold) + dist_old;
        }
        return dist_correted;
    }


    private double correctRssiUnilateral(double rssi_old, double rssi_new) {
        double rssi_correted;
        if (rssi_new < rssi_old) {
            rssi_correted = rssi_new;
        } else {
            rssi_correted = Math.min(rssi_new - rssi_old, THRESHOLD_RSSI_AWAY) + rssi_old;
        }
        return rssi_correted;
    }


    public void calculatePredictionStandard(double threshold_prob) {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return;
        }
        int temp_prediction = most(predictions);

        if (classes[temp_prediction].equals(BleRangingHelper.PREDICTION_LEFT)
                | classes[temp_prediction].equals(BleRangingHelper.PREDICTION_RIGHT)
                | classes[temp_prediction].equals(BleRangingHelper.PREDICTION_BACK)) {
            if (classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                if (distribution[temp_prediction] > THRESHOLD_PROB_UIR) {
                    prediction_old = temp_prediction;
                    return;
                }
            } else if (classes[prediction_old].equals(BleRangingHelper.PREDICTION_TRUNK)) {
                prediction_old = temp_prediction;
                return;
            }
        }

        if (distribution[temp_prediction] > threshold_prob) {
            prediction_old = temp_prediction;
            return;
        }
    }

    public void calculatePredictionRP(double threshold_prob) {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return;
        }
        int temp_prediction = most(predictions);
        if (classes[temp_prediction].equals(BleRangingHelper.PREDICTION_FAR)) {
            prediction_old = temp_prediction;
            return;
        }
        if (distribution[temp_prediction] > threshold_prob) {
            prediction_old = temp_prediction;
        }

    }


    public void calculatePredictionSimple() {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return;
        }
        int temp_prediction = most(predictions);
        if (classes[temp_prediction].equals(PREDICTION_LOCK) &&
                distribution[temp_prediction] > THRESHOLD_PROB_NO_PSU) {
            prediction_old = temp_prediction;
        } else if ((classes[temp_prediction].equals(PREDICTION_LEFT) ||
                (classes[temp_prediction].equals(PREDICTION_RIGHT))) &&
                distribution[temp_prediction] > THRESHOLD_PROB_UIR) {
            prediction_old = temp_prediction;
        } else if (classes[temp_prediction].equals(PREDICTION_START) &&
                distribution[temp_prediction] > THRESHOLD_PROB_START) {
            prediction_old = temp_prediction;
        }
    }


    public void calculatePredictionEar(double threshold_prob) {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return;
        }
        int temp_prediction = most(predictions);
        if (classes[temp_prediction].equals(BleRangingHelper.PREDICTION_LOCK)) {
            prediction_old = temp_prediction;
            return;
        }
        if (distribution[temp_prediction] > threshold_prob) {
            prediction_old = temp_prediction;
        }
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
        } else if (prediction_old == -1) {
            return null;
        } else {
            sb.append(title).append(" ").append(getPrediction()).append(" ").append(distribution[prediction_old]).append("\n");
            for (double aDistance : distance) {
                sb.append(String.format(Locale.FRANCE, "%.2f", aDistance)).append("   ");
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
