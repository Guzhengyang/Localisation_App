package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    private static final double THRESHOLD_PROB_STANDARD = 0.8;
    private static final int N_VOTE_STANDARD = 3;
    private static final double THRESHOLD_PROB_EAR = 0.8;
    private static final int N_VOTE_EAR = 3;
    private static final double THRESHOLD_PROB_NEAR_FAR = 0.8;
    private static final int N_VOTE_NEAR_FAR = 3;
    private static final double THRESHOLD_DIST_AWAY_STANDARD = 0.25;
    private static final double THRESHOLD_DIST_AWAY_EAR = 0.4;
    private static final double THRESHOLD_DIST_AWAY_NEAR_FAR = 0.4;
    private static final String STANDARD_LOC = "Standard Localisation:";
    private static final String EAR_HELD_LOC = "Ear held Localisation:";
    private static final String NEAR_FAR_LOC = "Near Far Localisation:";
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction nearFarPrediction;
    private String lastModelUsed = STANDARD_LOC;
    private boolean comValid = false;
    private int OFFSET_EAR = 0;
    private int OFFSET_NEAR_FAR = 0;


    Ranging(Context context, double[] rssi) {
        this.standardPrediction = new Prediction(context, R.raw.classes_standard,
                R.raw.rf_standard, R.raw.sample_standard);
        this.earPrediction = new Prediction(context, R.raw.classes_ear,
                R.raw.rf_ear, R.raw.sample_ear);
        this.nearFarPrediction = new Prediction(context, R.raw.classes_near_far,
                R.raw.rf_near_far, R.raw.sample_near_far);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        earPrediction.init(rssi, OFFSET_EAR); //TODO create other offsets
        nearFarPrediction.init(rssi, OFFSET_NEAR_FAR);
        standardPrediction.predict(N_VOTE_STANDARD);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD, comValid);
            earPrediction.setRssi(i, rssi[i], OFFSET_EAR, THRESHOLD_DIST_AWAY_EAR, comValid);
            nearFarPrediction.setRssi(i, rssi[i], OFFSET_NEAR_FAR, THRESHOLD_DIST_AWAY_NEAR_FAR, comValid);
        }
        if (comValid) {
            comValid = false;
        }
        standardPrediction.predict(N_VOTE_STANDARD);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
    }

    void calculatePrediction() {
        standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD);
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

    public String getPredictionPosition(boolean smartphoneIsInPocket) {
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) { // if smartphone com activated and near ear
            if (lastModelUsed.equals(STANDARD_LOC)) {
                comValid = true;
            }
            lastModelUsed = EAR_HELD_LOC;
            return earPrediction.getPrediction();
        } else {
            lastModelUsed = STANDARD_LOC;
            return standardPrediction.getPrediction();
        }
    }

    public String getPredictionNearFar() {
        return nearFarPrediction.getPrediction();
    }

}
