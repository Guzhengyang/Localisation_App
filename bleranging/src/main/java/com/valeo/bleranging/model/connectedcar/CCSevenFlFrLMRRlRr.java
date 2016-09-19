package com.valeo.bleranging.model.connectedcar;

import android.content.Context;
import android.graphics.Color;
import android.text.SpannableStringBuilder;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;
import com.valeo.bleranging.utils.TextUtils;
import com.valeo.bleranging.utils.TrxUtils;

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
    private static final int MODE_ONE_OF_SIX_WITHOUT_MIDDLE = 13;
    private static final int MODE_ALL_SEVEN = 14;

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
        boolean isInStartArea = isInStartArea(TrxUtils.getCurrentStartThreshold(SdkPreferencesHelper.getInstance().getStartThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket));
        return (isInStartArea
                && (!newLockStatus)
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()))
                || isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType())))
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMax(SdkPreferencesHelper.getInstance().getConnectedCarType()))
                || isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMax(SdkPreferencesHelper.getInstance().getConnectedCarType())))
                && (isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_LEFT, SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMin(SdkPreferencesHelper.getInstance().getConnectedCarType()))
                && isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_START, NUMBER_TRX_MIDDLE, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMin(SdkPreferencesHelper.getInstance().getConnectedCarType())))
        );
    }

    @Override
    public boolean isInStartArea(int threshold) {
        boolean trxFL = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxM = isTrxGreaterThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_START, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getStartMode(SdkPreferencesHelper.getInstance().getConnectedCarType()), trxL, trxM, trxR, false, trxFL, trxRL, trxFR, trxRR);
    }

    private boolean isNearDoor(int mode, int trxNumber, int threshold, int threshold2, int threshold3, boolean areEquals) {
        switch (trxNumber) {
            case NUMBER_TRX_FRONT_LEFT:
                return isNearDoor(mode, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_FRONT_RIGHT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_FRONT_RIGHT:
                return isNearDoor(mode, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_LEFT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_LEFT:
                return isNearDoor(mode, NUMBER_TRX_LEFT, NUMBER_TRX_FRONT_LEFT, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_RIGHT:
                return isNearDoor(mode, NUMBER_TRX_RIGHT, NUMBER_TRX_FRONT_RIGHT, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_REAR_LEFT:
                return isNearDoor(mode, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_LEFT, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_REAR_RIGHT:
                return isNearDoor(mode, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_RIGHT, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, areEquals);
            case NUMBER_TRX_BACK:
                boolean isNearBackL = isNearDoor(mode, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_LEFT, threshold, threshold2, threshold3, areEquals);
                boolean isNearBackR = isNearDoor(mode, NUMBER_TRX_REAR_RIGHT, NUMBER_TRX_REAR_LEFT, NUMBER_TRX_RIGHT, threshold, threshold2, threshold3, areEquals);
                return isNearBackL && isNearBackR;
            default:
                return false;
        }
    }

    private boolean isNearDoor(int mode, int nearThisTrx, int sideTrx, int farTrx, int threshold, int threshold2, int threshold3, boolean areEquals) {
        boolean isNearSideTrx = isRatioNearDoorGreaterThanThreshold(mode, nearThisTrx, sideTrx, threshold);
        boolean isNearFarTrx = isRatioNearDoorGreaterThanThreshold(mode, nearThisTrx, farTrx, threshold);
        if (areEquals) {
            boolean isEquidistantToSideTrxAndFarTrx = isRatioNearDoorLowerThanThreshold(mode, sideTrx, farTrx, threshold2);
            return isNearSideTrx && isNearFarTrx && isEquidistantToSideTrxAndFarTrx;
        } else {
            boolean isNearerToSideTrxThanFarTrx = isRatioNearDoorGreaterThanThreshold(mode, sideTrx, farTrx, threshold3);
            return isNearSideTrx && isNearFarTrx && isNearerToSideTrxThanFarTrx;
        }
    }

    private boolean isNearDoor(int mode, int trxNumber) {
        int threshold = 0; //SdkPreferencesHelper.getInstance().getNearDoorThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType())
        int threshold2 = 0; //SdkPreferencesHelper.getInstance().getNearerThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType())
        int threshold3 = 0; //SdkPreferencesHelper.getInstance().getEquallyNearThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType())
        boolean isNearFront;
        boolean isNearSide;
        boolean isNearRear;
        // left front side proche de Fl : ((Fl - Fr) > thr && (Fl - L) > thr) && (L - Fr) > thr2 :Fr<L
        // OR left side  proche de L : ((L - Fl) > thr && (L - Rl) > thr) && (Fl - Rl < thr) :FletRl quasi egal car equidistant donc delta petit
        // OR rear left side  proche de Rl : ((Rl - L) > thr && (Rl - Rr) > thr) && (L - Rr) > thr2 :Rr<L
        switch (trxNumber) {
            case NUMBER_TRX_LEFT:
                isNearFront = isNearDoor(mode, NUMBER_TRX_FRONT_LEFT, threshold, threshold2, threshold3, false);
                isNearSide = isNearDoor(mode, NUMBER_TRX_LEFT, threshold, threshold2, threshold3, true);
                isNearRear = isNearDoor(mode, NUMBER_TRX_REAR_LEFT, threshold, threshold2, threshold3, false);
                return isNearFront || isNearSide || isNearRear;
            case NUMBER_TRX_RIGHT:
                isNearFront = isNearDoor(mode, NUMBER_TRX_FRONT_RIGHT, threshold, threshold2, threshold3, false);
                isNearSide = isNearDoor(mode, NUMBER_TRX_RIGHT, threshold, threshold2, threshold3, true);
                isNearRear = isNearDoor(mode, NUMBER_TRX_REAR_RIGHT, threshold, threshold2, threshold3, false);
                return isNearFront || isNearSide || isNearRear;
            case NUMBER_TRX_BACK:
                return isNearDoor(mode, NUMBER_TRX_BACK, threshold, threshold2, threshold3, false);
            default:
                return false;
        }
    }

    @Override
    public int unlockStrategy(boolean smartphoneIsInPocket) {
        boolean isInUnlockArea = isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket));
        boolean isApproaching = TrxUtils.getAverageLSDeltaLowerThanThreshold(this, TrxUtils.getCurrentUnlockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket));
        if (isInUnlockArea && isApproaching) {
            boolean isNearLeftDoor = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT);
            boolean isNearRightDoor = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_RIGHT);
            boolean isNearBackDoor = isNearDoor(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_BACK);
            boolean isNearDoorLRMax = isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()));
            boolean isNearDoorLRMin = isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()));
            if (isNearDoorLRMax && isNearLeftDoor) {
                return NUMBER_TRX_LEFT;
            } else if (isNearDoorLRMin && isNearRightDoor) {
                return NUMBER_TRX_RIGHT;
            } else if (isNearBackDoor) {
                return NUMBER_TRX_BACK;
            }
        }
        return 0;
    }

    @Override
    public boolean isInUnlockArea(int threshold) {
        boolean trxFL = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxFR = isTrxGreaterThanThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxL = isTrxGreaterThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxM = isTrxGreaterThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxR = isTrxGreaterThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRL = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        boolean trxRR = isTrxGreaterThanThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_UNLOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getUnlockMode(SdkPreferencesHelper.getInstance().getConnectedCarType()), trxL, trxM, trxR, false, trxFL, trxRL, trxFR, trxRR);
    }

    @Override
    public boolean lockStrategy(boolean smartphoneIsInPocket) {
        boolean isInLockArea = isInLockArea(TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket));
        boolean isLeaving = TrxUtils.getAverageLSDeltaGreaterThanThreshold(this, TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket));
        return (isInLockArea && isLeaving);
    }

    @Override
    public boolean isInLockArea(int threshold) {
        boolean trxFL = isTrxLowerThanThreshold(NUMBER_TRX_FRONT_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxFR = isTrxLowerThanThreshold(NUMBER_TRX_FRONT_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxL = isTrxLowerThanThreshold(NUMBER_TRX_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxM = isTrxLowerThanThreshold(NUMBER_TRX_MIDDLE, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxR = isTrxLowerThanThreshold(NUMBER_TRX_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRL = isTrxLowerThanThreshold(NUMBER_TRX_REAR_LEFT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        boolean trxRR = isTrxLowerThanThreshold(NUMBER_TRX_REAR_RIGHT, Trx.ANTENNA_AND, Antenna.AVERAGE_LOCK, threshold);
        return numberOfTrxValid(SdkPreferencesHelper.getInstance().getLockMode(SdkPreferencesHelper.getInstance().getConnectedCarType()), trxL, trxM, trxR, false, trxFL, trxRL, trxFR, trxRR)
                || (trxM && (isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()))
                || isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_LOCK, NUMBER_TRX_LEFT, NUMBER_TRX_RIGHT, -SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()))));

    }

    @Override
    public boolean welcomeStrategy(int totalAverage, boolean newlockStatus, boolean smartphoneIsInPocket) {
        int threshold = TrxUtils.getCurrentLockThreshold(SdkPreferencesHelper.getInstance().getWelcomeThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType()), smartphoneIsInPocket);
        return (totalAverage >= threshold) && newlockStatus;
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
                .append("): ").append(totalAverage).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                totalAverage > TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket),
                footerSB.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_WELCOME, Color.WHITE,
                TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, this));
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
        footerSB.append("rearm Lock: ").append(rearmLock).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                rearmLock,
                footerSB.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_LOCK, Color.RED,
                TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket), "<", smartphoneIsLaidDownLAcc, this));
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
        footerSB.append("rearm Unlock: ").append(rearmUnlock).append("\n");
        spannableStringBuilder.append(TextUtils.colorText(
                rearmUnlock,
                footerSB.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_UNLOCK, Color.GREEN,
                TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, this));
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
        footerSB.append("rssi > (").append(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)).append(")\n");
        spannableStringBuilder.append(TextUtils.colorText(
                isInStartArea(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)),
                footerSB.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, this));
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
            case MODE_ONE_OF_SIX_WITHOUT_MIDDLE:
                return trxFL || trxFR || trxL || trxR || trxRL || trxRR;
            case MODE_ALL_SEVEN:
                return trxFL && trxFR && trxL && trxM && trxR && trxRL && trxRR;
            default:
                return false;
        }
    }
}
