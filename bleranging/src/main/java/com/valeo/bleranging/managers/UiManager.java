package com.valeo.bleranging.managers;

import android.content.Context;
import android.graphics.PointF;
import android.os.Handler;
import android.os.Looper;
import android.text.SpannedString;
import android.widget.Toast;

import com.valeo.bleranging.bluetooth.protocol.InblueProtocolManager;
import com.valeo.bleranging.listeners.ChessBoardListener;
import com.valeo.bleranging.listeners.DebugListener;
import com.valeo.bleranging.listeners.SpinnerListener;
import com.valeo.bleranging.listeners.TestListener;
import com.valeo.bleranging.model.connectedcar.ConnectedCarFactory;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.PSALogs;

import java.util.List;

import static com.valeo.bleranging.BleRangingHelper.connectedCar;
import static com.valeo.bleranging.persistence.Constants.BASE_2;
import static com.valeo.bleranging.persistence.Constants.BASE_3;
import static com.valeo.bleranging.persistence.Constants.PREDICTIONS;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_THATCHAM;
import static com.valeo.bleranging.persistence.Constants.PREDICTION_WELCOME;
import static com.valeo.bleranging.utils.TextUtils.createFirstFooterDebugData;
import static com.valeo.bleranging.utils.TextUtils.createHeaderDebugData;

/**
 * Created by l-avaratha on 13/09/2017
 */

public class UiManager {
    /**
     * Single helper instance.
     */
    private static UiManager sSingleInstance = null;
    private final ChessBoardListener chessBoardListener;
    private final DebugListener debugListener;
    private final SpinnerListener spinnerListener;
    private final TestListener testListener;
    private final Handler mMainHandler = new Handler(Looper.getMainLooper());
    public final Runnable updateCarLocalizationRunnable = new Runnable() {
        @Override
        public void run() {
            if (connectedCar != null) {
                // update ble trame
                CommandManager.getInstance().tryMachineLearningStrategies(connectedCar);
                // update car localization img
                updateCarLocalization(connectedCar.getMultiPrediction().getPredictionZone(SensorsManager.getInstance().isSmartphoneInPocket()),
                        connectedCar.getMultiPrediction().getPredictionRP(),
                        connectedCar.getMultiPrediction().getPredictionCoord(),
                        connectedCar.getMultiPrediction().getDist2Car());
            }
            mMainHandler.postDelayed(this, 400);
        }
    };
    final Runnable printRunner = new Runnable() {
        @Override
        public void run() {
            BleConnectionManager.getInstance().getLock().readLock().lock();
            final SpannedString spannedString =
                    (SpannedString) android.text.TextUtils.concat(
                            createHeaderDebugData(BleConnectionManager.getInstance().getBytesToSend(),
                                    BleConnectionManager.getInstance().getBytesReceived(),
                                    BleConnectionManager.getInstance().isFullyConnected()),
                            createFirstFooterDebugData(connectedCar),
                            SensorsManager.getInstance().createSensorsDebugData());
            BleConnectionManager.getInstance().getLock().readLock().unlock();
            debugListener.printDebugInfo(spannedString);
            mMainHandler.postDelayed(this, 100);
        }
    };
    private String lastConnectedCarType = "";
    private String lastOpeningOrientation = "";
    private Boolean lastPrintRooftop;
    private Boolean lastMiniPredictionUsed;

    /**
     * Private constructor.
     */
    private UiManager(ChessBoardListener chessBoardListener,
                      DebugListener debugListener,
                      SpinnerListener accuracyListener, TestListener testListener) {
        this.chessBoardListener = chessBoardListener;
        this.debugListener = debugListener;
        this.spinnerListener = accuracyListener;
        this.testListener = testListener;
    }

    /**
     * Initialize the helper instance.
     */
    public static void initializeInstance(ChessBoardListener chessBoardListener,
                                          DebugListener debugListener,
                                          SpinnerListener accuracyListener,
                                          TestListener testListener) {
        if (sSingleInstance == null) {
            sSingleInstance = new UiManager(chessBoardListener, debugListener, accuracyListener,
                    testListener);
        }
    }

    /**
     * @return the single helper instance.
     */
    public static UiManager getInstance() {
        return sSingleInstance;
    }

    /**
     * Update the mini map with our location around the car
     */
    private void updateCarLocalization(String predictionPosition,
                                       String predictionProximity, List<PointF> coords, List<Double> dists) {
        for (String elementPred : PREDICTIONS) {
            debugListener.darkenArea(elementPred);
        }
        //THATCHAM
        if (InblueProtocolManager.getInstance().getPacketOne().isThatcham()) {
            debugListener.lightUpArea(PREDICTION_THATCHAM);
        }
        // WELCOME
        if (CommandManager.getInstance().isInWelcomeArea()) {
            debugListener.lightUpArea(PREDICTION_WELCOME);
        }
        if (predictionPosition != null && !predictionPosition.isEmpty()) {
            debugListener.lightUpArea(predictionPosition);
        }
        // REMOTE PARKING
        if (predictionProximity != null && !predictionProximity.isEmpty()) {
            debugListener.lightUpArea(predictionProximity);
        }
        debugListener.applyNewDrawable();
        chessBoardListener.updateChessboard(coords, dists);
        testListener.changeColor(connectedCar.getMultiPrediction().getPredictionPositionTest());
    }

    /**
     * Initialize the connected car.
     * Call this method in onResume.
     */
    public void initializeConnectedCar(final Context context) {
        if (!lastConnectedCarType.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getConnectedCarType())
                || !lastOpeningOrientation.equalsIgnoreCase(SdkPreferencesHelper.getInstance().getOpeningStrategy())
                || !lastPrintRooftop.equals(SdkPreferencesHelper.getInstance().isPrintRooftopEnabled())
                || !lastMiniPredictionUsed.equals(SdkPreferencesHelper.getInstance().isMiniPredictionUsed())) {
            // if car type has changed,
            if (BleConnectionManager.getInstance().isFullyConnected()) {
                PSALogs.w("NIH", "INITIALIZED_NEW_CAR");
                // if connected, stop connection, and restart it
                PSALogs.i("restartConnection", "disconnect after changing car type");
                BleConnectionManager.getInstance().restartConnection();
            }
        }
        createConnectedCar(context); // then create a new car
        // car type did not changed, but settings did
        enableSecurityWal();
        debugListener.updateCarDrawable(BleConnectionManager.getInstance().getLockStatus());
        chessBoardListener.applyNewDrawable();
        if (mMainHandler != null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    PSALogs.d("init2", "readPredictionsRawFiles\n");
                    if (connectedCar != null) {
                        connectedCar.getMultiPrediction().readPredictionsRawFiles(context);
                    } else {
                        mMainHandler.postDelayed(this, 500);
                    }
                }
            });
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    if (connectedCar == null ||
                            connectedCar.getMultiTrx().getRssiForRangingPrediction() == null) {
                        mMainHandler.postDelayed(this, 500);
                    } else if (connectedCar != null && connectedCar.isInitialized()) {
                        PSALogs.d("init2", "initPredictions\n");
                        connectedCar.initPredictions();
                        RunnerManager.getInstance().startRunners();
                        spinnerListener.updateAccuracySpinner();
                    } else {
                        mMainHandler.postDelayed(this, 500);
                    }
                }
            });
        }
    }

    private void createConnectedCar(final Context context) {
        connectedCar = null;
        lastConnectedCarType = SdkPreferencesHelper.getInstance().getConnectedCarType();
        lastOpeningOrientation = SdkPreferencesHelper.getInstance().getOpeningStrategy();
        lastPrintRooftop = SdkPreferencesHelper.getInstance().isPrintRooftopEnabled();
        lastMiniPredictionUsed = SdkPreferencesHelper.getInstance().isMiniPredictionUsed();
        connectedCar = ConnectedCarFactory.getConnectedCar(context, lastConnectedCarType);
        if (connectedCar == null) {
            mMainHandler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(context, "ConnectedCar is NULL", Toast.LENGTH_SHORT).show();
                    PSALogs.d("init2", "ConnectedCar is NULL\n");
                }
            });
        }
        PSALogs.d("init2", "createConnectedCar\n");
    }

    private void enableSecurityWal() {
        String connectedCarBase = SdkPreferencesHelper.getInstance().getConnectedCarBase();
        InblueProtocolManager.getInstance().getPacketOne().setCarBase(connectedCarBase);
        if (connectedCarBase.equalsIgnoreCase(BASE_2)
                || connectedCarBase.equalsIgnoreCase(BASE_3)) {
            SdkPreferencesHelper.getInstance().setSecurityWALEnabled(true);
        }
    }
}
