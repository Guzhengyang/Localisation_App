package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by l-avaratha on 07/09/2016.
 */
public class CCSevenFlFrLMRRlRr extends ConnectedCar {
    private static final String SPACE_ONE = "  ";
    private static final String SPACE_TWO = "   ";

    public CCSevenFlFrLMRRlRr(Context mContext, ConnectionNumber connectionNumber) {
        super(mContext, connectionNumber);
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_REAR_LEFT, trxRearLeft);
        trxLinkedHMap.put(NUMBER_TRX_REAR_RIGHT, trxRearRight);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_FRONT_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_FRONT_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_REAR_RIGHT).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public boolean startStrategy(boolean newLockStatus, boolean smartphoneIsInPocket) {
        boolean isInStartArea = isInStartArea(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket));
        return (isInStartArea
                && (!newLockStatus)
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold)
                || isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold))
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMax)
                || isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMax))
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMin)
                && isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin))
        );
    }

    @Override
    public boolean isInStartArea(int threshold) {
        boolean trxM = isTrxGreaterThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_MIDDLE, trxM);
        return numberOfTrxValid(startMode, result);
    }

//    private boolean isNearDoor(int mode, int trxNumber, int threshold, int threshold2, int threshold3, boolean areEquals) {
//        switch (trxNumber) {
//            case NUMBER_TRX_FRONT_LEFT:
//                return isNearDoor(mode, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_FRONT_RIGHT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_FRONT_RIGHT:
//                return isNearDoor(mode, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_LEFT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_LEFT:
//                return isNearDoor(mode, NUMBER_TRX_LEFT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_RIGHT:
//                return isNearDoor(mode, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_REAR_LEFT:
//                return isNearDoor(mode, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_REAR_RIGHT:
//                return isNearDoor(mode, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, areEquals);
//            case NUMBER_TRX_BACK:
//                boolean isNearBackL = isNearDoor(mode, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_LEFT, threshold, threshold2, threshold3, areEquals);
//                boolean isNearBackR = isNearDoor(mode, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_RIGHT, threshold, threshold2, threshold3, areEquals);
//                return isNearBackL && isNearBackR;
//            default:
//                return false;
//        }
//    }
//
//    // left front side proche de Fl : ((Fl - Fr) > thr && (Fl - L) > thr) && (L - Fr) > thr2 :Fr<L
//    // OR left side  proche de L : ((L - Fl) > thr && (L - Rl) > thr) && (Fl - Rl < thr) :FletRl quasi egal car equidistant donc delta petit
//    // OR rear left side  proche de Rl : ((Rl - L) > thr && (Rl - Rr) > thr) && (L - Rr) > thr2 :Rr<L
//    private boolean isNearDoor(int mode, int nearThisTrx, int sideTrx, int farTrx, int threshold, int threshold2, int threshold3, boolean areEquals) {
//        boolean isNearSideTrx = isRatioNearDoorGreaterThanThreshold(mode, nearThisTrx, sideTrx, threshold);
//        boolean isNearFarTrx = isRatioNearDoorGreaterThanThreshold(mode, nearThisTrx, farTrx, threshold);
//        if (areEquals) {
//            boolean isEquidistantToSideTrxAndFarTrx = isRatioNearDoorLowerThanThreshold(mode, sideTrx, farTrx, threshold2);
//            return isNearSideTrx && isNearFarTrx && isEquidistantToSideTrxAndFarTrx;
//        } else {
//            boolean isNearerToSideTrxThanFarTrx = isRatioNearDoorGreaterThanThreshold(mode, sideTrx, farTrx, threshold3);
//            return isNearSideTrx && isNearFarTrx && isNearerToSideTrxThanFarTrx;
//        }
//    }
//
//    public List<Integer> unlockStrategy2(boolean smartphoneIsInPocket) {
//        boolean isInUnlockArea = isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket));
//        boolean isApproaching = TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(averageDeltaUnlockThreshold, smartphoneIsInPocket));
//        if (isInUnlockArea && isApproaching) {
//            int threshold = SdkPreferencesHelper.getInstance().getNearDoorThreshold(connectedCarType);
//            int threshold2 = SdkPreferencesHelper.getInstance().getEquallyNearThreshold(connectedCarType);
//            int threshold3 = SdkPreferencesHelper.getInstance().getNearerThreshold(connectedCarType);
//            boolean isNearFrontLeft = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_FRONT_LEFT, threshold, threshold2, threshold3, false);
//            boolean isNearSideLeft = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, threshold, threshold2, threshold3, true);
//            boolean isNearRearLeft = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, false);
//            boolean isNearFrontRight = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_FRONT_RIGHT, threshold, threshold2, threshold3, false);
//            boolean isNearSideRight = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, threshold, threshold2, threshold3, true);
//            boolean isNearRearRight = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, false);
//            boolean isNearBack = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_BACK, threshold, threshold2, threshold3, false);
//            List<Integer> result = new ArrayList<>();
//            if (isNearFrontLeft) {
//                result.add(NUMBER_TRX_FRONT_LEFT);
//            }
//            if (isNearSideLeft) {
//                result.add(NUMBER_TRX_LEFT);
//            }
//            if (isNearRearLeft) {
//                result.add(NUMBER_TRX_REAR_LEFT);
//            }
//            if (isNearFrontRight) {
//                result.add(NUMBER_TRX_FRONT_RIGHT);
//            }
//            if (isNearSideRight) {
//                result.add(NUMBER_TRX_RIGHT);
//            }
//            if (isNearRearRight) {
//                result.add(NUMBER_TRX_REAR_RIGHT);
//            }
//            if (isNearBack) {
//                result.add(NUMBER_TRX_BACK);
//            }
//            if (result.size() == 0) {
//                return null;
//            }
//            return result;
//        }
//        return null;
//    }

    private int getRatioMaxMin(int trxNumber1, int trxNumber2, int trxNumber3,
                               int trxNumber4, int trxNumber5, int trxNumber6, int mode) {
        if (trxLinkedHMap != null) {
            int max = Math.max(Math.max(trxLinkedHMap.get(trxNumber1).getTrxRssiAverage(mode),
                    trxLinkedHMap.get(trxNumber2).getTrxRssiAverage(mode)),
                    trxLinkedHMap.get(trxNumber3).getTrxRssiAverage(mode));
            int min = Math.min(Math.min(trxLinkedHMap.get(trxNumber4).getTrxRssiAverage(mode),
                    trxLinkedHMap.get(trxNumber5).getTrxRssiAverage(mode)),
                    trxLinkedHMap.get(trxNumber6).getTrxRssiAverage(mode));
            return max - min;
        }
        return 0;
    }

    private boolean isRatioGreaterThanThreshold(int ratio, int threshold) {
        return ratio > threshold;
    }

    private int getRatioCloseToCar(int trxNumber, int mode) {
        if (trxLinkedHMap != null) {
            return (trxLinkedHMap.get(trxNumber).getTrxRssiAverage(mode) - getMinAverageRssi(trxNumber, mode));
        }
        return 0;
    }

    private int getMinAverageRssi(int trxNumberToIgnore, int mode) {
        int min = 0;
        if (trxLinkedHMap != null) {
            for (Integer trxNumber : trxLinkedHMap.keySet()) {
                if (trxNumber != trxNumberToIgnore) {
                    min = Math.min(min, trxLinkedHMap.get(trxNumber).getTrxRssiAverage(mode));
                }
            }
        }
        return min;
    }

    @Override
    public List<Integer> unlockStrategy(boolean smartphoneIsInPocket) {
//        boolean isInUnlockArea = isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket));
//        boolean isApproaching = TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(averageDeltaUnlockThreshold, smartphoneIsInPocket));
        boolean isInUnlockArea = true;
        boolean isApproaching = true;
        if (isInUnlockArea && isApproaching) {
            int thresholdMaxMin = SdkPreferencesHelper.getInstance().getRatioMaxMinThreshold(connectedCarType);
            boolean maxMinLeft = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            boolean maxMinRearLeft = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            boolean maxMinRearRight = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            boolean maxMinRight = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            boolean maxMinFrontRight = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            boolean maxMinFrontLeft = isRatioGreaterThanThreshold(getRatioMaxMin(NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin);
            int thresholdCloseToCar = SdkPreferencesHelper.getInstance().getRatioCloseToCarThreshold(connectedCarType);
            boolean closeToCarFrontLeft = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            boolean closeToCarLeft = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            boolean closeToCarRearLeft = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            boolean closeToCarRearRight = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            boolean closeToCarRight = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            boolean closeToCarFrontRight = isRatioGreaterThanThreshold(getRatioCloseToCar(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdCloseToCar);
            List<Integer> result = new ArrayList<>();
            if (maxMinFrontLeft || closeToCarFrontLeft) {
                result.add(NUMBER_TRX_FRONT_LEFT);
            }
            if (maxMinLeft || closeToCarLeft) {
                result.add(NUMBER_TRX_LEFT);
            }
            if (maxMinRearLeft || closeToCarRearLeft) {
                result.add(NUMBER_TRX_REAR_LEFT);
            }
            if (maxMinFrontRight || closeToCarFrontRight) {
                result.add(NUMBER_TRX_FRONT_RIGHT);
            }
            if (maxMinRight || closeToCarRight) {
                result.add(NUMBER_TRX_RIGHT);
            }
            if (maxMinRearRight || closeToCarRearRight) {
                result.add(NUMBER_TRX_REAR_RIGHT);
            }
            if (result.size() == 0) {
                return null;
            }
            return result;

        }
        return null;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxFL = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_FRONT_LEFT, trxFL);
        result.put(NUMBER_TRX_FRONT_RIGHT, trxFR);
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_REAR_LEFT, trxRL);
        result.put(NUMBER_TRX_REAR_RIGHT, trxRR);
        return numberOfTrxValid(unlockMode, result);
    }

    @Override
    public boolean lockStrategy(boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket));
        boolean isLeaving = TrxUtils.getAverageLSDeltaGreaterThanThreshold(this, TrxUtils.getCurrentLockThreshold(averageDeltaLockThreshold, smartphoneIsInPocket));
        return (isInLockArea && isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxFL = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_FRONT_LEFT, trxFL);
        result.put(NUMBER_TRX_FRONT_RIGHT, trxFR);
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_REAR_LEFT, trxRL);
        result.put(NUMBER_TRX_REAR_RIGHT, trxRR);
        return numberOfTrxValid(lockMode, result);
    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newLockStatus, boolean smartphoneIsInPocket) {
        int threshold = TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket);
        return (totalAverage >= threshold) && newLockStatus;
    }

    @Override
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        return createFirstFooterDebugData(spannableStringBuilder, SPACE_ONE, SPACE_TWO);
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
    @Override
    public SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder, boolean smartphoneIsInPocket, boolean smartphoneIsLaidDownLAcc,
                                                              int totalAverage, boolean rearmLock, boolean rearmUnlock) {
        // WELCOME
        spannableStringBuilder.append("welcome ");
        StringBuilder footerSB = new StringBuilder();
        footerSB.append("rssi > (")
                .append(TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket))
                .append("): ").append(totalAverage);
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket),
                footerSB.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_WELCOME, smartphoneIsLaidDownLAcc))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_WELCOME, Color.WHITE,
                TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket), ">", SPACE_TWO));
        // LOCK
        spannableStringBuilder.append("lock").append("  mode : ").append(String.valueOf(lockMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append(String.valueOf(TrxUtils.getAverageLSDelta(this))).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getAverageLSDeltaGreaterThanThreshold(this, TrxUtils.getCurrentLockThreshold(averageDeltaLockThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rssi < (").append(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInLockArea(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rearm Lock: ").append(rearmLock);
        spannableStringBuilder.append(TextUtils.colorText(
                rearmLock, footerSB.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_LOCK, smartphoneIsLaidDownLAcc))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_LOCK, Color.RED,
                TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket), "<", SPACE_TWO));
        // UNLOCK
        spannableStringBuilder.append("unlock").append("  mode : ").append(String.valueOf(unlockMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append(String.valueOf(TrxUtils.getAverageLSDelta(this))).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(averageDeltaUnlockThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rearm Unlock: ").append(rearmUnlock);
        spannableStringBuilder.append(TextUtils.colorText(
                rearmUnlock, footerSB.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_UNLOCK, smartphoneIsLaidDownLAcc))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_UNLOCK, Color.GREEN,
                TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket), ">", SPACE_TWO));
        footerSB.setLength(0);
        footerSB.append("       ratio L/R > (+/-")
                .append(nearDoorRatioThreshold)
                .append("): ").append(getRatioNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold)
                        || isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInStartArea(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_START, smartphoneIsLaidDownLAcc))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket), ">", SPACE_TWO));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L OR M/R Max > (")
                .append(nearDoorThresholdMLorMRMax)
                .append("): ").append(getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" | ").append(getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMax
                        || getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMax,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L AND M/R Min > (")
                .append(nearDoorThresholdMLorMRMin)
                .append("): ").append(getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" & ").append(getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMin
                        && getRatioNearDoor(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMin,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        return spannableStringBuilder;
    }
}
