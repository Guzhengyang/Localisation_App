package com.valeo.bleranging.model.connectedcar;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.HashMap;

/**
 * Created by l-avaratha on 05/09/2016.
 */
public abstract class ConnectedCar {
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
    protected Trx trxLeft;
    protected Trx trxMiddle;
    protected Trx trxRight;
    protected Trx trxBack;
    protected Trx trxFrontLeft;
    protected Trx trxRearLeft;
    protected Trx trxFrontRight;
    protected Trx trxRearRight;
    protected HashMap<Integer, Trx> trxMap;
    protected ConnectionNumber connectionNumber;

    public ConnectedCar(ConnectionNumber connectionNumber) {
        this.connectionNumber = connectionNumber;
    }

    /**
     * Initialize trx and antenna and their rssi historic with default value periph and central
     *
     * @param historicDefaultValuePeriph  the peripheral trx default value
     * @param historicDefaultValueCentral the central trx default value
     */
    public abstract void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral);

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

    /**
     * Check all trx antenna to see if they are active
     */
    public void compareCheckerAndSetAntennaActive() {
        for (Trx trx : trxMap.values()) {
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

    /**
     * Condition to enable Start action
     *
     * @param newLockStatus        the car lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket);

    /**
     * Check if we are in start area
     *
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
     *
     * @return true if we are in unlock area, false otherwise
     */
    public abstract boolean isInUnlockArea(int threshold);

    /**
     * Condition to enable lock action
     *
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean lockStrategy(boolean smartphoneIsInPocket);

    /**
     * Check if we are in lock area
     *
     * @return true if we are in lock area, false otherwise
     */
    public abstract boolean isInLockArea(int threshold);

    /**
     * Condition to enable welcome action
     *
     * @param totalAverage         the total average of all antenna rssi
     * @param newlockStatus        the lock status
     * @param smartphoneIsInPocket the "is in pocket" status
     * @return true if the strategy is verified, false otherwise
     */
    public abstract boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket);

    /**
     * Select a mode of validity and check it
     *
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

    protected enum ConnectionNumber {
        THREE_CONNECTION, FOUR_CONNECTION, FIVE_CONNECTION, SIX_CONNECTION, SEVEN_CONNECTION
    }
}
