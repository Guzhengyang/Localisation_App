package com.valeo.bleranging.machinelearningalgo;

import android.content.Context;
import android.os.Handler;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.R;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

/**
 * Created by zgu4 on 02/12/2016.
 */
public class Ranging {
    public static final String THATCHAM_ORIENTED = "Thatcham oriented";
    public static final String ENTRY_ORIENTED = "Entry oriented";
    private static final String SIMPLE_LOC = "Simple Localisation:";
    private static final String STANDARD_LOC = "Standard Localisation:";
    private static final String EAR_HELD_LOC = "Ear held Localisation:";
    private static final String RP_LOC = "RP Localisation:";
    private static final String START_LOC = "Start Localisation:";
    private static final int N_VOTE_LONG = 5;
    private static final int N_VOTE_SHORT = 3;

    private static final double THRESHOLD_PROB = 0.8;
    private static final double THRESHOLD_PROB_LOCK = 0.6;
    private static final double THRESHOLD_PROB_UNLOCK = 0.9;

    private static final double THRESHOLD_DIST_AWAY_SLOW = 0.07;
    private static final double THRESHOLD_DIST_AWAY_STANDARD = 0.10;
    private static final double THRESHOLD_DIST_AWAY_EAR = 0.25;
    private static final int START_OFFSET = 2;
    private final Handler mHandlerComValidTimeOut = new Handler();
    private Prediction simplePrediction;
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction rpPrediction;
    private Prediction startPrediction;
    private String lastModelUsed = STANDARD_LOC;
    private boolean comValid = false;

    Ranging(Context context, double[] rssi) {

        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
                standardPrediction = new Prediction(context, R.raw.classes_standard_thatcham,
                        R.raw.rf_standard_thatcham, R.raw.sample_standard_thatcham);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction = new Prediction(context, R.raw.classes_standard_entry,
                        R.raw.rf_standard_entry, R.raw.sample_standard_entry);
            }
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);

            this.rpPrediction = new Prediction(context, R.raw.classes_rp,
                    R.raw.rf_rp, R.raw.sample_rp);
            rpPrediction.init(rssi, 0); //TODO create other offsets
            rpPrediction.predict(N_VOTE_LONG);

            this.earPrediction = new Prediction(context, R.raw.classes_ear,
                    R.raw.rf_ear, R.raw.sample_ear);
            earPrediction.init(rssi, 0); //TODO create other offsets
            earPrediction.predict(N_VOTE_LONG);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            this.simplePrediction = new Prediction(context, R.raw.classes_simple,
                    R.raw.rf_simple, R.raw.sample_simple);
            simplePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            simplePrediction.predict(N_VOTE_LONG);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {
            this.startPrediction = new Prediction(context, R.raw.classes_start,
                    R.raw.rf_start, R.raw.sample_start);
            startPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET);
            startPrediction.predict(N_VOTE_LONG);

        } else {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
                standardPrediction = new Prediction(context, R.raw.classes_standard_thatcham,
                        R.raw.rf_standard_thatcham, R.raw.sample_standard_thatcham);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction = new Prediction(context, R.raw.classes_standard_entry,
                        R.raw.rf_standard_entry, R.raw.sample_standard_entry);
            }
            standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
            standardPrediction.predict(N_VOTE_SHORT);

            this.rpPrediction = new Prediction(context, R.raw.classes_rp,
                    R.raw.rf_rp, R.raw.sample_rp);
            rpPrediction.init(rssi, 0); //TODO create other offsets
            rpPrediction.predict(N_VOTE_LONG);

            this.earPrediction = new Prediction(context, R.raw.classes_ear,
                    R.raw.rf_ear, R.raw.sample_ear);
            earPrediction.init(rssi, 0); //TODO create other offsets
            earPrediction.predict(N_VOTE_LONG);
        }
    }

    public void setRssi(double[] rssi) {

        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD);
                earPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid);
                rpPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_STANDARD);
            }
            standardPrediction.predict(N_VOTE_SHORT);
            earPrediction.predict(N_VOTE_LONG);
            rpPrediction.predict(N_VOTE_LONG);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            for (int i = 0; i < rssi.length; i++) {
                simplePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SLOW);
            }
            simplePrediction.predict(N_VOTE_LONG);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {
            for (int i = 0; i < rssi.length; i++) {
                startPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone() + START_OFFSET, THRESHOLD_DIST_AWAY_SLOW);
            }
            startPrediction.predict(N_VOTE_LONG);

        } else {
            for (int i = 0; i < rssi.length; i++) {
                standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD);
                earPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_EAR, comValid);
                rpPrediction.setRssi(i, rssi[i], 0, THRESHOLD_DIST_AWAY_STANDARD);
            }
            standardPrediction.predict(N_VOTE_SHORT);
            earPrediction.predict(N_VOTE_LONG);
            rpPrediction.predict(N_VOTE_LONG);
        }

    }

    void calculatePrediction() {
        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, ENTRY_ORIENTED);
            }
            earPrediction.calculatePredictionEar(THRESHOLD_PROB);
            rpPrediction.calculatePredictionRP(THRESHOLD_PROB);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            simplePrediction.calculatePredictionSimple(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, THRESHOLD_PROB_LOCK);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {
            startPrediction.calculatePredictionStart(THRESHOLD_PROB);

        } else {
            if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.THATCHAM_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, THATCHAM_ORIENTED);
            } else if (SdkPreferencesHelper.getInstance().getOpeningOrientation().equalsIgnoreCase(AlgoManager.PASSIVE_ENTRY_ORIENTED)) {
                standardPrediction.calculatePredictionStandard(THRESHOLD_PROB, THRESHOLD_PROB_UNLOCK, ENTRY_ORIENTED);
            }
            earPrediction.calculatePredictionEar(THRESHOLD_PROB);
            rpPrediction.calculatePredictionRP(THRESHOLD_PROB);
        }

    }

    String printDebug(boolean smartphoneIsInPocket) {

        String result;
        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
            if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
                result = earPrediction.printDebug(EAR_HELD_LOC);
            } else {
                result = standardPrediction.printDebug(STANDARD_LOC);
            }
            result += rpPrediction.printDebug(RP_LOC);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {

            result = simplePrediction.printDebug(SIMPLE_LOC);
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {

            result = startPrediction.printDebug(START_LOC);
        } else {
            if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
                result = earPrediction.printDebug(EAR_HELD_LOC);
            } else {
                result = standardPrediction.printDebug(STANDARD_LOC);
            }
            result += rpPrediction.printDebug(RP_LOC);
        }
        return result;
    }

    public String getPredictionPosition(boolean smartphoneIsInPocket) {

        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
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

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            return simplePrediction.getPrediction();

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {
            return startPrediction.getPrediction();

        } else {
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
    }

    public String getPredictionRP() {
        if (rpPrediction != null) {
            return rpPrediction.getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }
}
