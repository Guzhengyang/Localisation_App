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

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_EXTERNAL;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LOCK;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_OUTSIDE;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionZone {
    private static final double f = 2.45 * Math.pow(10, 9);
    private static final double c = 3 * Math.pow(10, 8);
    private static final double P = -22;
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private List<Integer> predictions = new ArrayList<>();
    private double[] distribution;
    private double[] distance;
    private double[] rssi;
    private double[] rssi_offset;
    private int prediction_old = -1;
    private Context mContext;
    private boolean arePredictRawFileRead = false;
    private int INDEX_LOCK;
    private boolean isThresholdMethod = false;
    private StringBuilder sb = new StringBuilder();
    private EasyPredictModelWrapper modelWrapper;
    private RowData rowData;
    private List<String> rowDataKeySet;
    private String label;
    private String predictionType;

    public PredictionZone(Context context, String modelClassName, List<String> rowDataKeySet, String predictionType) {
        this.mContext = context;
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        this.predictionType = predictionType;
        new AsyncPredictionInit().execute(modelClassName);
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

//    public void setRssi(int index, double rssi, int offset, double threshold, boolean comValid, boolean lockStatus) {
//        this.rssi_offset[index] = rssi - offset;
//        if (prediction_old != -1) {
//            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
//                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE)) {
//                rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
//            }
//
//            // Add unlock hysteresis to all the trx
//            if (this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
//                    this.classes[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
//                rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
//            }
//        }
//        double dist_new = rssi2dist(rssi_offset[index]);
//        if (comValid) {
//            distance[index] = dist_new;
//        } else {
//            distance[index] = correctDistUnilateral(distance[index], dist_new, threshold);
//        }
//    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.rssi = new double[rssi.length];
        this.distribution = new double[modelWrapper.getResponseDomainValues().length];
//        find index of lock
        for (int i = 0; i < modelWrapper.getResponseDomainValues().length; i++) {
            if (modelWrapper.getResponseDomainValues()[i].equalsIgnoreCase(PREDICTION_LOCK)
                    || modelWrapper.getResponseDomainValues()[i].equalsIgnoreCase(PREDICTION_EXTERNAL)
                    || modelWrapper.getResponseDomainValues()[i].equalsIgnoreCase(PREDICTION_OUTSIDE)) {
                INDEX_LOCK = i;
                break;
            }
        }
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi[i] = rssi_offset[i];
            this.distance[i] = rssi2dist(this.rssi[i]);
        }
        constructRowData(rssi);
    }

    public void setRssi(double rssi[], int offset, boolean lockStatus) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                if (prediction_old != -1) {
                    // trx order : l, m, r, t, fl, fr, rl, rr
                    // Add lock hysteresis to all the trx
                    if (this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK) |
                            this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_OUTSIDE) |
                            this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_EXTERNAL) |
                            this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_FAR)) {
                        rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
                    }
                    // Add unlock hysteresis to all the trx
                    if (this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
                            this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
                        rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                    }
//            if(lockStatus){
//                rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
//            }else {
//                rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
//            }
                }

                this.rssi[index] = correctRssiUnilateral(this.rssi[index], rssi_offset[index]);
                distance[index] = rssi2dist(this.rssi[index]);
            }
            constructRowData(this.rssi);
        }
    }

    private void constructRowData(double[] rssi) {
        if (rssi == null) {
            return;
        }
        int i = 0;
        for (String elem : rowDataKeySet) {
            if (i < rssi.length) {
                rowData.put(elem, String.valueOf(rssi[i]));
                i++;
            }
        }
    }

    public void predict(int nVote) {
        int result = 0;
        try {
            if (predictionType.equalsIgnoreCase(PredictionFactory.PREDICTION_RP)) {
                final BinomialModelPrediction modelPrediction = modelWrapper.predictBinomial(rowData);
                label = modelPrediction.label;
                result = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else {
                if (rssi.length == 8 | rssi.length == 6) {
                    final MultinomialModelPrediction modelPrediction = modelWrapper.predictMultinomial(rowData);
                    label = modelPrediction.label;
                    result = modelPrediction.labelIndex;
                    distribution = modelPrediction.classProbabilities;
                } else {
                    final BinomialModelPrediction modelPrediction = modelWrapper.predictBinomial(rowData);
                    label = modelPrediction.label;
                    result = modelPrediction.labelIndex;
                    distribution = modelPrediction.classProbabilities;
                }
            }
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
        if (rssi_new > rssi_old) {
            rssi_correted = rssi_new;
        } else {
            rssi_correted = rssi_old - Math.min(rssi_old - rssi_new, THRESHOLD_RSSI_AWAY);
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
        return modelWrapper.getResponseDomainValues()[calculatedPrediction].equals(expectedPrediction);
    }

    private boolean compareDistribution(int temp_prediction, double threshold_prob) {
        return distribution[temp_prediction] > threshold_prob;
    }

    //    4, 6, 8 beacons prediction
    public void calculatePredictionStandard(double threshold_prob, double threshold_prob_lock2unlock, double threshold_prob_unlock2lock, String orientation) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (orientation.equals(THATCHAM_ORIENTED)) {
                //                when no decision for lock is made, use threshold method
                if (ifNoDecision2Lock(distribution, threshold_prob_unlock2lock)) {
                    if (if2Lock(rssi, SdkPreferencesHelper.getInstance().getThresholdLock())) {
                        prediction_old = INDEX_LOCK;
                        isThresholdMethod = true;
                        return;
                    }
                }
                isThresholdMethod = false;
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
//                when no decision for lock is made, use threshold method
                if (ifNoDecision2Lock(distribution, threshold_prob_unlock2lock)) {
                    if (if2Lock(rssi, SdkPreferencesHelper.getInstance().getThresholdLock())) {
                        prediction_old = INDEX_LOCK;
                        isThresholdMethod = true;
                        return;
                    }
                }
                isThresholdMethod = false;
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

    //    3 beacons prediction
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
            if (comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_INSIDE) ||
                    comparePrediction(temp_prediction, BleRangingHelper.PREDICTION_INTERNAL)) {
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
//            reduce false positive
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
            return modelWrapper.getResponseDomainValues()[prediction_old];
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
        sb.setLength(0);
        if (distance == null) {
            return "";
        } else if (rssi == null) {
            return "";
        } else if (distribution == null) {
            return "";
        } else if (prediction_old == -1) {
            return "";
        } else if (label == null) {
            return "";
        } else {
//            if (isThresholdMethod) {
//                sb.append("Threshold\n");
//            } else {
//                sb.append("Machine Learning\n");
//            }
//            sb.append("Current Prediction Label: ").append(label).append("\n");
            sb.append(String.format(Locale.FRANCE, "%1$s %2$s %3$.2f", title, getPrediction(), distribution[prediction_old])).append("\n");
            for (double arssi : rssi) {
                sb.append(String.format(Locale.FRANCE, "%d", (int) arssi)).append("      ");
            }
            sb.append("\n");
            for (double adistance : distance) {
                sb.append(String.format(Locale.FRANCE, "%.2f", adistance)).append("   ");
            }
            sb.append("\n");
            for (int i = 0; i < distribution.length; i++) {
                sb.append(modelWrapper.getResponseDomainValues()[i]).append(": ").append(String.format(Locale.FRANCE, "%.2f", distribution[i])).append(" \n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    public double[] getDistribution() {
        return distribution;
    }

    public String[] getClasses() {
        if (modelWrapper != null) {
            return modelWrapper.getResponseDomainValues();
        } else {
            return null;
        }
    }

    private double max(double[] rssi) {
        double result = rssi[0];
        for (int i = 1; i < rssi.length; i++) {
            if (rssi[i] > result) {
                result = rssi[i];
            }
        }
        return result;
    }

    private boolean ifNoDecision2Lock(double[] distribution, double threshold_prob_unlock2lock) {
        return distribution[INDEX_LOCK] <= threshold_prob_unlock2lock;
    }

    private boolean if2Lock(double[] rssi, double threshold_rssi_lock) {
        boolean result = true;
        for (int i = 0; i < rssi.length; i++) {
            if (rssi[i] > threshold_rssi_lock) {
                result = false;
                break;
            }
        }
        return result;
    }

    public double[] getRssi() {
        return rssi;
    }

    private class AsyncPredictionInit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... elements) {
            try {
                hex.genmodel.GenModel rawModel = (hex.genmodel.GenModel) Class.forName(elements[0]).newInstance();
                modelWrapper = new EasyPredictModelWrapper(rawModel);
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
                Toast.makeText(mContext, "Init for Random Forest failed", Toast.LENGTH_LONG).show();
            }
        }
    }
}
