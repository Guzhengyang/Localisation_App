package com.valeo.bleranging.managers;

import android.os.Handler;

import com.valeo.bleranging.utils.PSALogs;

import static com.valeo.bleranging.BleRangingHelper.connectedCar;

/**
 * Created by l-avaratha on 13/09/2017
 */

public class MachineLearningManager {
    /**
     * Single helper instance.
     */
    private static MachineLearningManager sSingleInstance = null;
    private final Handler mMainHandler = new Handler();
    final Runnable setRssiForRangingPrediction = new Runnable() {
        @Override
        public void run() {
            if (connectedCar != null) {
                double[] rssi = connectedCar.getMultiTrx().getRssiForRangingPrediction();
                if (rssi != null) {
                    connectedCar.getMultiPrediction().setRssi(rssi);
                } else {
                    PSALogs.d("init2", "setRssiForRangingPrediction is NULL\n");
                }
            }
            mMainHandler.postDelayed(this, 100);
        }
    };
    final Runnable calculateCoordPrediction = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("ml_info", "calculateCoordPrediction");
            if (connectedCar != null) {
                connectedCar.getMultiPrediction().calculatePredictionCoord();
            }
            mMainHandler.postDelayed(this, 100);
        }
    };
    final Runnable calculateZonePrediction = new Runnable() {
        @Override
        public void run() {
            PSALogs.d("ml_info", "calculateZonePrediction");
            if (connectedCar != null) {
                connectedCar.getMultiPrediction().calculatePredictionZone();
            }
            mMainHandler.postDelayed(this, 400);
        }
    };

    /**
     * Private constructor.
     */
    private MachineLearningManager() {
    }

    /**
     * Initialize the helper instance.
     */
    public static void initializeInstance() {
        if (sSingleInstance == null) {
            sSingleInstance = new MachineLearningManager();
        }
    }

    /**
     * @return the single helper instance.
     */
    public static MachineLearningManager getInstance() {
        return sSingleInstance;
    }
}
