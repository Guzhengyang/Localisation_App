package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;
import android.os.Handler;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    public static final String THATCHAM_ORIENTED = "Thatcham oriented";
    public static final String ENTRY_ORIENTED = "Entry oriented";
    private static final int N_VOTE_SIMPLE = 5;
    private static final double THRESHOLD_DIST_AWAY_SIMPLE = 0.07;
    private static final String SIMPLE_LOC = "Simple Localisation:";
    private static final double THRESHOLD_PROB_STANDARD = 0.8;
    private static final int N_VOTE_STANDARD = 3;
    private static final double THRESHOLD_DIST_AWAY_STANDARD = 0.10;
    private static final String STANDARD_LOC = "Standard Localisation:";
    private static final double THRESHOLD_PROB_EAR = 0.8;
    private static final int N_VOTE_EAR = 5;
    private static final double THRESHOLD_DIST_AWAY_EAR = 0.4;
    private static final String EAR_HELD_LOC = "Ear held Localisation:";
    private static final String RP_LOC = "RP Localisation:";
    private final Handler mHandlerComValidTimeOut = new Handler();
    private Prediction simplePrediction;
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction rpPrediction;
    private String lastModelUsed = STANDARD_LOC;
    private boolean comValid = false;

    Ranging(Context context, double[] rssi) {
//        this.simplePrediction = new Prediction(context, R.raw.classes_simple,
//                R.raw.rf_simple, R.raw.sample_simple);
//        simplePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
//        simplePrediction.predict(N_VOTE_SIMPLE);

        if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
            standardPrediction = new Prediction(context, R.raw.classes_standard_thatcham,
                    R.raw.rf_standard_thatcham, R.raw.sample_standard_thatcham);
        } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
            standardPrediction = new Prediction(context, R.raw.classes_standard_entry,
                    R.raw.rf_standard_entry, R.raw.sample_standard_entry);
        }
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.predict(N_VOTE_STANDARD);


//        this.earPrediction = new Prediction(context, R.raw.classes_ear,
//                R.raw.rf_ear, R.raw.sample_ear);
//        earPrediction.init(rssi, 0); //TODO create other offsets
//        earPrediction.predict(N_VOTE_EAR);

        this.rpPrediction = new Prediction(context, R.raw.classes_rp,
                R.raw.rf_rp, R.raw.sample_rp);
        rpPrediction.init(rssi, 0); //TODO create other offsets
        rpPrediction.predict(N_VOTE_STANDARD);

    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
//            simplePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SIMPLE, comValid);

            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD, comValid);
//            earPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid);
            rpPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid);
        }

//        simplePrediction.predict(N_VOTE_SIMPLE);
        standardPrediction.predict(N_VOTE_STANDARD);
//        earPrediction.predict(N_VOTE_EAR);
        rpPrediction.predict(N_VOTE_STANDARD);
    }

    void calculatePrediction() {

        if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
            standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD, THATCHAM_ORIENTED);
        } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
            standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD, ENTRY_ORIENTED);
        }

//        standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD);
//        simplePrediction.calculatePredictionSimple();
//        earPrediction.calculatePredictionEar(THRESHOLD_PROB_EAR);
        rpPrediction.calculatePredictionRP(THRESHOLD_PROB_STANDARD);

    }

    String printDebug(boolean smartphoneIsInPocket) {

        //        return simplePrediction.printDebug(SIMPLE_LOC);

        String result;
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
            result = earPrediction.printDebug(EAR_HELD_LOC);
        } else {
            result = standardPrediction.printDebug(STANDARD_LOC);
        }
        return result + rpPrediction.printDebug(RP_LOC);
    }

    public String getPredictionPosition(boolean smartphoneIsInPocket) {

        //        return simplePrediction.getPrediction();

        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) { // if smartphone com activated and near ear
            if (lastModelUsed.equals(STANDARD_LOC)) {
                comValid = true;
                mHandlerComValidTimeOut.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        comValid = false;
                        lastModelUsed = EAR_HELD_LOC;
                    }
                }, 2000);
                return BleRangingHelper.PREDICTION_UNKNOWN;
            }
            lastModelUsed = EAR_HELD_LOC;
            return earPrediction.getPrediction();
        } else {
            lastModelUsed = STANDARD_LOC;
            return standardPrediction.getPrediction();
        }
    }

    public String getPredictionRP() {
        return rpPrediction.getPrediction();
    }
}
