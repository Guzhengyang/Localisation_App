package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.bluetooth.AlgoManager;
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
public class CCFourLMRB extends ConnectedCar {
    private static final String SPACE_ONE = "        ";
    private static final String SPACE_TWO = "        ";

    public CCFourLMRB(Context mContext, boolean isIndoor) {
        super(mContext, ConnectionNumber.FOUR_CONNECTION, isIndoor);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxBack = new Trx(NUMBER_TRX_BACK, TRX_BACK_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxBack.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_BACK, trxBack);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_BACK).init(historicDefaultValuePeriph);
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
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin, true))
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_BACK, nearDoorThresholdMB, true)) {
//                result.add(BleRangingHelper.PREDICTION_START);
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
        boolean trxM = compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Antenna.AVERAGE_START, threshold, true);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_MIDDLE, trxM);
        return numberOfTrxValid(startMode, result);
    }

    @Override
    public List<Integer> unlockStrategy() {
        boolean isInUnlockArea = isInUnlockArea(unlockThreshold);
        boolean isNearDoorLRMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, true);
        boolean isNearDoorLRMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, false);
        boolean isNearDoorLBMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax, true);
        boolean isNearDoorLBMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin, false);
        boolean isNearDoorRBMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax, true);
        boolean isNearDoorRBMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin, false);
        boolean isApproaching = TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaUnlockThreshold, false);
        if (isInUnlockArea && isApproaching) {
            List<Integer> result = new ArrayList<>();
            if (isNearDoorLRMax && isNearDoorLBMax) {
                result.add(NUMBER_TRX_LEFT);
            }
            if (isNearDoorLRMin && isNearDoorRBMax) {
                result.add(NUMBER_TRX_RIGHT);
            }
            if (isNearDoorLBMin && isNearDoorRBMin) {
                result.add(NUMBER_TRX_BACK);
            }
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxB = compareTrxWithThreshold(NUMBER_TRX_BACK, Antenna.AVERAGE_UNLOCK, threshold, true);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_BACK, trxB);
        return numberOfTrxValid(unlockMode, result);
    }

    @Override
    public boolean lockStrategy() {
        boolean isInLockArea = isInLockArea(lockThreshold);
        boolean isLeaving = TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaLockThreshold, true);
        return (isInLockArea && isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxM = compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxB = compareTrxWithThreshold(NUMBER_TRX_BACK, Antenna.AVERAGE_LOCK, threshold, false);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_MIDDLE, trxM);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_BACK, trxB);
        return numberOfTrxValid(lockMode, result)
                || (trxM && (compareRatioWithThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, false)
                || compareRatioWithThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, true)));

    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newLockStatus) {
        return (totalAverage >= welcomeThreshold) && newLockStatus;
    }

    @Override
    public SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder) {
        return createFirstFooterDebugData(spannableStringBuilder, SPACE_ONE, SPACE_TWO);
    }

    @Override
    public SpannableStringBuilder createSecondFooterDebugData(
            SpannableStringBuilder spannableStringBuilder, int totalAverage, AlgoManager mAlgoManager) {
        // WELCOME
        spannableStringBuilder.append("welcome ");
        StringBuilder footerSB = new StringBuilder();
        footerSB.append("rssi > (")
                .append(welcomeThreshold)
                .append("): ").append(totalAverage);
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > welcomeThreshold,
                footerSB.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_WELCOME, mAlgoManager.isSmartphoneMovingSlowly()))).append("\n");
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
        spannableStringBuilder.append(TextUtils.colorText(
                isInLockArea(lockThreshold),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rearm Lock: ").append(mAlgoManager.getRearmLock());
        spannableStringBuilder.append(TextUtils.colorText(
                mAlgoManager.getRearmLock(), footerSB.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_LOCK, mAlgoManager.isSmartphoneMovingSlowly()))).append("\n");
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
        footerSB.append("rearm Unlock: ").append(mAlgoManager.getRearmUnlock());
        spannableStringBuilder.append(TextUtils.colorText(
                mAlgoManager.getRearmUnlock(), footerSB.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_UNLOCK, mAlgoManager.isSmartphoneMovingSlowly()))).append("\n");
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
        footerSB.setLength(0);
        footerSB.append("       ratio LouR - B (")
                .append("< ").append(nearBackDoorRatioThresholdMin)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK))
                .append("|").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                (compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin, false)
                        || compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin, false)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio LouR - B (")
                .append(" > ").append(nearBackDoorRatioThresholdMax)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK))
                .append("|").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                (compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax, true)
                        || compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax, true)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(startThreshold).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInStartArea(startThreshold),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_START, mAlgoManager.isSmartphoneMovingSlowly()))).append("\n");
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                startThreshold, ">", SPACE_TWO));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L OR M/R Max > (")
                .append(nearDoorThresholdMLorMRMax)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" | ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMax, true)
                        || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMax, true),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio M/L AND M/R Min > (")
                .append(nearDoorThresholdMLorMRMin)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT))
                .append(" & ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMin, true)
                        && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin, true),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio B/M > (")
                .append(nearDoorThresholdMB)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_BACK))
                .append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_BACK, nearDoorThresholdMB, true),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        return createSecondFooterDebugData(spannableStringBuilder, SPACE_TWO);
    }

}
