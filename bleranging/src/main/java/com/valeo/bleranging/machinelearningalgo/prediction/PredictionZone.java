package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.widget.Toast;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.model.connectedcar.ConnectedCar.THATCHAM_ORIENTED;
import static com.valeo.bleranging.utils.CalculUtils.correctRssiUnilateral;
import static com.valeo.bleranging.utils.CalculUtils.most;
import static com.valeo.bleranging.utils.CalculUtils.rssi2dist;
import static com.valeo.bleranging.utils.CheckUtils.compareDistribution;
import static com.valeo.bleranging.utils.CheckUtils.comparePrediction;
import static com.valeo.bleranging.utils.CheckUtils.if2Lock;
import static com.valeo.bleranging.utils.CheckUtils.ifNoDecision2Lock;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionZone {
    private List<Integer> predictions = new ArrayList<>();// Machine learning prediction history list
    private double[] distribution;// prob for each Machine learning class
    private double[] distance;// distance converted using path loss propagation
    private String label;// prediction result string
    private String predictionType;// standard prediction or rp prediction
    private int INDEX_LOCK;// find the index of lock zone
    private int prediction_old = -1;// old prediction result index
    private StringBuilder sb = new StringBuilder();
    private boolean binomial;// whether the model is binomial
    private double threshold = 0.5;

    public PredictionZone(Context context, String modelClassName,
                          List<String> rowDataKeySet, String predictionType) {
        super(context, modelClassName, rowDataKeySet);
        this.predictionType = predictionType;
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.rssi = new double[rssi.length];
        this.distribution = new double[modelWrapper.getResponseDomainValues().length];
        // find index of lock
        for (int i = 0; i < modelWrapper.getResponseDomainValues().length; i++) {
            if (modelWrapper.getResponseDomainValues()[i].equalsIgnoreCase(PREDICTION_LOCK)) {
                INDEX_LOCK = i;
                break;
            }
        }
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi[i] = rssi_offset[i];
            this.distance[i] = rssi2dist(this.rssi[i]);
        }
        constructRowData(this.rssi);
    }

    public void initTest(double[] rssi) {
        this.rssi = new double[rssi.length];
        this.distribution = new double[modelWrapper.getResponseDomainValues().length];
        constructRowDataTest(this.rssi);
    }

    public void setRssi(double rssi[], int offset, boolean lockStatus) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                if (prediction_old != -1) {
                    // Add lock hysteresis to all the trx
                    if (this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_LOCK)) {
                        rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
                    }
                    // Add unlock hysteresis to all the trx
                    if (this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_LEFT) |
                            this.modelWrapper.getResponseDomainValues()[prediction_old].equals(BleRangingHelper.PREDICTION_RIGHT)) {
                        rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                    }
                }
                this.rssi[index] = correctRssiUnilateral(this.rssi[index], rssi_offset[index]);
                distance[index] = rssi2dist(this.rssi[index]);
            }
            constructRowData(this.rssi);
        }
    }

    public void predict(int nVote) {
        int result = 0;
        try {
            if (binomial) {
                final BinomialModelPrediction modelPrediction = modelWrapper.predictBinomial(rowData);
                label = modelPrediction.label;
                result = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else {
                final MultinomialModelPrediction modelPrediction = modelWrapper.predictMultinomial(rowData);
                label = modelPrediction.label;
                result = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(mContext, e.toString(), Toast.LENGTH_LONG).show();
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

    public void predictTest() {
        try {
            if (binomial) {
                final BinomialModelPrediction modelPrediction = modelWrapper.predictBinomial(rowData);
                label = modelPrediction.label;
//                prediction_old = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else {
                final MultinomialModelPrediction modelPrediction = modelWrapper.predictMultinomial(rowData);
                label = modelPrediction.label;
//                prediction_old = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //    4, 6, 8 beacons prediction
    public void calculatePredictionStandard(double threshold_prob, double threshold_prob_lock2unlock, double threshold_prob_unlock2lock, String strategy) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (strategy.equals(THATCHAM_ORIENTED)) {
//                lock --> left, right, front, back
                if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(distribution, temp_prediction, threshold_prob_lock2unlock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
//                left, right, front, back --> lock
                if (comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(distribution, temp_prediction, threshold_prob_unlock2lock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                if (compareDistribution(distribution, temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            } else if (strategy.equals(PASSIVE_ENTRY_ORIENTED)) {
//                left, right, front, back --> lock
                if (comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_LEFT)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_BACK)
                        || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                            && compareDistribution(distribution, temp_prediction, threshold_prob_unlock2lock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                if (compareDistribution(distribution, temp_prediction, threshold_prob)) {
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
            if (comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_LEFT)
                    || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_RIGHT)
                    || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_BACK)
                    || comparePrediction(modelWrapper, prediction_old, BleRangingHelper.PREDICTION_BACK)) {
                if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_LOCK)
                        && compareDistribution(distribution, temp_prediction, threshold_prob_lock)) {
                    prediction_old = temp_prediction;
                    return;
                }
            }
            if (compareDistribution(distribution, temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
                return;
            }
        }
    }

    public void calculatePredictionStart(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
//            cover internal space
            if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_INSIDE) ||
                    comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_INTERNAL)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(distribution, temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    public void calculatePredictionRP(double threshold_prob) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
//            reduce false positive
            if (comparePrediction(modelWrapper, temp_prediction, BleRangingHelper.PREDICTION_FAR)) {
                prediction_old = temp_prediction;
                return;
            }
            if (compareDistribution(distribution, temp_prediction, threshold_prob)) {
                prediction_old = temp_prediction;
            }
        }
    }

    private boolean checkOldPrediction() {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return false;
        }
        return true;
    }
    public void calculatePredictionTest() {
        if (compareDistribution(0, threshold)) {
            prediction_old = 0;
        } else {
            prediction_old = 1;
        }
    }

    public String getPrediction() {
        if (prediction_old != -1) {
            return modelWrapper.getResponseDomainValues()[prediction_old];
        }
        return PREDICTION_UNKNOWN;
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
            sb.append(String.format(Locale.FRANCE, "%1$s %2$s ", title, getPrediction())).append("\n");
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

    public String printDebugTest(String title) {
        sb.setLength(0);
        if (distribution == null || rssi == null) {
            return "";
        }
        PSALogs.d("debug", prediction_old + " " + threshold);
        sb.append(String.format(Locale.FRANCE, "%1$s %2$s ", title, getPrediction())).append("\n");
        for (double arssi : rssi) {
            sb.append(String.format(Locale.FRANCE, "%d", (int) arssi)).append("      ");
        }
        sb.append("\n");
        for (int i = 0; i < distribution.length; i++) {
            sb.append(modelWrapper.getResponseDomainValues()[i]).append(": ").append(String.format(Locale.FRANCE, "%.2f", distribution[i])).append(" \n");
        }
        sb.append("\n");
        return sb.toString();
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
}
