package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.algorithm.AlgoManager;
import com.valeo.bleranging.bluetooth.RKEManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static com.valeo.bleranging.BleRangingHelper.PREDICTION_LEFT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_RIGHT;
import static com.valeo.bleranging.BleRangingHelper.PREDICTION_TRUNK;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCFourLMRT extends ConnectedCar {
    private static final String SPACE_ONE = "        ";
    private static final String SPACE_TWO = "        ";

    public CCFourLMRT(Context mContext, boolean isIndoor) {
        super(mContext, ConnectionNumber.FOUR_CONNECTION, isIndoor);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
    }

    @Override
    public void initializeTrx(int historicDefaultValuePeriph, int historicDefaultValueCentral) {
        if (trxLinkedHMap != null) {
            trxLinkedHMap.get(NUMBER_TRX_LEFT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_MIDDLE).init(historicDefaultValueCentral);
            trxLinkedHMap.get(NUMBER_TRX_RIGHT).init(historicDefaultValuePeriph);
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
        }
    }

    @Override
    public List<String> startStrategy() {
        boolean isInStartArea = isInStartArea(startThreshold);
        if (isInStartArea) {
            List<String> result = new ArrayList<>();
            if ((compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, false))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMax, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMax, true))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMin, true)
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin, true))
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_TRUNK, nearDoorThresholdMB, true)) {
                result.add(BleRangingHelper.PREDICTION_START);
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
    public List<String> unlockStrategy() {
        boolean isInUnlockArea = isInUnlockArea(unlockThreshold);
        boolean isNearDoorLRMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, true);
        boolean isNearDoorLRMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, false);
        boolean isNearDoorLBMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMax, true);
        boolean isNearDoorLBMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMin, false);
        boolean isNearDoorRBMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMax, true);
        boolean isNearDoorRBMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMin, false);
        boolean isApproaching = TrxUtils.compareWithThreshold(getAverageLSDelta(), averageDeltaUnlockThreshold, false);
        if (isInUnlockArea && isApproaching) {
            List<String> result = new ArrayList<>();
            if (isNearDoorLRMax && isNearDoorLBMax) {
                result.add(PREDICTION_LEFT);
            }
            if (isNearDoorLRMin && isNearDoorRBMax) {
                result.add(PREDICTION_RIGHT);
            }
            if (isNearDoorLBMin && isNearDoorRBMin) {
                result.add(PREDICTION_TRUNK);
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
        boolean trxT = compareTrxWithThreshold(NUMBER_TRX_TRUNK, Antenna.AVERAGE_UNLOCK, threshold, true);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_TRUNK, trxT);
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
        boolean trxT = compareTrxWithThreshold(NUMBER_TRX_TRUNK, Antenna.AVERAGE_LOCK, threshold, false);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_LEFT, trxL);
        result.put(NUMBER_TRX_MIDDLE, trxM);
        result.put(NUMBER_TRX_RIGHT, trxR);
        result.put(NUMBER_TRX_TRUNK, trxT);
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
            SpannableStringBuilder spannableStringBuilder, int totalAverage,
            AlgoManager mAlgoManager, RKEManager rkeManager) {
        // WELCOME
        spannableStringBuilder.append("welcome ");
        StringBuilder footerSB = new StringBuilder();
        footerSB.append("rssi > (")
                .append(welcomeThreshold)
                .append("): ").append(totalAverage);
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > welcomeThreshold,
                footerSB.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_WELCOME, mAlgoManager.getAlgoStandard().isSmartphoneMovingSlowly()))).append("\n");
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
        footerSB.append("rearm Lock: ").append(rkeManager.getRearmLock());
        spannableStringBuilder.append(TextUtils.colorText(
                rkeManager.getRearmLock(), footerSB.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_LOCK, mAlgoManager.getAlgoStandard().isSmartphoneMovingSlowly()))).append("\n");
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
        footerSB.append("rearm Unlock: ").append(rkeManager.getRearmUnlock());
        spannableStringBuilder.append(TextUtils.colorText(
                rkeManager.getRearmUnlock(), footerSB.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_UNLOCK, mAlgoManager.getAlgoStandard().isSmartphoneMovingSlowly()))).append("\n");
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
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK))
                .append("|").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                (compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMin, false)
                        || compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMin, false)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio LouR - B (")
                .append(" > ").append(nearBackDoorRatioThresholdMax)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK))
                .append("|").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                (compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMax, true)
                        || compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT, NUMBER_TRX_TRUNK, nearBackDoorRatioThresholdMax, true)),
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        footerSB.setLength(0);
        footerSB.append("rssi > (").append(startThreshold).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                isInStartArea(startThreshold),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(" ").append(String.valueOf(TextUtils.getNbElement(Antenna.AVERAGE_START, mAlgoManager.getAlgoStandard().isSmartphoneMovingSlowly()))).append("\n");
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
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_TRUNK))
                .append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_TRUNK, nearDoorThresholdMB, true),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        return createSecondFooterDebugData(spannableStringBuilder, SPACE_TWO);
    }

}
