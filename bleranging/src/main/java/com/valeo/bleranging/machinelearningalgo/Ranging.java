package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    private static final int N_VOTE_SIMPLE = 5;
    private static final double THRESHOLD_DIST_AWAY_SIMPLE = 0.07;
    private static final String SIMPLE_LOC = "Simple Localisation:";
    private static final double THRESHOLD_PROB_STANDARD = 0.7;
    private static final int N_VOTE_STANDARD = 3;
    private static final double THRESHOLD_DIST_AWAY_STANDARD = 0.10;
    private static final String STANDARD_LOC = "Standard Localisation:";
    private static final double THRESHOLD_PROB_EAR = 0.8;
    private static final int N_VOTE_EAR = 5;
    private static final double THRESHOLD_DIST_AWAY_EAR = 0.4;
    private static final String EAR_HELD_LOC = "Ear held Localisation:";
    private Prediction simplePrediction;
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private int OFFSET_EAR = 0;


    private String lastModelUsed = STANDARD_LOC;
    private boolean comValid = false;


    Ranging(Context context, double[] rssi) {
//        this.simplePrediction = new Prediction(context, R.raw.classes_simple,
//                R.raw.rf_simple, R.raw.sample_simple);
//        simplePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//        simplePrediction.predict(N_VOTE_SIMPLE);

        this.standardPrediction = new Prediction(context, R.raw.classes_standard,
                R.raw.rf_standard, R.raw.sample_standard);
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.predict(N_VOTE_STANDARD);

        this.earPrediction = new Prediction(context, R.raw.classes_ear,
                R.raw.rf_ear, R.raw.sample_ear);
        earPrediction.init(rssi, OFFSET_EAR); //TODO create other offsets
        earPrediction.predict(N_VOTE_EAR);
    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
//            simplePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SIMPLE, comValid);

            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD, comValid);
            earPrediction.setRssi(i, rssi[i], OFFSET_EAR, THRESHOLD_DIST_AWAY_EAR, comValid);
        }
        if (comValid) {
            comValid = false;
        }
//        simplePrediction.predict(N_VOTE_SIMPLE);

        standardPrediction.predict(N_VOTE_STANDARD);
        earPrediction.predict(N_VOTE_EAR);
    }

    void calculatePrediction() {
//        simplePrediction.calculatePredictionSimple();

        standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD);
        earPrediction.calculatePredictionEar(THRESHOLD_PROB_EAR);

    }

    String printDebug(boolean smartphoneIsInPocket) {

        //        return simplePrediction.printDebug(SIMPLE_LOC);

        String result;
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
            result = earPrediction.printDebug(EAR_HELD_LOC);
        } else {
            result = standardPrediction.printDebug(STANDARD_LOC);
        }
        return result;
    }

    public String getPredictionPosition(boolean smartphoneIsInPocket) {

        //        return simplePrediction.getPrediction();

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


}
