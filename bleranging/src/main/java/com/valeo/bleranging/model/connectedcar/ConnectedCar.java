package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Ranging;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by l-avaratha on 05/09/2016.
 */
public abstract class ConnectedCar {
    public static final int NUMBER_TRX_FRONT_LEFT = 1;
    public static final int NUMBER_TRX_FRONT_RIGHT = 2;
    public static final int NUMBER_TRX_LEFT = 3;
    public static final int NUMBER_TRX_MIDDLE = 4;
    public static final int NUMBER_TRX_RIGHT = 5;
    public static final int NUMBER_TRX_REAR_LEFT = 6;
    public static final int NUMBER_TRX_BACK = 7;
    public static final int NUMBER_TRX_REAR_RIGHT = 8;
    public static final String TRX_FRONT_LEFT_NAME = "FLeft";
    public static final String TRX_FRONT_RIGHT_NAME = "FRight";
    public static final String TRX_LEFT_NAME = "Left";
    public static final String TRX_MIDDLE_NAME = "Mid";
    public static final String TRX_RIGHT_NAME = "Right";
    public static final String TRX_REAR_LEFT_NAME = "RLeft";
    public static final String TRX_BACK_NAME = "Back";
    public static final String TRX_REAR_RIGHT_NAME = "RRight";
    public final static int RSSI_LOCK_DEFAULT_VALUE = -120;
    public final static int RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE = -50;
    public final static int RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE = -30;
    public final static int RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE = -70;
    public final static int RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE = -80;
    protected final static int welcomeThreshold = SdkPreferencesHelper.getInstance().getWelcomeThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int lockThreshold = SdkPreferencesHelper.getInstance().getLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int unlockThreshold = SdkPreferencesHelper.getInstance().getUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int startThreshold = SdkPreferencesHelper.getInstance().getStartThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static float linAccThreshold = SdkPreferencesHelper.getInstance().getCorrectionLinAcc();
    protected final static int averageDeltaLockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int averageDeltaUnlockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int lockMode = SdkPreferencesHelper.getInstance().getLockMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int unlockMode = SdkPreferencesHelper.getInstance().getUnlockMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int startMode = SdkPreferencesHelper.getInstance().getStartMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int nearDoorRatioThreshold = SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int nearBackDoorRatioThresholdMin = SdkPreferencesHelper.getInstance().getNearBackDoorRatioThresholdMin(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int nearBackDoorRatioThresholdMax = SdkPreferencesHelper.getInstance().getNearBackDoorRatioThresholdMax(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int nearDoorThresholdMLorMRMin = SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMin(SdkPreferencesHelper.getInstance().getConnectedCarType());
    protected final static int nearDoorThresholdMLorMRMax = SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMax(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final String trxAddressLeft = SdkPreferencesHelper.getInstance().getTrxAddressLeft();
    private final String trxAddressMiddle = SdkPreferencesHelper.getInstance().getTrxAddressMiddle();
    private final String trxAddressRight = SdkPreferencesHelper.getInstance().getTrxAddressRight();
    private final String trxAddressBack = SdkPreferencesHelper.getInstance().getTrxAddressBack();
    private final String trxAddressFrontLeft = SdkPreferencesHelper.getInstance().getTrxAddressFrontLeft();
    private final String trxAddressFrontRight = SdkPreferencesHelper.getInstance().getTrxAddressFrontRight();
    private final String trxAddressRearLeft = SdkPreferencesHelper.getInstance().getTrxAddressRearLeft();
    private final String trxAddressRearRight = SdkPreferencesHelper.getInstance().getTrxAddressRearRight();
    protected Trx trxLeft;
    protected Trx trxMiddle;
    protected Trx trxRight;
    protected Trx trxBack;
    protected Trx trxFrontLeft;
    protected Trx trxRearLeft;
    protected Trx trxFrontRight;
    protected Trx trxRearRight;
    protected LinkedHashMap<Integer, Trx> trxLinkedHMap;
    protected ConnectionNumber connectionNumber;
    private boolean initialized = false;

    public ConnectedCar(ConnectionNumber connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     * @param newLockStatus the lock status that determines which values to set
     */
    public void initializeTrx(boolean newLockStatus) {
        trxLinkedHMap = new LinkedHashMap<>();
        if (newLockStatus) {
            initializeTrx(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
        } else {
            initializeTrx(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
        }
        initialized = true;
    }

    /**
     * Reset rssi historic with new value
     * @param newLockStatus         the lock status
     * @param isUnlockStrategyValid the unlock strategy result
     */
    public void resetWithHysteresis(boolean newLockStatus, int isUnlockStrategyValid) {
        if (!newLockStatus) { // just perform an unlock
            switch (isUnlockStrategyValid) {
                case ConnectedCar.NUMBER_TRX_LEFT:
                    resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                    break;
                case ConnectedCar.NUMBER_TRX_RIGHT:
                    resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE);
                    break;
                case ConnectedCar.NUMBER_TRX_BACK:
                    resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE);
                    break;
                default:
                    resetTrxWithHysteresis(RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE,
                            RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE);
                    break;
            }
        } else { // just perform a lock
            resetTrxWithHysteresis(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE,
                    RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE,
                    RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
        }
    }

    /**
     * Reset Trxs with these values
     *
     * @param valueMiddle     the middle trx new value
     * @param valueLeft       the left trx new value
     * @param valueRight      the right trx new value
     * @param valueBack       the back trx new value
     * @param valueFrontLeft  the front left trx new value
     * @param valueFrontRight the front right trx new value
     * @param valueRearLeft   the rear left trx new value
     * @param valueRearRight  the rear right trx new value
     */
    private void resetTrxWithHysteresis(int valueMiddle, int valueLeft, int valueRight,
                                        int valueBack, int valueFrontLeft, int valueFrontRight,
                                        int valueRearLeft, int valueRearRight) {
        if(trxLeft != null) {
            trxLeft.resetWithHysteresis(valueLeft);
        }
        if(trxFrontLeft != null) {
            trxFrontLeft.resetWithHysteresis(valueFrontLeft);
        }
        if(trxRearLeft != null) {
            trxRearLeft.resetWithHysteresis(valueRearLeft);}
        if(trxMiddle != null) {
            trxMiddle.resetWithHysteresis(valueMiddle);
        }
        if(trxRight != null) {
            trxRight.resetWithHysteresis(valueRight);
        }
        if(trxFrontRight != null) {
            trxFrontRight.resetWithHysteresis(valueFrontRight);
        }
        if(trxRearRight != null) {
            trxRearRight.resetWithHysteresis(valueRearRight);
        }
        if(trxBack != null) {
            trxBack.resetWithHysteresis(valueBack);
        }
    }

    /**
     * Save an incoming rssi
     * @param trxNumber                the trx that sent the signal
     * @param antennaId                the trx antenna id that sent the signal
     * @param rssi                     the rssi value to save
     * @param bleChannel               the ble channel used to sent
     * @param smartphoneIsLaidDownLAcc the boolean that determines if the smartphone is moving or not
     */
    public void saveRssi(int trxNumber, int antennaId, int rssi, Antenna.BLEChannel bleChannel, boolean smartphoneIsLaidDownLAcc) {
        Trx tmpTrx = trxLinkedHMap.get(trxNumber);
        if(tmpTrx != null) {
            tmpTrx.saveRssi(antennaId, rssi, bleChannel, smartphoneIsLaidDownLAcc);
            trxLinkedHMap.put(trxNumber, tmpTrx);
        }
    }

    public int getRssiAverage(int trxNumber, int antennaId, int averageMode) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getAntennaRssiAverage(antennaId, averageMode);
        } else {
            return 0;
        }
    }

    public int getCurrentOriginalRssi(int trxNumber, int antennaId) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getCurrentOriginalRssi(antennaId);
        } else {
            return 0;
        }
    }

    /**
     * Check all trx antenna to see if they are active
     */
    public void compareCheckerAndSetAntennaActive() {
        for (Trx trx : trxLinkedHMap.values()) {
            trx.compareCheckerAndSetAntennaActive();
        }
    }

    /**
     * Calculate all the trx average
     * @param averageMode the average mode
     * @return the average of all active trx or 0 if there is none
     */
    public int getAllTrxAverage(int averageMode) {
        int totalAverage = 0;
        int numberOfAntenna = 0;
        for (Trx trx : trxLinkedHMap.values()) {
            if (trx.isActive()) {
                totalAverage += (trx.getTrxRssiAverage(averageMode));
                numberOfAntenna++;
            }
        }
        if (numberOfAntenna == 0) {
            // If all trx are down restart the app
//            Intent i = mContext.getPackageManager()
//                    .getLaunchIntentForPackage(mContext.getPackageName() );
//            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
//            int mPendingIntentId = 123456;
//            PendingIntent mPendingIntent = PendingIntent.getActivity(mContext, mPendingIntentId, i, PendingIntent.FLAG_CANCEL_CURRENT);
//            AlarmManager mgr = (AlarmManager)mContext.getSystemService(Context.ALARM_SERVICE);
//            mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
//            System.exit(0);
            return 0;
        }
        totalAverage /= numberOfAntenna;
        return totalAverage;
    }

    public int getRatioNearDoor(int mode, int trx1, int trx2) {
        if (trxLinkedHMap.get(trx1) != null && trxLinkedHMap.get(trx2) != null) {
            return TrxUtils.getRatioNearDoor(mode, trxLinkedHMap.get(trx1), trxLinkedHMap.get(trx2));
        } else {
            return 0;
        }
    }

    public boolean isRatioNearDoorGreaterThanThreshold(int mode, int trx1, int trx2, int threshold) {
        if (trxLinkedHMap.get(trx1) != null && trxLinkedHMap.get(trx2) != null) {
            return TrxUtils.getRatioNearDoorGreaterThanThreshold(mode, trxLinkedHMap.get(trx1), trxLinkedHMap.get(trx2), threshold);
        } else {
            return false;
        }
    }

    public boolean isRatioNearDoorLowerThanThreshold(int mode, int trx1, int trx2, int threshold) {
        if (trxLinkedHMap.get(trx1) != null && trxLinkedHMap.get(trx2) != null) {
            return TrxUtils.getRatioNearDoorLowerThanThreshold(mode, trxLinkedHMap.get(trx1), trxLinkedHMap.get(trx2), threshold);
        } else {
            return false;
        }
    }

    public boolean isTrxGreaterThanThreshold(int trxNumber, int antennaMode, int averageMode, int threshold) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).trxGreaterThanThreshold(antennaMode, averageMode, threshold);
        } else {
            return false;
        }
    }

    public boolean isTrxLowerThanThreshold(int trxNumber, int antennaMode, int averageMode, int threshold) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).trxLowerThanThreshold(antennaMode, averageMode, threshold);
        } else {
            return false;
        }
    }

    public boolean isActive(int trxNumber) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).isActive();
        } else {
            return false;
        }
    }

    public int getOffsetBleChannel38(int trxNumber, int antennaId) {
        if (trxLinkedHMap.get(trxNumber) != null) {
            return trxLinkedHMap.get(trxNumber).getOffset38(antennaId);
        } else {
            return 0;
        }
    }

    /**
     * Condition to enable Start action
     * @param newLockStatus        the car lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket);

    /**
     * Check if we are in start area
     * @return true if we are in start area, false otherwise
     */
    public abstract boolean isInStartArea(int threshold);

    /**
     * Condition to enable unlock action
     *
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract int unlockStrategy(boolean smartphoneIsInPocket);

    /**
     * Check if we are in unlock area
     * @return true if we are in unlock area, false otherwise
     */
    public abstract boolean isInUnlockArea(int threshold);

    /**
     * Condition to enable lock action
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean lockStrategy(boolean smartphoneIsInPocket);

    /**
     * Check if we are in lock area
     * @return true if we are in lock area, false otherwise
     */
    public abstract boolean isInLockArea(int threshold);

    /**
     * Condition to enable welcome action
     * @param totalAverage         the total average of all antenna rssi
     * @param newlockStatus        the lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket);

    /**
     * Select a mode of validity and check it
     * @param mode  the mode of validity to check
     * @param trxL  the left trx status
     * @param trxM  the middle trx status
     * @param trxR  the right trx status
     * @param trxB  the back trx status
     * @param trxFL the front left trx status
     * @param trxRL the rear left trx status
     * @param trxFR the front right trx status
     * @param trxRR the rear right trx status
     * @return true if the trx check the condition of validity of the select mode
     */
    public abstract boolean numberOfTrxValid(int mode, boolean trxL, boolean trxM, boolean trxR, boolean trxB,
                                             boolean trxFL, boolean trxRL, boolean trxFR, boolean trxRR);

    /**
     * Create a string of header debug
     * @param spannableStringBuilder the spannable string builder to fill
     * @param bleChannel the ble channel used
     * @return the spannable string builder filled with the header
     */
    public SpannableStringBuilder createHeaderDebugData(
            SpannableStringBuilder spannableStringBuilder, Antenna.BLEChannel bleChannel) {
        spannableStringBuilder.append("Scanning on channel: ").append(bleChannel.toString()).append("\n");
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        for (Trx trx : trxLinkedHMap.values()) {
            spannableStringBuilder
                    .append(TextUtils.colorText(isActive(trx.getTrxNumber()), trx.getTrxName(), Color.WHITE, Color.DKGRAY))
                    .append("    ");
        }
        return spannableStringBuilder;
    }

    /**
     * Create a string of footer debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @return the spannable string builder filled with the first footer
     */
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        spannableStringBuilder.append("\n"); // return to line after tryStrategies print if success
        StringBuilder dataStringBuilder = new StringBuilder();
        for (Trx trx : trxLinkedHMap.values()) {
            dataStringBuilder
                    .append(getRssiAverage(trx.getTrxNumber(), Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT))
                    .append("    ");
        }
        dataStringBuilder.append('\n');
        for (Trx trx : trxLinkedHMap.values()) {
            dataStringBuilder
                    .append(getCurrentOriginalRssi(trx.getTrxNumber(), Trx.ANTENNA_ID_1))
                    .append("    ");
        }
        dataStringBuilder.append('\n');
        for (Trx trx : trxLinkedHMap.values()) {
            dataStringBuilder
                    .append(getRssiAverage(trx.getTrxNumber(), Trx.ANTENNA_ID_0, Antenna.AVERAGE_DEFAULT))
                    .append("    ");
        }
        dataStringBuilder.append('\n');
        dataStringBuilder.append("                               ")
                .append("Total :").append(" ").append(getAllTrxAverage(Antenna.AVERAGE_DEFAULT)).append("\n");
        spannableStringBuilder.append(dataStringBuilder.toString());
        return spannableStringBuilder;
    }

    /**
     * Get the string from the second footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param smartphoneIsInPocket     a boolean that determine if the smartphone is in the user pocket or not.
     * @param smartphoneIsLaidDownLAcc a boolean that determine if the smartphone is moving
     * @param totalAverage             the total average of all trx
     * @param rearmLock                a boolean corresponding to the rearm for lock purpose
     * @param rearmUnlock              a boolean corresponding to the rear for unlock purpose
     * @return the spannable string builder filled with the second footer
     */
    public abstract SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder,
                                                                       boolean smartphoneIsInPocket, boolean smartphoneIsLaidDownLAcc,
                                                                       int totalAverage, boolean rearmLock, boolean rearmUnlock);

    /**
     * Get the string from the third footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param bytesToSend              the bytes to send
     * @param bytesReceived            the bytes received
     * @param deltaLinAcc              the delta of linear acceleration
     * @param smartphoneIsLaidDownLAcc the boolean that determine if the smartphone is moving or not
     * @param mBluetoothManager        the bluetooth manager
     * @return the string builder filled with the third footer data
     */
    public SpannableStringBuilder createThirdFooterDebugData(SpannableStringBuilder spannableStringBuilder,
                                                             byte[] bytesToSend, byte[] bytesReceived, double deltaLinAcc,
                                                             boolean smartphoneIsLaidDownLAcc, BluetoothManagement mBluetoothManager) {
        if (mBluetoothManager.isFullyConnected()) {
            spannableStringBuilder.append("Connected").append("\n")
                    .append("       Send:       ").append(TextUtils.printBleBytes((bytesToSend))).append("\n")
                    .append("       Receive: ").append(TextUtils.printBleBytes(bytesReceived)).append("\n");
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        StringBuilder lAccStringBuilder = new StringBuilder().append("Linear Acceleration < (")
                .append(linAccThreshold)
                .append("): ").append(String.format(Locale.FRANCE, "%1$.4f", deltaLinAcc)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                smartphoneIsLaidDownLAcc,
                lAccStringBuilder.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append("offset channel 38 :\n");
        StringBuilder offset38StringBuilder = new StringBuilder();
        for (Trx trx : trxLinkedHMap.values()) {
            offset38StringBuilder
                    .append(getOffsetBleChannel38(trx.getTrxNumber(), Trx.ANTENNA_ID_1))
                    .append("    ");
        }
        offset38StringBuilder.append('\n');
        spannableStringBuilder.append(offset38StringBuilder.toString());
        spannableStringBuilder.append("-------------------------------------------------------------------------");
        return spannableStringBuilder;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param historicDefaultValuePeriph  the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    public abstract void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral);


    /**
     * Color each antenna average with color if comparaisonSign (> or <) threshold, DK_GRAY otherwise
     *
     * @param mode            the average mode to calculate
     * @param color           the color to use if the conditions is checked
     * @param threshold       the threshold to compare with
     * @param comparaisonSign the comparaison sign
     * @return a colored spannablestringbuilder with all the trx's average
     */
    public SpannableStringBuilder printModedAverage(int mode, int color, int threshold, String comparaisonSign, boolean smartphoneIsLaidDownLAcc, ConnectedCar connectedCar) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(String.valueOf(TextUtils.getNbElement(mode, smartphoneIsLaidDownLAcc)) + "     ");
        for (Trx trx : trxLinkedHMap.values()) {
            ssb.append(TextUtils.colorAntennaAverage(getRssiAverage(trx.getTrxNumber(), Trx.ANTENNA_ID_1, mode), color, threshold, comparaisonSign));
        }
        ssb.append("\n");
        return ssb;
    }

    public Ranging prepareRanging(Context context, boolean smartphoneIsInPocket) {
        Ranging ranging = new Ranging(context);
        ranging.setLeft(getCurrentOriginalRssi(NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1));
        ranging.setMiddle(getCurrentOriginalRssi(NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1));
        ranging.setRight(getCurrentOriginalRssi(NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1));
        ranging.setBack(getCurrentOriginalRssi(NUMBER_TRX_BACK, Trx.ANTENNA_ID_1));
        if (smartphoneIsInPocket) {
            ranging.setPocket(1);
        } else {
            ranging.setPocket(0);
        }
        return ranging;
    }

    public int getTrxNumber(String address) {
        if (address.equals(trxAddressLeft)) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (address.equals(trxAddressMiddle)) {
            return ConnectedCar.NUMBER_TRX_MIDDLE;
        } else if (address.equals(trxAddressRight)) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (address.equals(trxAddressBack)) {
            return ConnectedCar.NUMBER_TRX_BACK;
        } else if (address.equals(trxAddressFrontLeft)) {
            return ConnectedCar.NUMBER_TRX_FRONT_LEFT;
        } else if (address.equals(trxAddressFrontRight)) {
            return ConnectedCar.NUMBER_TRX_FRONT_RIGHT;
        } else if (address.equals(trxAddressRearLeft)) {
            return ConnectedCar.NUMBER_TRX_REAR_LEFT;
        } else if (address.equals(trxAddressRearRight)) {
            return ConnectedCar.NUMBER_TRX_REAR_RIGHT;
        } else {
            return -1;
        }
    }

    public boolean isInitialized() {
        return initialized;
    }

    protected enum ConnectionNumber {
        THREE_CONNECTION, FOUR_CONNECTION, FIVE_CONNECTION, SIX_CONNECTION, SEVEN_CONNECTION
    }
}
