package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    private static final double THRESHOLD_PROB = 0.7;
    private static final int N_VOTE = 3;
    private static final double THRESHOLD_PROB_EAR = 0.6;
    private static final int N_VOTE_EAR = 3;
    private static final double THRESHOLD_PROB_NEAR_FAR = 0.6;
    private static final int N_VOTE_NEAR_FAR = 3;
    private static final String POSITION_LOC = "Position Localisation:\n";
    private static final String EAR_HELD_LOC = "Ear held Localisation:\n";
    private static final String NEAR_FAR_LOC = "Near-Far Localisation:\n";
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction nearFarPrediction;
    private double[] rssi;

    Ranging(Context context, double[] rssi) {
        this.standardPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes,
                R.raw.eight_flfrlmrtrlrr_rf, R.raw.eight_flfrlmrtrlrr_sample);
        this.earPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes,
                R.raw.eight_flfrlmrtrlrr_rf, R.raw.eight_flfrlmrtrlrr_sample);
        this.nearFarPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes_near_far,
                R.raw.eight_flfrlmrtrlrr_rf_near_far, R.raw.eight_flfrlmrtrlrr_sample_near_far);
        this.rssi = new double[rssi.length];
        standardPrediction.initTab(rssi.length);
        earPrediction.initTab(rssi.length);
        nearFarPrediction.initTab(rssi.length);
        for (int i = 0; i < rssi.length; i++) {
            this.rssi[i] = rssi[i] - SdkPreferencesHelper.getInstance().getOffsetSmartphone();
            standardPrediction.init(i, this.rssi[i]);
            earPrediction.init(i, this.rssi[i]);
            nearFarPrediction.init(i, this.rssi[i]);
        }
        standardPrediction.predict();
        earPrediction.predict();
        nearFarPrediction.predict();
    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            this.rssi[i] = rssi[i] - SdkPreferencesHelper.getInstance().getOffsetSmartphone();
            standardPrediction.setRssi(i, this.rssi[i]);
            earPrediction.setRssi(i, this.rssi[i]);
            nearFarPrediction.setRssi(i, this.rssi[i]);
        }
        standardPrediction.finalizeSetRssi(N_VOTE);
        earPrediction.finalizeSetRssi(N_VOTE_EAR);
        nearFarPrediction.finalizeSetRssi(N_VOTE_NEAR_FAR);
    }

    void calculatePrediction() {
        standardPrediction.calculatePrediction(THRESHOLD_PROB);
        earPrediction.calculatePrediction(THRESHOLD_PROB_EAR);
        nearFarPrediction.calculatePrediction(THRESHOLD_PROB_NEAR_FAR);
    }

    String printDebug() {
        return standardPrediction.printDebug(POSITION_LOC)
                + earPrediction.printDebug(EAR_HELD_LOC)
                + nearFarPrediction.printDebug(NEAR_FAR_LOC);
    }

    public String getPrediction() {
        return standardPrediction.getPrediction();
    }

    public String getPredictionNearFar() {
        return nearFarPrediction.getPrediction();
    }
}
