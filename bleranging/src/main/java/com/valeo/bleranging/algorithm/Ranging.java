package com.valeo.bleranging.algorithm;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import com.valeo.bleranging.R;
import com.valeo.bleranging.bluetooth.InblueProtocolManager;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.concurrent.atomic.AtomicBoolean;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_NEAR;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_WELCOME;
import static com.valeo.bleranging.model.Antenna.AVERAGE_WELCOME;

/**
 * Created by zgu4 on 02/12/2016
 */
class Ranging implements SensorEventListener {
    private static final double THRESHOLD_DIST_AWAY_SIMPLE = 0.15;
    private static final int N_VOTE_SIMPLE = 5;
    private static final double THRESHOLD_PROB_STANDARD = 0.8;
    private static final int N_VOTE_STANDARD = 3;
    private static final double THRESHOLD_PROB_EAR = 0.8;
    private static final int N_VOTE_EAR = 3;
    private static final double THRESHOLD_PROB_NEAR_FAR = 0.8;
    private static final int N_VOTE_NEAR_FAR = 3;
    private static final double THRESHOLD_DIST_AWAY_STANDARD = 0.25;
    private static final double THRESHOLD_DIST_AWAY_EAR = 0.4;
    private static final double THRESHOLD_DIST_AWAY_NEAR_FAR = 0.4;
    private static final String SIMPLE_LOC = "Simple Localisation:";
    private static final String STANDARD_LOC = "Standard Localisation:\n";
    private static final String EAR_HELD_LOC = "Ear held Localisation:\n";
    private static final String NEAR_FAR_LOC = "Near Far Localisation:\n";
    private final AtomicBoolean rearmWelcome = new AtomicBoolean(true);
    private Prediction simplePrediction;
    private Prediction standardPrediction;
    private Prediction earPrediction;
    private Prediction nearFarPrediction;
    private String lastModelUsed = STANDARD_LOC;
    private boolean comValid = false;
    private int OFFSET_EAR = 0;
    private int OFFSET_NEAR_FAR = 0;
    private boolean smartphoneIsInPocket = false;

    Ranging(Context context, double[] rssi) {
        this.simplePrediction = new Prediction(context, R.raw.classes_simple,
                R.raw.rf_simple, R.raw.sample_simple);
        this.standardPrediction = new Prediction(context, R.raw.classes_standard,
                R.raw.rf_standard, R.raw.sample_standard);
        this.earPrediction = new Prediction(context, R.raw.classes_ear,
                R.raw.rf_ear, R.raw.sample_ear);
        this.nearFarPrediction = new Prediction(context, R.raw.classes_near_far,
                R.raw.rf_near_far, R.raw.sample_near_far);
        simplePrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        standardPrediction.init(rssi, SdkPreferencesHelper.getInstance().getOffsetSmartphone());
        earPrediction.init(rssi, OFFSET_EAR); //TODO create other offsets
        nearFarPrediction.init(rssi, OFFSET_NEAR_FAR);
        simplePrediction.predict(N_VOTE_SIMPLE);
        standardPrediction.predict(N_VOTE_STANDARD);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
        SensorManager senSensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        Sensor senProximity = senSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        senSensorManager.registerListener(this, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
    }

    public void setRssi(double[] rssi) {
        for (int i = 0; i < rssi.length; i++) {
            simplePrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_SIMPLE, comValid);
            standardPrediction.setRssi(i, rssi[i], SdkPreferencesHelper.getInstance().getOffsetSmartphone(), THRESHOLD_DIST_AWAY_STANDARD, comValid);
            earPrediction.setRssi(i, rssi[i], OFFSET_EAR, THRESHOLD_DIST_AWAY_EAR, comValid);
            nearFarPrediction.setRssi(i, rssi[i], OFFSET_NEAR_FAR, THRESHOLD_DIST_AWAY_NEAR_FAR, comValid);
        }
        if (comValid) {
            comValid = false;
        }
        simplePrediction.predict(N_VOTE_SIMPLE);
        standardPrediction.predict(N_VOTE_STANDARD);
        earPrediction.predict(N_VOTE_EAR);
        nearFarPrediction.predict(N_VOTE_NEAR_FAR);
    }

    String tryMachineLearningStrategies(InblueProtocolManager mProtocolManager,
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
        simplePrediction.calculatePredictionStandard2();
        standardPrediction.calculatePredictionStandard(THRESHOLD_PROB_STANDARD);
        earPrediction.calculatePredictionEar(THRESHOLD_PROB_EAR);
        nearFarPrediction.calculatePredictionStandard(THRESHOLD_PROB_NEAR_FAR);
    }

    String printDebug() {
        String temp;
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) {
            temp = earPrediction.printDebug(EAR_HELD_LOC);
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            temp = simplePrediction.printDebug(SIMPLE_LOC);
        } else {
            temp = standardPrediction.printDebug(STANDARD_LOC);
        }
        return temp + nearFarPrediction.printDebug(NEAR_FAR_LOC);
    }

    String getPredictionPosition() {
        if (SdkPreferencesHelper.getInstance().getComSimulationEnabled() && smartphoneIsInPocket) { // if smartphone com activated and near ear
            if (lastModelUsed.equals(STANDARD_LOC)) {
                comValid = true;
            }
            lastModelUsed = EAR_HELD_LOC;
            return earPrediction.getPrediction();
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            lastModelUsed = SIMPLE_LOC;
            return simplePrediction.getPrediction();
        } else {
            lastModelUsed = STANDARD_LOC;
            return standardPrediction.getPrediction();
        }
    }

    String getPredictionProximity() {
        return nearFarPrediction.getPrediction();
    }

    void setRearmWelcome(boolean enableWelcome) {
        rearmWelcome.set(enableWelcome);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            //near
            smartphoneIsInPocket = (event.values[0] == 0);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }
}
