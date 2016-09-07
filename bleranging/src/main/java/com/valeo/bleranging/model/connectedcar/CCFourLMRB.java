package com.valeo.bleranging.model.connectedcar;

import android.util.Log;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.HashMap;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCFourLMRB extends ConnectedCar {
    private static final int MODE_ONE_OF_FOUR = 1;
    private static final int MODE_TWO_OF_FOUR = 2;
    private static final int MODE_ALL_FOUR = 3;
    private static final int MODE_ONE_OF_THREE_WITHOUT_BACK = 4;
    private static final int MODE_TWO_OF_THREE_WITHOUT_BACK = 5;
    private static final int MODE_ALL_OF_THREE_WITHOUT_BACK = 6;
    private static final int MODE_ONE_OF_THREE_WITHOUT_MIDDLE = 7;
    private static final int MODE_TWO_OF_THREE_WITHOUT_MIDDLE = 8;
    private static final int MODE_ALL_OF_THREE_WITHOUT_MIDDLE = 9;
    private static final int MODE_ONE_OF_TWO_WITHOUT_BACK_AND_MIDDLE = 10;
    private static final int MODE_ALL_OF_TWO_WITHOUT_BACK_AND_MIDDLE = 11;
    private static final int MODE_ONLY_MIDDLE = 12;

    public CCFourLMRB(ConnectionNumber connectionNumber) {
        super(connectionNumber);
        Log.d("create car", "four type A");
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxMap = new HashMap<>();
        trxLeft = new Trx(NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, historicDefaultValueCentral);
        trxRight = new Trx(NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxBack = new Trx(NUMBER_TRX_BACK, historicDefaultValuePeriph);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxBack.setEnabled(true);
        trxMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxMap.put(NUMBER_TRX_BACK, trxBack);
    }

    @Override
    public boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket) {
        boolean isInStartArea = isInStartArea(TrxUtils.getCurrentStartThreshold(SdkPreferencesHelper.getInstance().getStartThreshold(), smartphoneIsInPocket));
        return (isInStartArea
                && (!newLockStatus)
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold()))
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax())
                || isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax()))
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin())
                && isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin()))
        );
    }

    @Override
    public boolean isInStartArea(int threshold) {
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxM = isTrxGreaterThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxB = isTrxGreaterThanThreshold(NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getStartMode(), trxL, trxM, trxR, trxB, false, false, false, false);
    }

    @Override
    public int unlockStrategy(boolean smartphoneIsInPocket) {
        boolean isInUnlockArea = isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getUnlockThreshold(), smartphoneIsInPocket));
        boolean isNextToDoorLRMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLRMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLBMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorLBMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isNextToDoorRBMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorRBMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isApproaching = TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(), smartphoneIsInPocket));
        if (isInUnlockArea && (isNextToDoorLRMax && isNextToDoorLBMax) && isApproaching) {
            return NUMBER_TRX_LEFT;
        } else if (isInUnlockArea && (isNextToDoorLRMin && isNextToDoorRBMax) && isApproaching) {
            return NUMBER_TRX_RIGHT;
        } else if (isInUnlockArea && (isNextToDoorLBMin && isNextToDoorRBMin) && isApproaching) {
            return NUMBER_TRX_BACK;
        }
        return 0;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxM = isTrxGreaterThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxB = isTrxGreaterThanThreshold(NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getUnlockMode(), trxL, trxM, trxR, trxB, false, false, false, false);
    }

    @Override
    public boolean lockStrategy(boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getLockThreshold(), smartphoneIsInPocket));
        boolean isLeaving = TrxUtils.getAverageLSDeltaGreaterThanThreshold(this, TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(), smartphoneIsInPocket));
        return (isInLockArea && isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxL = isTrxLowerThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxM = isTrxLowerThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxR = isTrxLowerThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxB = isTrxLowerThanThreshold(NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getLockMode(), trxL, trxM, trxR, trxB, false, false, false, false)
                || (trxM && (isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())));

    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket) {
        int threshold = TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getWelcomeThreshold(), smartphoneIsInPocket);
        return (totalAverage >= threshold) && newlockStatus;
    }

    @Override
    public boolean numberOfTrxValid(int mode, boolean trxL, boolean trxM, boolean trxR, boolean trxB, boolean trxFL, boolean trxRL, boolean trxFR, boolean trxRR) {
        switch (mode) {
            case MODE_ONE_OF_FOUR:
                return (trxL || trxM || trxR || trxB);
            case MODE_TWO_OF_FOUR:
                return (trxL && trxM) || (trxL && trxR) || (trxL && trxB) || (trxM && trxR) || (trxM && trxB) || (trxR && trxB);
            case MODE_ALL_FOUR:
                return trxL && trxM && trxR && trxB;
            case MODE_ONE_OF_THREE_WITHOUT_BACK:
                return (trxL || trxM || trxR);
            case MODE_TWO_OF_THREE_WITHOUT_BACK:
                return (trxL && trxM) || (trxL && trxR) || (trxM && trxR);
            case MODE_ALL_OF_THREE_WITHOUT_BACK:
                return trxL && trxM && trxR;
            case MODE_ONE_OF_THREE_WITHOUT_MIDDLE:
                return (trxL || trxR || trxB);
            case MODE_TWO_OF_THREE_WITHOUT_MIDDLE:
                return (trxL && trxR) || (trxL && trxB) || (trxR && trxB);
            case MODE_ALL_OF_THREE_WITHOUT_MIDDLE:
                return trxL && trxR && trxB;
            case MODE_ONE_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL || trxR);
            case MODE_ALL_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL && trxR);
            case MODE_ONLY_MIDDLE:
                return trxM;
            default:
                return false;
        }
    }
}
