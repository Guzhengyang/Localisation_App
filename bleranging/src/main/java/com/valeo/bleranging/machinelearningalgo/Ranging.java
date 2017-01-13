package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    private static final double THRESHOLD_PROB_Standard = 0.7;
    private static final int N_VOTE = 3;
    private static final double THRESHOLD_PROB_EAR = 0.8;
    private static final int N_VOTE_EAR = 3;
    private static final double THRESHOLD_PROB_NEAR_FAR = 0.8;
    private static final int N_VOTE_NEAR_FAR = 3;
    private static final String STANDARD_LOC = "Standard Localisation:\n";
    private static final String EAR_HELD_LOC = "Ear held Localisation:\n";
    private static final String NEAR_FAR_LOC = "Near-Far Localisation:\n";
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction nearFarPrediction;
    private int OFFSET_EAR = 10;

    Ranging(Context context, double[] rssi) {
        this.standardPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes,
                R.raw.eight_flfrlmrtrlrr_rf, R.raw.eight_flfrlmrtrlrr_sample);
        this.earPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes_ear,
                R.raw.eight_flfrlmrtrlrr_rf_ear, R.raw.eight_flfrlmrtrlrr_sample_ear);
        this.nearFarPrediction = new Prediction(context, R.raw.eight_flfrlmrtrlrr_classes_near_far,
                R.raw.eight_flfrlmrtrlrr_rf_near_far, R.raw.eight_flfrlmrtrlrr_sample_near_far);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        earPrediction.init(rssi, OFFSET_EAR); //TODO create other offsets
        nearFarPrediction.init(rssi, 0);
        standardPrediction.predict(N_VOTE);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            earPrediction.setRssi(i, rssi[i], OFFSET_EAR);
            nearFarPrediction.setRssi(i, rssi[i], 0);
        }
        standardPrediction.predict(N_VOTE);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
    }

    void calculatePrediction() {
        standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_Standard);
        earPrediction.calculatePredictionEar(THRESHOLD_PROB_EAR);
        nearFarPrediction.calculatePredictionStandard(THRESHOLD_PROB_NEAR_FAR);
    }

    String printDebug() {
        String temp;
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled()) {
            temp = earPrediction.printDebug(EAR_HELD_LOC);
        } else {
            temp = standardPrediction.printDebug(STANDARD_LOC);
        }
        return temp + nearFarPrediction.printDebug(NEAR_FAR_LOC);
    }

    public String getPrediction() {
        return standardPrediction.getPrediction();
    }

    public String getPredictionEar() {
        return earPrediction.getPrediction();
    }

    public String getPredictionNearFar() {
        return nearFarPrediction.getPrediction();
    }
}
