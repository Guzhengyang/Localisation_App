package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.os.Handler;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.machinelearningalgo.prediction.Prediction;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;

import java.util.LinkedHashMap;
import java.util.Locale;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_UNKNOWN;

/**
 * Created by l-avaratha on 05/09/2016
 */
public abstract class ConnectedCar {
    public final static int NUMBER_TRX_FRONT_LEFT = 1;
    public final static int NUMBER_TRX_FRONT_RIGHT = 2;
    public final static int NUMBER_TRX_LEFT = 3;
    public final static int NUMBER_TRX_MIDDLE = 4;
    public final static int NUMBER_TRX_RIGHT = 5;
    public final static int NUMBER_TRX_TRUNK = 6;
    public final static int NUMBER_TRX_REAR_LEFT = 7;
    public final static int NUMBER_TRX_BACK = 8;
    public final static int NUMBER_TRX_REAR_RIGHT = 9;
    public final static String THATCHAM_ORIENTED = "thatcham_oriented";
    public final static String PASSIVE_ENTRY_ORIENTED = "passive_entry_oriented";
    final static String SIMPLE_LOC = "Simple Localisation:";
    final static String STANDARD_LOC = "Standard Localisation:";
    final static String EAR_HELD_LOC = "Ear held Localisation:";
    final static String RP_LOC = "RP Localisation:";
    final static String START_LOC = "Start Localisation:";
    final static String FULL_LOC = "Full Localisation:";
    final static String INSIDE_LOC = "Inside Localisation:";
    final static int N_VOTE_VERY_LONG = 10;
    final static int N_VOTE_LONG = 5;
    final static int N_VOTE_SHORT = 3;
    final static double THRESHOLD_PROB_LOCK = 0.7;
    final static double THRESHOLD_PROB_UNLOCK = 0.9;
    final static double THRESHOLD_DIST_AWAY_SLOW = 0.07;
    final static double THRESHOLD_DIST_AWAY_EAR = 0.25;
    final static int START_OFFSET = 2;
    final static String TRX_FRONT_LEFT_NAME = "FLeft";
    final static String TRX_FRONT_RIGHT_NAME = "FRight";
    final static String TRX_LEFT_NAME = "Left";
    final static String TRX_MIDDLE_NAME = "Middle";
    final static String TRX_RIGHT_NAME = "Right";
    final static String TRX_TRUNK_NAME = "Trunk";
    final static String TRX_REAR_LEFT_NAME = "RLeft";
    final static String TRX_BACK_NAME = "Back";
    final static String TRX_REAR_RIGHT_NAME = "RRight";
    private final static int RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE = -60;
    private final static int RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE = -55;
    private final static int RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE = -75;
    protected final Handler mHandlerComValidTimeOut = new Handler();
    final LinkedHashMap<Integer, Trx> trxLinkedHMap;
    private final ConnectionNumber connectionNumber;
    protected Context mContext;
    protected Prediction standardPrediction;
    protected Prediction earPrediction;
    protected Prediction rpPrediction;
    protected Prediction insidePrediction;
    protected double[] rssi;
    protected boolean comValid = false;
    protected String lastModelUsed = STANDARD_LOC;
    Trx trxFrontLeft;
    Trx trxFrontRight;
    Trx trxLeft;
    Trx trxMiddle;
    Trx trxRight;
    Trx trxTrunk;
    Trx trxRearLeft;
    Trx trxBack;
    Trx trxRearRight;

    ConnectedCar(Context context, ConnectionNumber connectionNumber) {
        this.mContext = context;
        this.connectionNumber = connectionNumber;
        this.trxLinkedHMap = new LinkedHashMap<>();
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param newLockStatus the lock status that determines which values to set
     */
    public void initializeTrx(boolean newLockStatus) {
        if (newLockStatus) {
            initializeTrx(RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
        } else {
            initializeTrx(RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
        }
    }

    // COMPARE UTILS

    /**
     * Save the current ble channel
     *
     * @param trxNumber  the trx that sent the signal
     * @param bleChannel the ble channel used to sent
     */
    public void saveBleChannel(int trxNumber, Antenna.BLEChannel bleChannel) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveBleChannel(bleChannel);
        }
    }

    /**
     * Save an incoming rssi
     *
     * @param trxNumber the trx that sent the signal
     * @param rssi      the rssi value to save
     */
    public void saveRssi(int trxNumber, int rssi) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            trxLinkedHMap.get(trxNumber).saveRssi(rssi, true);
        }
    }

    public Antenna.BLEChannel getCurrentBLEChannel(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentBLEChannel();
        } else {
            return Antenna.BLEChannel.UNKNOWN;
        }
    }

    public String getCurrentBLEChannelString(int trxNumber) {
        String result;
        if (trxLinkedHMap.get(trxNumber) != null) {
            switch (trxLinkedHMap.get(trxNumber).getCurrentBLEChannel()) {
                case BLE_CHANNEL_37:
                    result = " 37";
                    break;
                case BLE_CHANNEL_38:
                    result = " 38";
                    break;
                case BLE_CHANNEL_39:
                    result = " 39";
                    break;
                default:
                    result = "   ";
                    break;
            }
        } else {
            result = "   ";
        }
        return result;
    }

    public int getCurrentOriginalRssi(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentOriginalRssi();
        } else {
            return 0;
        }
    }

    public int getCurrentModifiedRssi(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentModifiedRssi();
        } else {
            return 0;
        }
    }

    /**
     * Calculate all the trx average
     *
     * @return the average of all active trx or 0 if there is none
     */
    public int getAllTrxAverage() {
        int totalAverage = 0;
        int numberOfAntenna = 0;
        for (Trx trx : trxLinkedHMap.values()) {
            if (trx.isEnabled()) {
                totalAverage += (trx.getCurrentOriginalRssi());
                numberOfAntenna++;
            }
        }
        if (numberOfAntenna == 0) {
            return 0;
        }
        totalAverage /= numberOfAntenna;
        return totalAverage;
    }

    public boolean isActive(int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    /**
     * Condition to enable welcome action
     *
     * @param totalAverage  the total average of all antenna rssi
     * @param newLockStatus the lock status
     * @return true if the strategy is verified, false otherwise
     */
    public boolean welcomeStrategy(int totalAverage, boolean newLockStatus) {
        return (totalAverage >= -100) && newLockStatus;
    }

    /**
     * Create a string of header debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @param bytesToSend            the bytes to send
     * @param bytesReceived          the bytes received
     * @param isFullyConnected       the boolean that determine if the smartphone is connected or not
     * @return the spannable string builder filled with the header
     */
    public SpannableStringBuilder createHeaderDebugData(
            SpannableStringBuilder spannableStringBuilder, final byte[] bytesToSend, final byte[] bytesReceived, boolean isFullyConnected) {
        if (isFullyConnected) {
            if (bytesToSend != null) {
                spannableStringBuilder.append("       Send:       ").append(TextUtils.printBleBytes((bytesToSend))).append("\n");
            }
            if (bytesReceived != null) {
                spannableStringBuilder.append("       Receive: ").append(TextUtils.printBleBytes(bytesReceived)).append("\n");
            }
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        return spannableStringBuilder;
    }

    /**
     * Create a string of footer debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @return the spannable string builder filled with the first footer
     */
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%7s",
                    TextUtils.colorText(isActive(trx.getTrxNumber()), trx.getTrxName(), Color.WHITE, Color.DKGRAY)));

        }
        spannableStringBuilder.append("\n");

        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%10d",
                    getCurrentOriginalRssi(trx.getTrxNumber())));
        }
        spannableStringBuilder.append('\n');

        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%10s",
                    getCurrentBLEChannelString(trx.getTrxNumber())));
        }

        spannableStringBuilder.append('\n');
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        return spannableStringBuilder;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param historicDefaultValuePeriph  the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    protected abstract void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral);

    /**
     * Create predictions
     */
    public abstract void readPredictionsRawFiles();

    /**
     * Initialize predictions
     */
    public abstract void initPredictions();

    /**
     * Check if predictions has been initialized
     *
     * @return true if prediction were initialized, false otherwise
     */
    public abstract boolean isInitialized();

    /**
     * Get rssi from beacon to make a prediction
     *
     * @return an array with a rssi from each beacon
     */
    public abstract double[] getRssiForRangingPrediction();

    /**
     * Set the rssi into the machine learning algorithm
     * @param rssi the array containing rssi from beacons
     */
    public abstract void setRssi(double[] rssi);

    /**
     * Calculate a prediction using machine learning
     */
    public abstract void calculatePrediction();

    /**
     * Print debug info
     * @param smartphoneIsInPocket true if the smartphone is supposedly in the pocket, false otherwise
     * @return the debug information
     */
    public abstract String printDebug(boolean smartphoneIsInPocket);

    /**
     * Get a prediction of position regarding the car
     * @param smartphoneIsInPocket true if the smartphone is supposedly in the pocket, false otherwise
     * @return the position prediction
     */
    public abstract String getPredictionPosition(boolean smartphoneIsInPocket);

    /**
     * Get a prediction of proximity with the car
     * @return the proximity prediction
     */
    public String getPredictionProximity() {
        if (rpPrediction != null) {
            return rpPrediction.getPrediction();
        }
        return PREDICTION_UNKNOWN;
    }

    /**
     * Check if the array of rssi contains only non null value
     * @param mRssi the array of rssi to check
     * @return null if a value is equal to 0, the entire array otherwise
     */
    protected double[] checkForRssiNonNull(double[] mRssi) {
        for (Double elem : mRssi) {
            if (elem == 0) {
                return null;
            }
        }
        return mRssi;
    }

    public int getTrxNumber(String address) {
        if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft())) {
            return ConnectedCar.NUMBER_TRX_FRONT_LEFT;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressFrontRight())) {
            return ConnectedCar.NUMBER_TRX_FRONT_RIGHT;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressLeft())) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressMiddle())) {
            return ConnectedCar.NUMBER_TRX_MIDDLE;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressRight())) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressTrunk())) {
            return ConnectedCar.NUMBER_TRX_TRUNK;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressRearLeft())) {
            return ConnectedCar.NUMBER_TRX_REAR_LEFT;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressBack())) {
            return ConnectedCar.NUMBER_TRX_BACK;
        } else if (address.equals(SdkPreferencesHelper.getInstance().getTrxAddressRearRight())) {
            return ConnectedCar.NUMBER_TRX_REAR_RIGHT;
        } else {
            return -1;
        }
    }

    public enum ConnectionNumber {
        TWO_CONNECTION, THREE_CONNECTION,
        FOUR_CONNECTION, FIVE_CONNECTION,
        SIX_CONNECTION, SEVEN_CONNECTION,
        EIGHT_CONNECTION
    }
}
