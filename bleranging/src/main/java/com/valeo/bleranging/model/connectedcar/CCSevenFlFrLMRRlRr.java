package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCSevenFlFrLMRRlRr extends ConnectedCar {
    private static final String SPACE_ONE = "  ";
    private static final String SPACE_TWO = "   ";
    private int ratio;
    private int closeToCarFL;
    private int closeToCarFR;
    private int closeToCarRL;
    private int closeToCarRR;
    private int thresholdMaxMinRatio = 0;

    public CCSevenFlFrLMRRlRr(Context mContext, boolean isIndoor) {
        super(mContext, ConnectionNumber.SEVEN_CONNECTION, isIndoor);
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
    public List<Integer> startStrategy() {
        boolean isInStartArea = isInStartArea(startThreshold);
        if (isInStartArea) {
            List<Integer> result = new ArrayList<>();
            if ((compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, false))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMax, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMax, true))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMin, true)
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin, true))) {
                result.add(BleRangingHelper.START_PASSENGER_AREA);
            }
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean isInStartArea(int threshold) {
        boolean trxM = compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold, true);
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



    @Override
    public List<Integer> unlockStrategy() {
        boolean isInUnlockArea = isInUnlockArea(unlockThreshold);
//        boolean isApproaching = TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaUnlockThreshold, false);
        boolean isApproaching = true;
        closeToCarFL = getRatioCloseToCar(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
        closeToCarFR = getRatioCloseToCar(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
        closeToCarRL = getRatioCloseToCar(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
        closeToCarRR = getRatioCloseToCar(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
        thresholdMaxMinRatio = getThreeCornerLowerMaxMinRatio();

//        ratio = (closeToCarFL - closeToCarRR) - (closeToCarFR - closeToCarRL);
        if (isInUnlockArea && isApproaching) {
//            boolean maxMinLeft = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRearLeft = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRearRight = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRight = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinFrontRight = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinFrontLeft = TrxUtils.compareWithThreshold(getRatioMaxMin(NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinLeft = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRearLeft = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_REAR_LEFT, NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRearRight = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinRight = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_RIGHT, NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinFrontRight = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);
//            boolean maxMinFrontLeft = TrxUtils.compareWithThreshold(getRatioBetweenTwoTrx(NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK), thresholdMaxMin, true);

            boolean isNearDoorLRMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, true);
            boolean isNearDoorLRMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, false);

//            int thresholdMaxMin = SdkPreferencesHelper.getInstance().getRatioMaxMinThreshold(connectedCarType);

//            boolean sideA = TrxUtils.compareWithThreshold(ratio, thresholdMaxMin, true);
//            boolean sideB = TrxUtils.compareWithThreshold(ratio, -thresholdMaxMin, false);
//            if (sideA || sideB) {
            boolean closeToCarFrontLeft = TrxUtils.compareWithThreshold(closeToCarFL, thresholdCloseToCar + thresholdMaxMinRatio, true);
            boolean closeToCarRearLeft = TrxUtils.compareWithThreshold(closeToCarRL, thresholdCloseToCar + thresholdMaxMinRatio, true);
            boolean closeToCarRearRight = TrxUtils.compareWithThreshold(closeToCarRR, thresholdCloseToCar + thresholdMaxMinRatio, true);
            boolean closeToCarFrontRight = TrxUtils.compareWithThreshold(closeToCarFR, thresholdCloseToCar + thresholdMaxMinRatio, true);
                List<Integer> result = new ArrayList<>();
                if (closeToCarFrontLeft) { //maxMinFrontLeft ||
                    result.add(NUMBER_TRX_FRONT_LEFT);
                }
                if (isNearDoorLRMax) { //maxMinLeft ||
                    result.add(NUMBER_TRX_LEFT);
                }
                if (closeToCarRearLeft) { //maxMinRearLeft ||
                    result.add(NUMBER_TRX_REAR_LEFT);
                }
                if (closeToCarFrontRight) { //maxMinFrontRight ||
                    result.add(NUMBER_TRX_FRONT_RIGHT);
                }
                if (isNearDoorLRMin) { //maxMinRight ||
                    result.add(NUMBER_TRX_RIGHT);
                }
                if (closeToCarRearRight) { //maxMinRearRight ||
                    result.add(NUMBER_TRX_REAR_RIGHT);
                }
            if (result.isEmpty()) {
                    return null;
                }
                return result;
            }
//        }
        return null;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxFL = compareTrxWithThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxFR = compareTrxWithThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxRL = compareTrxWithThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxRR = compareTrxWithThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold, true);
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
    public boolean lockStrategy() {
        boolean isInLockArea = isInLockArea(lockThreshold);
//        boolean isLeaving = TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaLockThreshold, true);
        return (isInLockArea); //&& isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxFL = compareTrxWithThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxFR = compareTrxWithThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxM = compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxRL = compareTrxWithThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxRR = compareTrxWithThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold, false);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_FRONT_LEFT, trxFL);
        result.put(NUMBER_TRX_FRONT_RIGHT, trxFR);
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_MIDDLE, trxM);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_REAR_LEFT, trxRL);
        result.put(NUMBER_TRX_REAR_RIGHT, trxRR);
        return numberOfTrxValid(lockMode, result);
    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newLockStatus) {
        return (totalAverage >= welcomeThreshold) && newLockStatus;
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
     * @param smartphoneIsMovingSlowly a boolean that determine if the smartphone is moving
     * @param totalAverage             the total average of all trx
     * @param rearmLock                a boolean corresponding to the rearm for lock purpose
     * @param rearmUnlock              a boolean corresponding to the rear for unlock purpose
     * @return the spannable string builder filled with the second footer
     */
    @Override
    public SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder, boolean smartphoneIsInPocket, boolean smartphoneIsMovingSlowly,
                                                              int totalAverage, boolean rearmLock, boolean rearmUnlock) {
        // WELCOME
//        spannableStringBuilder.append(String.valueOf(ratio)).append("\n");
        spannableStringBuilder //TODO Remove after test
                .append(String.valueOf(getThreeCornerLowerMaxMinRatio())).append(" ")
                .append(String.valueOf(thresholdMaxMinRatio)).append("\n")
                .append("FL:   ").append(String.valueOf(closeToCarFL)).append("   ")
                .append("FR:   ").append(String.valueOf(closeToCarFR)).append("   ")
                .append("RL:   ").append(String.valueOf(closeToCarRL)).append("   ")
                .append("RR:   ").append(String.valueOf(closeToCarRR)).append("\n");
        spannableStringBuilder.append("welcome ");
        StringBuilder footerSB = new StringBuilder();
        footerSB.append("rssi > (")
                .append(welcomeThreshold)
                .append("): ").append(totalAverage);
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > welcomeThreshold,
                footerSB.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_WELCOME, smartphoneIsMovingSlowly))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_WELCOME, Color.WHITE,
                welcomeThreshold, ">", SPACE_TWO));
        // LOCK
        spannableStringBuilder.append("lock").append("  mode : ").append(String.valueOf(lockMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append(String.valueOf(getAverageLSDelta())).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaLockThreshold, true),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rssi < (").append(lockThreshold).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(isInLockArea(lockThreshold),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rearm Lock: ").append(rearmLock);
        spannableStringBuilder.append(TextUtils.colorText(
                rearmLock, footerSB.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_LOCK, smartphoneIsMovingSlowly))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_LOCK, Color.RED,
                lockThreshold, "<", SPACE_TWO));
        // UNLOCK
        spannableStringBuilder.append("unlock").append("  mode : ").append(String.valueOf(unlockMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append(String.valueOf(getAverageLSDelta())).append(" ");
        spannableStringBuilder.append(TextUtils.colorText(
                TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaUnlockThreshold, false),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(unlockThreshold).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInUnlockArea(unlockThreshold),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rearm Unlock: ").append(rearmUnlock);
        spannableStringBuilder.append(TextUtils.colorText(
                rearmUnlock, footerSB.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_UNLOCK, smartphoneIsMovingSlowly))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_UNLOCK, Color.GREEN,
                unlockThreshold, ">", SPACE_TWO));
        footerSB.setLength(0);
        footerSB.append("       ratio L/R > (+/-")
                .append(nearDoorRatioThreshold)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, true)
                        || compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, false),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(startThreshold).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInStartArea(startThreshold),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_START, smartphoneIsMovingSlowly))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                startThreshold, ">", SPACE_TWO));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L OR M/R Max > (")
                .append(nearDoorThresholdMLorMRMax)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" | ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMax
                        || getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMax,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L AND M/R Min > (")
                .append(nearDoorThresholdMLorMRMin)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" & ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMin
                        && getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMin,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        return createSecondFooterDebugData(spannableStringBuilder, SPACE_TWO);
    }
}
