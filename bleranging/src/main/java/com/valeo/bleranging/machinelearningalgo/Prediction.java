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
import static com.valeo.bleranging.machinelearningalgo.AlgoManager.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.machinelearningalgo.AlgoManager.THATCHAM_ORIENTED;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class Prediction {
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P = -30;
    private static final double THRESHOLD_RSSI_AWAY = 1;
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
            // Add lock and outside hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE)) {
                rssiModified[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }

            // Add unlock hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
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

    public void setRssi(int index, double rssi, int offset, double threshold) {
        this.rssiModified[index] = rssi - offset;
        if (prediction_old != -1) {
            // trx order : l, m, r, t, fl, fr, rl, rr
            // Add hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                rssiModified[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }

        }
        double dist_new = rssi2dist(rssiModified[index]);
        distance[index] = correctDistUnilateral(distance[index], dist_new, threshold);
        sample.setValue(index, distance[index]);
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

    private boolean checkOldPrediction() {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return false;
        }
        return true;
    }

    private boolean comparePrediction(int calculatedPrediction, String expectedPrediction) {
        return classes[calculatedPrediction].equals(expectedPrediction);
    }

    private boolean compareDistribution(int temp_prediction, double threshold_prob) {
        return distribution[temp_prediction] > threshold_prob;
    }

    public void calculatePredictionStandard(double threshold_prob, double threshold_prob_unlock, String orientation) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (orientation.equals(THATCHAM_ORIENTED)) {
                if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_BACK)) {
                    if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LOCK)) {
                        if (compareDistribution(temp_prediction, threshold_prob_unlock)) {
                            prediction_old = temp_prediction;
                            return;
                        }
                    } else if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_TRUNK)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                if (compareDistribution(temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            } else if (orientation.equals(PASSIVE_ENTRY_ORIENTED)) {
                if (compareDistribution(temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            }
        }
    }

    public void calculatePredictionStandard(double threshold_prob, double threshold_prob_unlock) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LEFT)
                    || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_RIGHT)
                    || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_BACK)) {
                if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LOCK)) {
                    if (compareDistribution(temp_prediction, threshold_prob_unlock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                } else if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_TRUNK)) {
                    prediction_old = temp_prediction;
                    return;
                }
            }
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
                return;
            }
        }
    }

    public void calculatePredictionRP(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_FAR)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    public void calculatePredictionSimple(double threshold_prob, double threshold_prob_unlock, double threshold_prob_lock) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(temp_prediction, PREDICTION_LOCK) &&
                    compareDistribution(temp_prediction, threshold_prob_lock)) {
                prediction_old = temp_prediction;
            } else if ((comparePrediction(temp_prediction, PREDICTION_LEFT) ||
                    (comparePrediction(temp_prediction, PREDICTION_RIGHT))) &&
                    compareDistribution(temp_prediction, threshold_prob_unlock)) {
                prediction_old = temp_prediction;
            } else if (comparePrediction(temp_prediction, PREDICTION_START) &&
                    compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    public void calculatePredictionEar(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LOCK)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    public void calculatePredictionStart(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_OUTSIDE)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    public void calculatePredictionFull(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
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
