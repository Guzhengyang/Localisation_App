package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

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
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;

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
    private double[] rssi;
    private double[] rssi_offset;
    private int prediction_old = -1;
    private Instance sample;
    private RandomForest rf;
    private String[] classes;
    private Context mContext;
    private boolean arePredictRawFileRead = false;

    public Prediction(Context context, int classesId, int rfId, int sampleId) {
        this.mContext = context;
        new AsyncPredictionInit().execute(classesId, rfId, sampleId);
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.rssi = new double[rssi.length];
        this.distribution = new double[classes.length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;

//            distance[i] = rssi2dist(this.rssi_offset[i]);
//            sample.setValue(i, distance[i]);

            this.rssi[i] = rssi_offset[i];
            this.distance[i] = rssi2dist(this.rssi[i]);
            sample.setValue(i, this.rssi[i]);
        }
    }

    public void setRssi(int index, double rssi, int offset, double threshold, boolean comValid) {
        this.rssi_offset[index] = rssi - offset;
        if (prediction_old != -1) {
            // trx order : l, m, r, t, fl, fr, rl, rr
            // Add lock and outside hysteresis to all the trx
//            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
//                if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
//                        this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE)) {
//                    rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
//                }
//            }

            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE)) {
                rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }

            // Add unlock hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
                rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }

        }
        double dist_new = rssi2dist(rssi_offset[index]);
        if (comValid) {
            distance[index] = dist_new;
        } else {
            distance[index] = correctDistUnilateral(distance[index], dist_new, threshold);
        }
        sample.setValue(index, distance[index]);
    }

    public void setRssi(int index, double rssi, int offset, double threshold) {
        this.rssi_offset[index] = rssi - offset;
        if (prediction_old != -1) {
            // trx order : l, m, r, t, fl, fr, rl, rr
            // Add hysteresis to all the trx

//            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(THATCHAM_ORIENTED)) {
//                if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
//                    rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
//                }
//            }

            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_EXTERNAL)) {
                rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
            }

            // Add unlock hysteresis to all the trx
            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
                rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
            }
        }

//        double dist_new = rssi2dist(rssi_offset[index]);
//        distance[index] = correctDistUnilateral(distance[index], dist_new, threshold);
//        sample.setValue(index, distance[index]);

        this.rssi[index] = correctRssiUnilateral(this.rssi[index], rssi_offset[index]);
        distance[index] = rssi2dist(this.rssi[index]);
        sample.setValue(index, this.rssi[index]);
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

    public void calculatePredictionStandard(double threshold_prob, double threshold_prob_lock2unlock, double threshold_prob_unlock2lock, String orientation) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (orientation.equals(THATCHAM_ORIENTED)) {
//                lock --> left, right, front, back
                if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(temp_prediction, threshold_prob_lock2unlock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
//                left, right, front, back --> lock
                if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(temp_prediction, threshold_prob_unlock2lock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                if (compareDistribution(temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            } else if (orientation.equals(PASSIVE_ENTRY_ORIENTED)) {
//                left, right, front, back --> lock
                if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(temp_prediction, threshold_prob_unlock2lock)) {
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
    }

    public void calculatePredictionDefault(double threshold_prob, double threshold_prob_lock) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (comparePrediction(prediction_old, BleRangingHelper.PREDICTION_LEFT)
                    || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                    || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_BACK)
                    || comparePrediction(prediction_old, BleRangingHelper.PREDICTION_BACK)) {
                if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                        && compareDistribution(temp_prediction, threshold_prob_lock)) {
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

    public void calculatePredictionStart(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
//            cover internal space
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_INSIDE)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
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

    public void calculatePredictionInside(double threshold_prob) {
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
            return "";
        } else if (rssi == null) {
            return "";
        } else if (distribution == null) {
            return "";
        } else if (prediction_old == -1) {
            return "";
        } else {
            sb.append(title).append(" ").append(getPrediction()).append(" ").append(String.format(Locale.FRANCE, "%.2f", distribution[prediction_old])).append("\n");
            for (double arssi : rssi) {
                sb.append(String.format(Locale.FRANCE, "%d", (int) arssi)).append("      ");
            }
            sb.append("\n");
            for (double adistance : distance) {
                sb.append(String.format(Locale.FRANCE, "%.2f", adistance)).append("   ");
            }
            sb.append("\n");
            for (int i = 0; i < distribution.length; i++) {
                sb.append(classes[i]).append(": ").append(String.format(Locale.FRANCE, "%.2f", distribution[i])).append(" \n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    private class AsyncPredictionInit extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... elements) {
            try {
                classes = (String[]) SerializationHelper.read(mContext.getResources().openRawResource(elements[0]));
                rf = (RandomForest) SerializationHelper.read(mContext.getResources().openRawResource(elements[1]));
                Instances instances = ConverterUtils.DataSource.read(mContext.getResources().openRawResource(elements[2]));
                instances.setClassIndex(instances.numAttributes() - 1);
                sample = instances.instance(0);
                arePredictRawFileRead = true;
            } catch (Exception e) {
                e.printStackTrace();
                arePredictRawFileRead = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!arePredictRawFileRead) {
                Toast.makeText(mContext, "Init failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
