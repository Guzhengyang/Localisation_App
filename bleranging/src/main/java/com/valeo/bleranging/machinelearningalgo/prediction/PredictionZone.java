package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.widget.Toast;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

import static com.valeo.bleranging.persistence.Constants.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_BACK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_EAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FAR;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FRONT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INSIDE;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_INTERNAL;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LEFT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RIGHT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RP;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.THATCHAM_ORIENTED;
import static com.valeo.bleranging.utils.CalculUtils.correctRssiUnilateral;
import static com.valeo.bleranging.utils.CalculUtils.most;
import static com.valeo.bleranging.utils.CalculUtils.rssi2dist;
import static com.valeo.bleranging.utils.CheckUtils.compareDistribution;
import static com.valeo.bleranging.utils.CheckUtils.comparePrediction;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionZone extends BasePrediction {
    private List<Integer> predictions = new ArrayList<>();// Machine learning prediction history list
    private double[] distribution;// prob for each Machine learning class
    private double[] distance;// distance converted using path loss propagation
    private String label;// prediction result string
    private String predictionType;// standard prediction or rp prediction
    private int INDEX_LOCK;// find the index of lock zone
    private int prediction_old = -1;// old prediction result index
    private StringBuilder sb = new StringBuilder();
    private double threshold = 0.5;

    public PredictionZone(Context context, String modelClassName,
                          List<String> rowDataKeySet, String predictionType) {
        super(context, modelClassName, rowDataKeySet);
        this.predictionType = predictionType;
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.modified_rssi = new double[rssi.length];
        this.distribution = new double[modelWrappers.get(0).getResponseDomainValues().length];
        // find index of lock
        for (int i = 0; i < modelWrappers.get(0).getResponseDomainValues().length; i++) {
            if (modelWrappers.get(0).getResponseDomainValues()[i].equalsIgnoreCase(PREDICTION_LOCK)) {
                INDEX_LOCK = i;
                break;
            }
        }
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.modified_rssi[i] = rssi_offset[i];
            this.distance[i] = rssi2dist(this.modified_rssi[i]);
        }
        constructRowData(this.modified_rssi);
    }

    public void initTest(double[] rssi) {
        this.modified_rssi = new double[rssi.length];
        this.distribution = new double[modelWrappers.get(0).getResponseDomainValues().length];
    }

    public void setRssi(double rssi[], int offset) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                if (prediction_old != -1) {
                    // Add lock hysteresis to all the trx
                    if (this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_LOCK)) {
                        rssi_offset[index] -= SdkPreferencesHelper.getInstance().getOffsetHysteresisLock();
                    }
                    // Add unlock hysteresis to all the trx
                    if (this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_LEFT) |
                            this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_RIGHT)) {
                        rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                    }
                }
                this.modified_rssi[index] = correctRssiUnilateral(this.modified_rssi[index], rssi_offset[index]);
                distance[index] = rssi2dist(this.modified_rssi[index]);
            }
            constructRowData(this.modified_rssi);
        }
    }

    public void predict(int nVote) {
        int result = 0;
        try {
            if (predictionType.equalsIgnoreCase(PREDICTION_RP)) {
                final BinomialModelPrediction modelPrediction = modelWrappers.get(0).predictBinomial(rowData);
                label = modelPrediction.label;
                result = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else if (predictionType.equalsIgnoreCase(PREDICTION_EAR)) {
                final MultinomialModelPrediction modelPrediction = modelWrappers.get(0).predictMultinomial(rowData);
                label = modelPrediction.label;
                result = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else {
                final MultinomialModelPrediction modelPrediction = modelWrappers.get(0).predictMultinomial(rowData);
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
                final BinomialModelPrediction modelPrediction = modelWrappers.get(0).predictBinomial(rowData);
                label = modelPrediction.label;
//                prediction_old = modelPrediction.labelIndex;
                distribution = modelPrediction.classProbabilities;
            } else {
                final MultinomialModelPrediction modelPrediction = modelWrappers.get(0).predictMultinomial(rowData);
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
                if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LOCK)
                            && compareDistribution(distribution, temp_prediction, threshold_prob_lock2unlock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
//                left, right, front, back --> lock
                if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LOCK)
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
                if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LOCK)
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
            if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LEFT)
                    || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_RIGHT)
                    || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)
                    || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)) {
                if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LOCK)
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
            if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_INSIDE) ||
                    comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_INTERNAL)) {
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
            if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_FAR)) {
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
        if (compareDistribution(distribution, 0, threshold)) {
            prediction_old = 0;
        } else {
            prediction_old = 1;
        }
    }

    public String getPrediction() {
        if (prediction_old != -1) {
            return modelWrappers.get(0).getResponseDomainValues()[prediction_old];
        }
        return PREDICTION_UNKNOWN;
    }

    public String printDebug() {
        sb.setLength(0);
        if (distance == null) {
            return "";
        } else if (modified_rssi == null) {
            return "";
        } else if (distribution == null) {
            return "";
        } else if (prediction_old == -1) {
            return "";
        } else if (label == null) {
            return "";
        } else {
            sb.append(String.format(Locale.FRANCE, "%1$s %2$s ", predictionType, getPrediction())).append("\n");
            for (double arssi : modified_rssi) {
                sb.append(String.format(Locale.FRANCE, "%d", (int) arssi)).append("      ");
            }
            sb.append("\n");
            for (double adistance : distance) {
                sb.append(String.format(Locale.FRANCE, "%.2f", adistance)).append("   ");
            }
            sb.append("\n");
            for (int i = 0; i < distribution.length; i++) {
                sb.append(modelWrappers.get(0).getResponseDomainValues()[i]).append(": ").append(String.format(Locale.FRANCE, "%.2f", distribution[i])).append(" \n");
            }
            sb.append("\n");
            return sb.toString();
        }
    }

    public String printDebugTest(String title) {
        sb.setLength(0);
        if (distribution == null || modified_rssi == null) {
            return "";
        }
        PSALogs.d("debug", prediction_old + " " + threshold);
        sb.append(String.format(Locale.FRANCE, "%1$s %2$s ", title, getPrediction())).append("\n");
        for (double arssi : modified_rssi) {
            sb.append(String.format(Locale.FRANCE, "%d", (int) arssi)).append("      ");
        }
        sb.append("\n");
        for (int i = 0; i < distribution.length; i++) {
            sb.append(modelWrappers.get(0).getResponseDomainValues()[i]).append(": ").append(String.format(Locale.FRANCE, "%.2f", distribution[i])).append(" \n");
        }
        sb.append("\n");
        return sb.toString();
    }

    public double[] getDistribution() {
        return distribution;
    }

    public void setThreshold(Double threshold) {
        this.threshold = threshold;
    }

    public String[] getClasses() {
        if (modelWrappers.get(0) != null) {
            return modelWrappers.get(0).getResponseDomainValues();
        } else {
            return null;
        }
    }
}
