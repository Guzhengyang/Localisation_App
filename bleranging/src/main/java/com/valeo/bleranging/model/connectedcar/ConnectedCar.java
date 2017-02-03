package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;

import java.util.LinkedHashMap;
import java.util.Locale;

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
    private final static String trxAddressFrontLeft = SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft();
    private final static String trxAddressFrontRight = SdkPreferencesHelper.getInstance().getTrxAddressFrontRight();
    private final static String trxAddressLeft = SdkPreferencesHelper.getInstance().getTrxAddressLeft();
    private final static String trxAddressMiddle = SdkPreferencesHelper.getInstance().getTrxAddressMiddle();
    private final static String trxAddressRight = SdkPreferencesHelper.getInstance().getTrxAddressRight();
    private final static String trxAddressTrunk = SdkPreferencesHelper.getInstance().getTrxAddressTrunk();
    private final static String trxAddressRearLeft = SdkPreferencesHelper.getInstance().getTrxAddressRearLeft();
    private final static String trxAddressBack = SdkPreferencesHelper.getInstance().getTrxAddressBack();
    private final static String trxAddressRearRight = SdkPreferencesHelper.getInstance().getTrxAddressRearRight();
    final LinkedHashMap<Integer, Trx> trxLinkedHMap;
    private final ConnectionNumber connectionNumber;
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
        Context mContext = context;
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

    // COMPARE UTILS

    public boolean isActive(int trxNumber) {
        return trxLinkedHMap.get(trxNumber) != null && trxLinkedHMap.get(trxNumber).isActive();
    }

    /**
     * Condition to enable welcome action
     *
     * @param totalAverage  the total average of all antenna rssi
     * @param newlockStatus the lock status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean welcomeStrategy(int totalAverage, boolean newlockStatus);

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
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%8s",
                    TextUtils.colorText(isActive(trx.getTrxNumber()), trx.getTrxName(), Color.WHITE, Color.DKGRAY)));

        }
        spannableStringBuilder.append("\n");

        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%11d",
                    getCurrentOriginalRssi(trx.getTrxNumber())));
        }
        spannableStringBuilder.append('\n');

        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder.append(String.format(Locale.FRANCE, "%11s",
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

    public int getTrxNumber(String address) {
        if (address.equals(trxAddressFrontLeft)) {
            return ConnectedCar.NUMBER_TRX_FRONT_LEFT;
        } else if (address.equals(trxAddressFrontRight)) {
            return ConnectedCar.NUMBER_TRX_FRONT_RIGHT;
        } else if (address.equals(trxAddressLeft)) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (address.equals(trxAddressMiddle)) {
            return ConnectedCar.NUMBER_TRX_MIDDLE;
        } else if (address.equals(trxAddressRight)) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (address.equals(trxAddressTrunk)) {
            return ConnectedCar.NUMBER_TRX_TRUNK;
        } else if (address.equals(trxAddressRearLeft)) {
            return ConnectedCar.NUMBER_TRX_REAR_LEFT;
        } else if (address.equals(trxAddressBack)) {
            return ConnectedCar.NUMBER_TRX_BACK;
        } else if (address.equals(trxAddressRearRight)) {
            return ConnectedCar.NUMBER_TRX_REAR_RIGHT;
        } else {
            return -1;
        }
    }

    public double[] getRssiForRangingPrediction() {
        double[] rssi;
        if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_4_B)) {
            rssi = new double[4];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
            rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
            rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_8_A)) {
            rssi = new double[8];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
            rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
            rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
            rssi[4] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_LEFT);
            rssi[5] = getCurrentOriginalRssi(NUMBER_TRX_FRONT_RIGHT);
            rssi[6] = getCurrentOriginalRssi(NUMBER_TRX_REAR_LEFT);
            rssi[7] = getCurrentOriginalRssi(NUMBER_TRX_REAR_RIGHT);
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_3_A)) {
            rssi = new double[3];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
            rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);

        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_A)) {
            rssi = new double[2];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        } else if (SdkPreferencesHelper.getInstance().getConnectedCarType().equalsIgnoreCase(ConnectedCarFactory.TYPE_2_B)) {
            rssi = new double[2];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
        } else {
            rssi = new double[4];
            rssi[0] = getCurrentOriginalRssi(NUMBER_TRX_LEFT);
            rssi[1] = getCurrentOriginalRssi(NUMBER_TRX_MIDDLE);
            rssi[2] = getCurrentOriginalRssi(NUMBER_TRX_RIGHT);
            rssi[3] = getCurrentOriginalRssi(NUMBER_TRX_TRUNK);
        }

        for (Double elem : rssi) {
            if (elem == 0) {
                return null;
            }
        }
        return rssi;
    }

    protected enum ConnectionNumber {
        TWO_CONNECTION, THREE_CONNECTION,
        FOUR_CONNECTION, FIVE_CONNECTION,
        SIX_CONNECTION, SEVEN_CONNECTION,
        EIGHT_CONNECTION
    }
}
