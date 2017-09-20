package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.widget.Toast;

import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.prediction.BinomialModelPrediction;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;

import static com.valeo.bleranging.persistence.Constants.PASSIVE_ENTRY_ORIENTED;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_ACCESS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_BACK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_FRONT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LEFT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_LOCK;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_RIGHT;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_UNKNOWN;
import static com.valeo.bleranging.persistence.Constants.THATCHAM_ORIENTED;
import static com.valeo.bleranging.utils.CalculUtils.correctRssiUnilateral;
import static com.valeo.bleranging.utils.CalculUtils.most;
import static com.valeo.bleranging.utils.CalculUtils.rssi2dist;
import static com.valeo.bleranging.utils.CheckUtils.comparePrediction;
import static com.valeo.bleranging.utils.CheckUtils.compareProb;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionZone extends BasePrediction {
    private final List<Integer> predictions = new ArrayList<>(); // ML prediction history list
    private final String predictionType; // standard prediction or rp prediction
    private final StringBuilder sb = new StringBuilder();
    private double[] distribution; // prob for each Machine learning class
    private double[] distance; // converted distance using path loss propagation
    private String label; // prediction result
    private int prediction_old = -1; // old prediction result index
    private double thresholdROC = 0.5; // prob threshold for binary classification

    public PredictionZone(Context context, String modelClassName,
                          List<String> rowDataKeySet, String predictionType) {
        super(context, modelClassName, rowDataKeySet);
        this.predictionType = predictionType;
    }

    /***
     *  initialization
     * @param rssi raw rssi vector
     * @param offset smartphone offset value
     */
    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.distance = new double[rssi.length];
        this.rssi_modified = new double[rssi.length];
        this.distribution = new double[modelWrappers.get(0).getResponseDomainValues().length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi_modified[i] = rssi_offset[i];
            this.distance[i] = rssi2dist(this.rssi_modified[i]);
        }
        constructRowData(this.rssi_modified);
    }

    /***
     * update rowData object for ML prediction
     * @param rssi raw rssi vector
     * @param offset
     */
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
                            this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_RIGHT) |
                            this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_FRONT) |
                            this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_BACK) |
                            this.modelWrappers.get(0).getResponseDomainValues()[prediction_old].equals(PREDICTION_ACCESS)) {
                        rssi_offset[index] += SdkPreferencesHelper.getInstance().getOffsetHysteresisUnlock();
                    }
                }
                this.rssi_modified[index] = correctRssiUnilateral(this.rssi_modified[index], rssi_offset[index]);
                distance[index] = rssi2dist(this.rssi_modified[index]);
            }
            constructRowData(this.rssi_modified);
        }
    }

    /***
     * predict result using ML model and rowData sample vector
     * @param nVote number of results kept in the history list
     */
    public void predict(int nVote) {
        int result = 0;
        try {
            if (binomial) {
                final BinomialModelPrediction modelPrediction = modelWrappers.get(0).predictBinomial(rowData);
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

    /***
     * update prediction result
     * @param threshold_prob prob for the change of zone by default
     * @param threshold_prob_lock2unlock prob for changing from lock zone to unlock zone
     * @param threshold_prob_unlock2lock prob for changing from unlock zone to lock zone
     * @param strategy passive entry oriented(normal) or thatcham oriented
     */
    public void calculatePrediction(double threshold_prob, double threshold_prob_lock2unlock, double threshold_prob_unlock2lock, String strategy) {
        if (checkOldPrediction()) {
            int temp_prediction = most(predictions);
            if (strategy.equals(THATCHAM_ORIENTED)) {
                // lock zone--> unlock zone
                if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LOCK)
                            && compareProb(distribution, temp_prediction, threshold_prob_lock2unlock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                // unlock zone --> lock zone
                if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LOCK)
                            && compareProb(distribution, temp_prediction, threshold_prob_unlock2lock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                // other change of zone
                if (compareProb(distribution, temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            } else if (strategy.equals(PASSIVE_ENTRY_ORIENTED)) {
                // unlock zone --> lock zone
                if (comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_LEFT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_RIGHT)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_BACK)
                        || comparePrediction(modelWrappers.get(0), prediction_old, PREDICTION_FRONT)) {
                    if (comparePrediction(modelWrappers.get(0), temp_prediction, PREDICTION_LOCK)
                            && compareProb(distribution, temp_prediction, threshold_prob_unlock2lock)) {
                        prediction_old = temp_prediction;
                        return;
                    }
                }
                // other change of zone
                if (compareProb(distribution, temp_prediction, threshold_prob)) {
                    prediction_old = temp_prediction;
                    return;
                }
            }
        }
    }

    /***
     * whether in first prediction
     * @return
     */
    private boolean checkOldPrediction() {
        if (prediction_old == -1) {
            prediction_old = most(predictions);
            return false;
        }
        return true;
    }

    /***
     * get corresponding string result of prediction
     * @return
     */
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
        } else if (rssi_modified == null) {
            return "";
        } else if (distribution == null) {
            return "";
        } else if (prediction_old == -1) {
            return "";
        } else if (label == null) {
            return "";
        } else {
            sb.append(String.format(Locale.FRANCE, "%1$s %2$s ", predictionType, getPrediction())).append("\n");
            for (double arssi : rssi_modified) {
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

    public double[] getDistribution() {
        return distribution;
    }

    public void setThresholdROC(Double threshold) {
        this.thresholdROC = threshold;
    }

    /***
     * get all the string results of ML
     * @return
     */
    public String[] getClasses() {
        if (modelWrappers.get(0) != null) {
            return modelWrappers.get(0).getResponseDomainValues();
        } else {
            return null;
        }
    }
}
