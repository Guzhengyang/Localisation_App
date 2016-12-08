package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.BleRangingHelper;
import com.valeo.bleranging.bluetooth.AlgoManager;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by l-avaratha on 07/09/2016
 */
public class CCEightFlFrLMRTRlRr extends ConnectedCar {
    private static final String SPACE_ONE = " ";
    private static final String SPACE_TWO = "   ";
    private int closeToCarFL;
    private int closeToCarFR;
    private int closeToCarRL;
    private int closeToCarRR;
    private boolean isLeftSide = true;
    private boolean isInFrontLeft = true;
    private boolean isInFrontRight = false;
    private int thresholdMaxMinRatio = 0;
    private ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public CCEightFlFrLMRTRlRr(Context mContext, boolean isIndoor) {
        super(mContext, ConnectionNumber.EIGHT_CONNECTION, isIndoor);
        trxFrontLeft = new Trx(NUMBER_TRX_FRONT_LEFT, TRX_FRONT_LEFT_NAME);
        trxFrontRight = new Trx(NUMBER_TRX_FRONT_RIGHT, TRX_FRONT_RIGHT_NAME);
        trxLeft = new Trx(NUMBER_TRX_LEFT, TRX_LEFT_NAME);
        trxMiddle = new Trx(NUMBER_TRX_MIDDLE, TRX_MIDDLE_NAME);
        trxRight = new Trx(NUMBER_TRX_RIGHT, TRX_RIGHT_NAME);
        trxTrunk = new Trx(NUMBER_TRX_TRUNK, TRX_TRUNK_NAME);
        trxRearLeft = new Trx(NUMBER_TRX_REAR_LEFT, TRX_REAR_LEFT_NAME);
        trxRearRight = new Trx(NUMBER_TRX_REAR_RIGHT, TRX_REAR_RIGHT_NAME);
        trxFrontLeft.setEnabled(true);
        trxFrontRight.setEnabled(true);
        trxLeft.setEnabled(true);
        trxMiddle.setEnabled(true);
        trxRight.setEnabled(true);
        trxTrunk.setEnabled(true);
        trxRearLeft.setEnabled(true);
        trxRearRight.setEnabled(true);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_LEFT, trxFrontLeft);
        trxLinkedHMap.put(NUMBER_TRX_FRONT_RIGHT, trxFrontRight);
        trxLinkedHMap.put(NUMBER_TRX_LEFT, trxLeft);
        trxLinkedHMap.put(NUMBER_TRX_MIDDLE, trxMiddle);
        trxLinkedHMap.put(NUMBER_TRX_RIGHT, trxRight);
        trxLinkedHMap.put(NUMBER_TRX_TRUNK, trxTrunk);
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
            trxLinkedHMap.get(NUMBER_TRX_TRUNK).init(historicDefaultValuePeriph);
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
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, false)) &&
                    (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMax, true)
                            || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMax, true)) &&
                    (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, nearDoorThresholdMLorMRMin, true)
                            && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, nearDoorThresholdMLorMRMin, true)) &&
                    (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_LEFT, nearDoorThresholdMRLorMRR, true)
                            || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_RIGHT, nearDoorThresholdMRLorMRR, true)) &&
                    (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_TRUNK, -10, true))) {
                result.add(BleRangingHelper.START_PASSENGER_AREA);
            }
            if ((compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT, nearDoorThresholdTLorTRMax, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT, nearDoorThresholdTLorTRMax, true))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT, nearDoorThresholdTLorTRMin, true)
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT, nearDoorThresholdTLorTRMin, true))
                    && (compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_LEFT, nearDoorThresholdTRLorTRR, true)
                    || compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_RIGHT, nearDoorThresholdTRLorTRR, true))
                    && compareRatioWithThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_TRUNK, -10, false)) {
                result.add(BleRangingHelper.START_TRUNK_AREA);
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
        boolean trxT = compareTrxWithThreshold(NUMBER_TRX_TRUNK, Antenna.AVERAGE_START, threshold, true);
        LinkedHashMap<Integer, Boolean> result = new LinkedHashMap<>();
        result.put(NUMBER_TRX_MIDDLE, trxM);
        result.put(NUMBER_TRX_TRUNK, trxT);
        return numberOfTrxValid(startMode, result);
    }

    @Override
    public List<Integer> unlockStrategy() {
        boolean isInUnlockArea = isInUnlockArea(unlockThreshold);
        lock.writeLock().lock();
//        PSALogs.d("closeR", "1 => " + closeToCarFL + " " + closeToCarFR + " " + closeToCarRL + " " + closeToCarRR);
        closeToCarFL = getRatioCloseToCar(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
//        PSALogs.d("closeR", "2 => " + closeToCarFL + " " + closeToCarFR + " " + closeToCarRL + " " + closeToCarRR);
        closeToCarFR = getRatioCloseToCar(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
//        PSALogs.d("closeR", "3 => " + closeToCarFL + " " + closeToCarFR + " " + closeToCarRL + " " + closeToCarRR);
        closeToCarRL = getRatioCloseToCar(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
//        PSALogs.d("closeR", "4 => " + closeToCarFL + " " + closeToCarFR + " " + closeToCarRL + " " + closeToCarRR);
        closeToCarRR = getRatioCloseToCar(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK, Antenna.AVERAGE_DEFAULT);
//        PSALogs.d("closeR", "5 => " + closeToCarFL + " " + closeToCarFR + " " + closeToCarRL + " " + closeToCarRR);
        thresholdMaxMinRatio = getThreeCornerLowerMaxMinRatio() + thresholdCloseToCar;
        lock.writeLock().unlock();
        if (isInUnlockArea) {
            boolean isNearDoorLRMax = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, nearDoorRatioThreshold, true);
            boolean isNearDoorLRMin = compareRatioWithThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -nearDoorRatioThreshold, false);
            lock.readLock().lock();
            boolean closeToCarFrontLeft = TrxUtils.compareWithThreshold(closeToCarFL, thresholdMaxMinRatio, true);
            boolean closeToCarRearLeft = TrxUtils.compareWithThreshold(closeToCarRL, thresholdMaxMinRatio, true);
            boolean closeToCarRearRight = TrxUtils.compareWithThreshold(closeToCarRR, thresholdMaxMinRatio, true);
            boolean closeToCarFrontRight = TrxUtils.compareWithThreshold(closeToCarFR, thresholdMaxMinRatio, true);
            lock.readLock().unlock();
            boolean closeToBeaconFL = compareTrxWithThreshold(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);
            boolean closeToBeaconFR = compareTrxWithThreshold(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);
            boolean closeToBeaconL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);
            boolean closeToBeaconR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);
            boolean closeToBeaconRL = compareTrxWithThreshold(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);
            boolean closeToBeaconRR = compareTrxWithThreshold(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK, closeToBeaconThreshold, true);

            List<Integer> result = new ArrayList<>();
            isLeftSide = isLeftSide();// if(average 3lefttrx - average 3righttrx) >0 isLeftSide is true, <0 isLeftSide is false
            if (isLeftSide) {
                isInFrontLeft = isFrontLeft(); // if(average fl& L - rearL&rearR) > 0 isInFrontLeft is true, <0 isInFrontLeft is false
                if (isInFrontLeft) {
                    if (closeToCarFrontLeft || closeToBeaconFL) { //maxMinFrontLeft ||
                        result.add(NUMBER_TRX_FRONT_LEFT);
                    }
                    if (isNearDoorLRMax || closeToBeaconL) { //maxMinLeft ||
                        result.add(NUMBER_TRX_LEFT);
                    }
                } else {
                    if (closeToCarRearLeft || closeToBeaconRL) { //maxMinRearLeft ||
                        result.add(NUMBER_TRX_REAR_LEFT);
                    }
                    if (((closeToCarRL + closeToCarRR) > (2 * thresholdMaxMinRatio)) || closeToBeaconRL || closeToBeaconRR) {
                        result.add(NUMBER_TRX_BACK);
                    }
                }
            } else {
                isInFrontRight = isFrontRight(); // if(average fR& R - rearR&rearL) > 0 isInFrontRight is true, <0 isInFrontRight is false
                if (isInFrontRight) {
                    if (closeToCarFrontRight || closeToBeaconFR) { //maxMinFrontRight ||
                        result.add(NUMBER_TRX_FRONT_RIGHT);
                    }
                    if (isNearDoorLRMin || closeToBeaconR) { //maxMinRight ||
                        result.add(NUMBER_TRX_RIGHT);
                    }
                } else {
                    if (closeToCarRearRight || closeToBeaconRR) { //maxMinRearRight ||
                        result.add(NUMBER_TRX_REAR_RIGHT);
                    }
                    if (((closeToCarRL + closeToCarRR) > (2 * thresholdMaxMinRatio)) || closeToBeaconRL || closeToBeaconRR) {
                        result.add(NUMBER_TRX_BACK);
                    }
                }
            }
            if (result.isEmpty()) {
                return null;
            }
            return result;
        }
        return null;
    }

    private boolean isLeftSide() {
        return (((getCurrentModifiedRssi(NUMBER_TRX_FRONT_LEFT) + getCurrentModifiedRssi(NUMBER_TRX_LEFT) + getCurrentModifiedRssi(NUMBER_TRX_REAR_LEFT)) / 3)
                - ((getCurrentModifiedRssi(NUMBER_TRX_FRONT_RIGHT) + getCurrentModifiedRssi(NUMBER_TRX_RIGHT) + getCurrentModifiedRssi(NUMBER_TRX_REAR_RIGHT)) / 3)) > 0;
    }

    private boolean isFrontLeft() {
        return (((getCurrentModifiedRssi(NUMBER_TRX_FRONT_LEFT) + getCurrentModifiedRssi(NUMBER_TRX_LEFT)) / 2)
                - ((getCurrentModifiedRssi(NUMBER_TRX_REAR_LEFT) + getCurrentModifiedRssi(NUMBER_TRX_REAR_RIGHT)) / 2)) > 0;
    }

    private boolean isFrontRight() {
        return (((getCurrentModifiedRssi(NUMBER_TRX_FRONT_RIGHT) + getCurrentModifiedRssi(NUMBER_TRX_RIGHT)) / 2)
                - ((getCurrentModifiedRssi(NUMBER_TRX_REAR_LEFT) + getCurrentModifiedRssi(NUMBER_TRX_REAR_RIGHT)) / 2)) > 0;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxFL = compareTrxWithThreshold(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxFR = compareTrxWithThreshold(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxRL = compareTrxWithThreshold(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_UNLOCK, threshold, true);
        boolean trxRR = compareTrxWithThreshold(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_UNLOCK, threshold, true);
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
        return (isInLockArea
                && compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Antenna.AVERAGE_LOCK, -60, false)
                && compareTrxWithThreshold(NUMBER_TRX_TRUNK, Antenna.AVERAGE_LOCK, -60, false)
        ); //&& isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxFL = compareTrxWithThreshold(NUMBER_TRX_FRONT_LEFT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxFR = compareTrxWithThreshold(NUMBER_TRX_FRONT_RIGHT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxL = compareTrxWithThreshold(NUMBER_TRX_LEFT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxM = compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxR = compareTrxWithThreshold(NUMBER_TRX_RIGHT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxRL = compareTrxWithThreshold(NUMBER_TRX_REAR_LEFT, Antenna.AVERAGE_LOCK, threshold, false);
        boolean trxRR = compareTrxWithThreshold(NUMBER_TRX_REAR_RIGHT, Antenna.AVERAGE_LOCK, threshold, false);
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
     * @param totalAverage             the total average of all trx
     * @param mAlgoManager             the algorithm manager
     * @return the spannable string builder filled with the second footer
     */
    @Override
    public SpannableStringBuilder createSecondFooterDebugData(
            SpannableStringBuilder spannableStringBuilder, int totalAverage, AlgoManager mAlgoManager) {
        lock.readLock().lock();
        spannableStringBuilder //TODO Remove after test
                .append(String.valueOf(getThreeCornerLowerMaxMinRatio())).append(" ")
                .append(String.valueOf(thresholdMaxMinRatio)).append("\n")
                .append(TextUtils.colorText(isLeftSide && isInFrontLeft, "FL:   ", Color.GREEN, Color.DKGRAY)).append(String.valueOf(closeToCarFL)).append("   ")
                .append(TextUtils.colorText(!isLeftSide && isInFrontRight, "FR:   ", Color.GREEN, Color.DKGRAY)).append(String.valueOf(closeToCarFR)).append("   ")
                .append(TextUtils.colorText(isLeftSide && !isInFrontLeft, "RL:   ", Color.GREEN, Color.DKGRAY)).append(String.valueOf(closeToCarRL)).append("   ")
                .append(TextUtils.colorText(!isLeftSide && !isInFrontRight, "RR:   ", Color.GREEN, Color.DKGRAY)).append(String.valueOf(closeToCarRR)).append("\n");
        lock.readLock().unlock();
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
        footerSB.append("rssiMid < (").append(-60).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                compareTrxWithThreshold(NUMBER_TRX_MIDDLE, Antenna.AVERAGE_LOCK, -60, false),
                footerSB.toString(), Color.RED, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("rssiTrunk < (").append(-60).append(") ");
        spannableStringBuilder.append(TextUtils.colorText(
                compareTrxWithThreshold(NUMBER_TRX_TRUNK, Antenna.AVERAGE_LOCK, -60, false),
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
        footerSB.setLength(0);
        footerSB.append("       ratio M/RL OR M/RR > (")
                .append(nearDoorThresholdMRLorMRR)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_LEFT))
                .append(" | ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_LEFT) > nearDoorThresholdMRLorMRR
                        || getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_REAR_RIGHT) > nearDoorThresholdMRLorMRR,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio T/L OR T/R Max > (")
                .append(nearDoorThresholdTLorTRMax)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT))
                .append(" | ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT) > nearDoorThresholdTLorTRMax
                        || getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT) > nearDoorThresholdTLorTRMax,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio T/L AND T/R Min > (")
                .append(nearDoorThresholdTLorTRMin)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT))
                .append(" & ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_LEFT) > nearDoorThresholdTLorTRMin
                        && getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_RIGHT) > nearDoorThresholdTLorTRMin,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        footerSB.setLength(0);
        footerSB.append("       ratio T/RL OR T/RR > (")
                .append(nearDoorThresholdTRLorTRR)
                .append("): ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_LEFT))
                .append(" | ").append(getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_RIGHT)).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_LEFT) > nearDoorThresholdTRLorTRR
                        || getRatioBetweenTwoTrx(Antenna.AVERAGE_START, NUMBER_TRX_TRUNK, NUMBER_TRX_REAR_RIGHT) > nearDoorThresholdTRLorTRR,
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        return createSecondFooterDebugData(spannableStringBuilder, SPACE_TWO);
    }
}
