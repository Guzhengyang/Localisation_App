package com.valeo.bleranging.model.connectedcar;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.HashMap;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCSevenFlFrLMRRlRr extends ConnectedCar {
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
    private static final int MODE_LEFT_SIDE = 13;
    private static final int MODE_RIGHT_SIDE = 14;
    private static final int MODE_TRUNK_SIDE = 15;
    private static final int MODE_LEFT_BACK_ANGLE = 16;
    private static final int MODE_RIGHT_BACK_ANGLE = 17;

    public CCSevenFlFrLMRRlRr(ConnectionNumber connectionNumber) {
        super(connectionNumber);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        trxMap = new HashMap<>();
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, historicDefaultValuePeriph);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, historicDefaultValuePeriph);
        trxLeft = new Trx(NUMBER_TRX_LEFT, historicDefaultValuePeriph);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, historicDefaultValueCentral);
        trxRight = new Trx(NUMBER_TRX_RIGHT, historicDefaultValuePeriph);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, historicDefaultValuePeriph);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, historicDefaultValuePeriph);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
    }

    @Override
    public boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket) {
        boolean isInStartArea = isInStartArea(TrxUtils.getCurrentStartThreshold(SdkPreferencesHelper.getInstance().getStartThreshold(), smartphoneIsInPocket));
        return (isInStartArea
                && (!newLockStatus)
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold()))
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax())
                || isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMax()))
                && (isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin())
                && isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorThresholdMLorMRMin()))
        );
    }

    @Override
    public boolean isInStartArea(int threshold) {
        boolean trxL = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxM = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxR = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxB = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxFL = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getStartMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR);
    }

    @Override
    public int unlockStrategy(boolean smartphoneIsInPocket) {
        boolean isInUnlockArea = isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getUnlockThreshold(), smartphoneIsInPocket));
        boolean isNextToDoorLRMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLRMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold());
        boolean isNextToDoorLBMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorLBMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isNextToDoorRBMax = isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMax());
        boolean isNextToDoorRBMin = isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, SdkPreferencesHelper.getInstance().getNextToBackDoorRatioThresholdMin());
        boolean isApproaching = TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(), smartphoneIsInPocket));
        if (isInUnlockArea && (isNextToDoorLRMax && isNextToDoorLBMax) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_LEFT;
        } else if (isInUnlockArea && (isNextToDoorLRMin && isNextToDoorRBMax) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_RIGHT;
        } else if (isInUnlockArea && (isNextToDoorLBMin && isNextToDoorRBMin) && isApproaching) {
            return ConnectedCar.NUMBER_TRX_BACK;
        }
        return 0;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxL  = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxM  = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxR  = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxB  = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFL = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getUnlockMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR);
    }

    @Override
    public boolean lockStrategy(boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getLockThreshold(), smartphoneIsInPocket));
        boolean isLeaving = TrxUtils.getAverageLSDeltaGreaterThanThreshold(this, TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(), smartphoneIsInPocket));
        return (isInLockArea && isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxL  = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxM  = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxR  = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxB  = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFL = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRL = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFR = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRR = isTrxLowerThanThreshold(ConnectedCar.NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getLockMode(), trxL, trxM, trxR, trxB, trxFL, trxRL, trxFR, trxRR)
                || (trxM && (isRatioNextToDoorLowerThanThreshold(Antenna.AVERAGE_LOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())
                || isRatioNextToDoorGreaterThanThreshold(Antenna.AVERAGE_LOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNextToDoorRatioThreshold())));

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
                return (trxL && trxR) || (trxL && trxB)  || (trxR && trxB);
            case MODE_ALL_OF_THREE_WITHOUT_MIDDLE:
                return trxL  && trxR && trxB;
            case MODE_ONE_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL || trxR);
            case MODE_ALL_OF_TWO_WITHOUT_BACK_AND_MIDDLE:
                return (trxL && trxR) ;
            case MODE_ONLY_MIDDLE:
                return trxM;
            case MODE_LEFT_SIDE:
                return trxL && trxFL && trxRL;
            case MODE_RIGHT_SIDE:
                return trxR && trxFR && trxRR;
            case MODE_TRUNK_SIDE:
                return trxB && trxRL && trxRR;
            case MODE_LEFT_BACK_ANGLE:
                return trxL && trxRL && trxB;
            case MODE_RIGHT_BACK_ANGLE:
                return trxR && trxRR && trxB;
            default:
                return false;
        }
    }
}
