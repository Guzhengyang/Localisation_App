package com.valeo.bleranging.model;

import com.valeo.bleranging.utils.TrxUtils;

import java.util.HashMap;

/**
 * Created by l-avaratha on 05/09/2016.
 */
public class ConnectedCar {
    public static final int NUMBER_TRX_LEFT = 1;
    public static final int NUMBER_TRX_MIDDLE = 2;
    public static final int NUMBER_TRX_RIGHT = 3;
    public static final int NUMBER_TRX_BACK = 4;
    public static final int NUMBER_TRX_REAR_LEFT = 5;
    public static final int NUMBER_TRX_REAR_RIGHT = 6;
    public static final int NUMBER_TRX_FRONT_LEFT = 7;
    public static final int NUMBER_TRX_FRONT_RIGHT = 8;
    public final static int RSSI_LOCK_DEFAULT_VALUE = -120;
    public final static int RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE = -50;
    public final static int RSSI_UNLOCK_PERIPH_NEAR_DEFAULT_VALUE = -30;
    public final static int RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE = -70;
    public final static int RSSI_UNLOCK_PERIPH_FAR_DEFAULT_VALUE = -80;
    private Trx trxLeft;
    private Trx trxMiddle;
    private Trx trxRight;
    private Trx trxBack;
    private Trx trxFrontLeft;
    private Trx trxRearLeft;
    private Trx trxFrontRight;
    private Trx trxRearRight;
    private HashMap<Integer, Trx> trxMap;
    private ConnectionNumber connectionNumber;

    public ConnectedCar(ConnectionNumber connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    public void setConnectionNumber(ConnectionNumber connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param historicDefaultValuePeriph  the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxMap = new HashMap<>();
        trxLeft = new Trx(NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, historicDefaultValueCentral);
        trxMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxRight = new Trx(NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
        trxBack = new Trx(NUMBER_TRX_BACK, historicDefaultValuePeriph);
        trxMap.put(NUMBER_TRX_BACK, trxBack);
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param newLockStatus the lock status that determines which values to set
     */
    public void initializeTrx(boolean newLockStatus) {
        if (newLockStatus) {
            initializeTrx(RSSI_LOCK_DEFAULT_VALUE, RSSI_LOCK_DEFAULT_VALUE);
        } else {
            initializeTrx(RSSI_UNLOCK_PERIPH_MEDIUM_DEFAULT_VALUE, RSSI_UNLOCK_CENTRAL_DEFAULT_VALUE);
        }
    }

    /**
     * Reset rssi historic with new value
     *
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
        trxLeft.resetWithHysteresis(valueLeft);
        trxFrontLeft.resetWithHysteresis(valueFrontLeft);
        trxRearLeft.resetWithHysteresis(valueRearLeft);
        trxMiddle.resetWithHysteresis(valueMiddle);
        trxRight.resetWithHysteresis(valueRight);
        trxFrontRight.resetWithHysteresis(valueFrontRight);
        trxRearRight.resetWithHysteresis(valueRearRight);
        trxBack.resetWithHysteresis(valueBack);
    }

    /**
     * Save an incoming rssi
     *
     * @param trxNumber                the trx that sent the signal
     * @param antennaId                the trx antenna id that sent the signal
     * @param rssi                     the rssi value to save
     * @param bleChannel               the ble channel used to sent
     * @param smartphoneIsLaidDownLAcc the boolean that determines if the smartphone is moving or not
     */
    public void saveRssi(int trxNumber, int antennaId, int rssi, Antenna.BLEChannel bleChannel, boolean smartphoneIsLaidDownLAcc) {
        Trx tmpTrx = trxMap.get(trxNumber);
        tmpTrx.saveRssi(antennaId, rssi, bleChannel, smartphoneIsLaidDownLAcc);
        trxMap.put(trxNumber, tmpTrx);
    }

    public int getRssiAverage(int trxNumber, int antennaId, int averageMode) {
        return trxMap.get(trxNumber).getAntennaRssiAverage(antennaId, averageMode);
    }

    public int getCurrentOriginalRssi(int trxNumber, int antennaId) {
        return trxMap.get(trxNumber).getCurrentOriginalRssi(antennaId);
    }

    public void compareCheckerAndSetAntennaActive() {
        for (Trx trx : trxMap.values()) {
            trx.compareCheckerAndSetAntennaActive();
        }
    }

    /**
     * Calculate all the trx average
     *
     * @param averageMode the average mode
     * @return the average of all active trx or 0 if there is none
     */
    public int getAllTrxAverage(int averageMode) {
        int totalAverage = 0;
        int numberOfAntenna = 0;
        for (Trx trx : trxMap.values()) {
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

    public int getRatioNextToDoor(int mode, int trx1, int trx2) {
        return TrxUtils.getRatioNextToDoor(mode, trxMap.get(trx1), trxMap.get(trx2));
    }

    public boolean isRatioNextToDoorGreaterThanThreshold(int mode, int trx1, int trx2, int threshold) {
        return TrxUtils.getRatioNextToDoorGreaterThanThreshold(mode, trxMap.get(trx1), trxMap.get(trx2), threshold);
    }

    public boolean isRatioNextToDoorLowerThanThreshold(int mode, int trx1, int trx2, int threshold) {
        return TrxUtils.getRatioNextToDoorLowerThanThreshold(mode, trxMap.get(trx1), trxMap.get(trx2), threshold);
    }

    public boolean isTrxGreaterThanThreshold(int trxNumber, int antennaMode, int averageMode, int threshold) {
        return trxMap.get(trxNumber).trxGreaterThanThreshold(antennaMode, averageMode, threshold);
    }

    public boolean isTrxLowerThanThreshold(int trxNumber, int antennaMode, int averageMode, int threshold) {
        return trxMap.get(trxNumber).trxLowerThanThreshold(antennaMode, averageMode, threshold);
    }

    public boolean isActive(int trxNumber) {
        return trxMap.get(trxNumber).isActive();
    }

    public int getOffsetBleChannel38(int trxNumber, int antennaId) {
        return trxMap.get(trxNumber).getOffset38(antennaId);
    }

    public enum ConnectionNumber {
        THREE_CONNECTION, FOUR_CONNECTION, FIVE_CONNECTION, SIX_CONNECTION, SEVEN_CONNECTION
    }
}
