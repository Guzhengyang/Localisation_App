package com.valeo.bleranging.algorithm;

import android.content.Context;

import com.valeo.bleranging.R;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;
import static com.valeo.bleranging.model.Antenna.AVERAGE_WELCOME;

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
    private final AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction nearFarPrediction;
    private int OFFSET_EAR = 10;

    Ranging(Context context, double[] rssi) {
        this.standardPrediction = new Prediction(context, R.raw.classes_standard, R.raw.rf_standard,
                R.raw.sample_standard);
        this.earPrediction = new Prediction(context, R.raw.classes_ear, R.raw.rf_ear,
                R.raw.sample_ear);
        this.nearFarPrediction = new Prediction(context, R.raw.classes_near_far, R.raw.rf_near_far,
                R.raw.sample_near_far);
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

    public String tryMachineLearningStrategies(InblueProtocolManager mProtocolManager,
                                               ConnectedCar connectedCar, boolean newLockStatus) {
        calculatePrediction();
        if (rearmWelcome.get()) {
            boolean isWelcomeStrategyValid = connectedCar.welcomeStrategy(connectedCar
                    .getAllTrxAverage(AVERAGE_WELCOME), newLockStatus);
            if (isWelcomeStrategyValid) {
                rearmWelcome.set(false);
                return PREDICTION_WELCOME;
            }
        }
        if (getPredictionProximity().equalsIgnoreCase(PREDICTION_NEAR)) {
            mProtocolManager.setInRemoteParkingArea(true);
        } else {
            mProtocolManager.setInRemoteParkingArea(false);
        }
        return getPredictionPosition();
    }

    private void calculatePrediction() {
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

    public String getPredictionPosition() {
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled()) {
            return earPrediction.getPrediction();
        } else {
            return standardPrediction.getPrediction();
        }
    }

    public String getPredictionProximity() {
        return nearFarPrediction.getPrediction();
    }

    public void setRearmWelcome(boolean enableWelcome) {
        rearmWelcome.set(enableWelcome);
    }
}
