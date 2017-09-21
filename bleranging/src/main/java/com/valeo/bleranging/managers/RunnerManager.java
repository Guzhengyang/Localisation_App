package com.valeo.bleranging.managers;

import android.media.ToneGenerator;
import android.os.Handler;

import com.valeo.bleranging.listeners.BleRangingListener;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import static com.valeo.bleranging.BleRangingHelper.connectedCar;
import static com.valeo.bleranging.utils.SoundUtils.makeNoise;

/**
 * Created by l-avaratha on 20/09/2017
 */

public class RunnerManager {
    /**
     * Single helper instance.
     */
    private static RunnerManager sSingleInstance = null;
    private final BleRangingListener bleRangingListener;
    private final Handler mMainHandler = new Handler();
    private boolean canStartRunner = true;
    private int beepInt = 0;
    private final Runnable beepRunner = new Runnable() {
        @Override
        public void run() {
            long delayedTime = 500;
            if (SdkPreferencesHelper.getInstance().getUserSpeedEnabled()) {
                beepInt = 1;
                makeNoise(SdkPreferencesHelper.getInstance().getmApplicationContext(), mMainHandler, ToneGenerator.TONE_CDMA_LOW_SS, 100);
                // interval time between each beep sound in milliseconds
                delayedTime = Math.round(((SdkPreferencesHelper.getInstance().getOneStepSize() / 100.0f) / (SdkPreferencesHelper.getInstance().getWantedSpeed() / 3.6)) * 1000);
            }
            mMainHandler.postDelayed(this, delayedTime);
        }
    };
    private boolean isLoggable = true;
    private final Runnable logRunner = new Runnable() {
        @Override
        public void run() {
            if (isLoggable) {
                LogFileManager.getInstance().appendRssiLogs(connectedCar,
                        BleConnectionManager.getInstance().getLockStatus(),
                        bleRangingListener.getMeasureCounterByte(), beepInt);
                beepInt = 0;
            }
            mMainHandler.postDelayed(this, 100);
        }
    };

    /**
     * Private constructor.
     *
     * @param bleRangingListener the bleRanging listener
     */
    private RunnerManager(BleRangingListener bleRangingListener) {
        this.bleRangingListener = bleRangingListener;
    }

    /**
     * Initialize the helper instance.
     *
     * @param bleRangingListener the bleRanging listener
     */
    public static void initializeInstance(BleRangingListener bleRangingListener) {
        if (sSingleInstance == null) {
            sSingleInstance = new RunnerManager(bleRangingListener);
        }
    }

    /**
     * @return the single helper instance.
     */
    public static RunnerManager getInstance() {
        return sSingleInstance;
    }

    public synchronized void stopRunners() {
        PSALogs.d("NIH", "stopRunners");
        mMainHandler.removeCallbacks(logRunner);
        mMainHandler.removeCallbacks(beepRunner);
        mMainHandler.removeCallbacks(MachineLearningManager.getInstance().setRssiForRangingPrediction);
        mMainHandler.removeCallbacks(MachineLearningManager.getInstance().calculateZonePrediction);
        mMainHandler.removeCallbacks(MachineLearningManager.getInstance().calculateCoordPrediction);
        mMainHandler.removeCallbacks(UiManager.getInstance().printRunner);
        mMainHandler.removeCallbacks(UiManager.getInstance().updateCarLocalizationRunnable);
        mMainHandler.removeCallbacks(BleConnectionManager.getInstance().sendPacketRunner);
        mMainHandler.removeCallbacks(BleConnectionManager.getInstance().checkNewPacketsRunner);
        mMainHandler.removeCallbacks(null);
        canStartRunner = true;
    }

    synchronized void startRunners() {
        if (canStartRunner) {
            PSALogs.d("NIH", "startRunners");
            mMainHandler.post(logRunner);
            mMainHandler.post(beepRunner);
            mMainHandler.post(MachineLearningManager.getInstance().setRssiForRangingPrediction);
            mMainHandler.post(MachineLearningManager.getInstance().calculateZonePrediction);
            mMainHandler.post(MachineLearningManager.getInstance().calculateCoordPrediction);
            mMainHandler.post(UiManager.getInstance().printRunner);
            mMainHandler.post(UiManager.getInstance().updateCarLocalizationRunnable);
            canStartRunner = false;
        }
    }

    public void setIsLoggable(boolean isLoggable) {
        this.isLoggable = isLoggable;
    }
}
