package com.valeo.bleranging.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.bluetooth.BluetoothManagement;
import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.model.connectedcar.ConnectedCar;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.Locale;

/**
 * Created by l-avaratha on 20/07/2016.
 */
public class TextUtils {
    private final static int welcomeThreshold = SdkPreferencesHelper.getInstance().getWelcomeThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int lockThreshold = SdkPreferencesHelper.getInstance().getLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int unlockThreshold = SdkPreferencesHelper.getInstance().getUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int startThreshold = SdkPreferencesHelper.getInstance().getStartThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static float linAccThreshold = SdkPreferencesHelper.getInstance().getCorrectionLinAcc();
    private final static int averageDeltaLockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaLockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int averageDeltaUnlockThreshold = SdkPreferencesHelper.getInstance().getAverageDeltaUnlockThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int lockMode = SdkPreferencesHelper.getInstance().getLockMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int unlockMode = SdkPreferencesHelper.getInstance().getUnlockMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int startMode = SdkPreferencesHelper.getInstance().getStartMode(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int nearDoorRatioThreshold = SdkPreferencesHelper.getInstance().getNearDoorRatioThreshold(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int nearBackDoorRatioThresholdMin = SdkPreferencesHelper.getInstance().getNearBackDoorRatioThresholdMin(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int nearBackDoorRatioThresholdMax = SdkPreferencesHelper.getInstance().getNearBackDoorRatioThresholdMax(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int nearDoorThresholdMLorMRMin = SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMin(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int nearDoorThresholdMLorMRMax = SdkPreferencesHelper.getInstance().getNearDoorThresholdMLorMRMax(SdkPreferencesHelper.getInstance().getConnectedCarType());
    private final static int rollingAvElement = SdkPreferencesHelper.getInstance().getRollingAvElement();
    private final static int startNbElement = SdkPreferencesHelper.getInstance().getStartNbElement();
    private final static int lockNbElement = SdkPreferencesHelper.getInstance().getLockNbElement();
    private final static int unlockNbElement = SdkPreferencesHelper.getInstance().getUnlockNbElement();
    private final static int welcomeNbElement = SdkPreferencesHelper.getInstance().getWelcomeNbElement();
    private final static int longNbElement = SdkPreferencesHelper.getInstance().getLongNbElement();
    private final static int shortNbElement = SdkPreferencesHelper.getInstance().getShortNbElement();

    /**
     * Color antenna average with color if comparaisonSign (> or <) threshold, DK_GRAY otherwise
     * @param average the average to color
     * @param color the color to use if the conditions is checked
     * @param threshold the threshold to compare with
     * @param comparaisonSign the comparaison sign
     * @return a colored average string
     */
    private static SpannableString colorAntennaAverage(int average, int color, int threshold, String comparaisonSign) {
        StringBuilder averageStringBuilder = new StringBuilder().append(average).append("     ");
        SpannableString spanString = new SpannableString(averageStringBuilder.toString());
        if(comparaisonSign.equals(">")) {
            if(average <= threshold) {
                color = Color.DKGRAY;
            }
        } else if(comparaisonSign.equals("<")) {
            if(average >= threshold) {
                color = Color.DKGRAY;
            }
        }
        spanString.setSpan(new ForegroundColorSpan(color), 0, averageStringBuilder.length(), 0);
        return spanString;
    }

    /**
     * Print the bytes of a tab of bytes
     * @param bytesTab the byte tab to print
     * @return the string representation of the tab of bytes
     */
    public static String printBleBytes(byte[] bytesTab) {
        if(bytesTab != null && bytesTab.length > 0) {
            StringBuilder sb = new StringBuilder(bytesTab.length * 2);
            for (byte b : bytesTab) {
                sb.append(String.format("%02X ", b));
            }
            return sb.toString();
        }
        return "";
    }

    /**
     * Color a text with different color if the boolean is true or false
     * @param active the boolean to check to get the right color
     * @param text the text to color
     * @param colorActive the active color
     * @param colorInactive the inactive color
     * @return the colored text as a spannable string
     */
    private static SpannableString colorText(boolean active, String text, int colorActive, int colorInactive) {
        SpannableString spanString = new SpannableString(text);
        if(active) {
            spanString.setSpan(new ForegroundColorSpan(colorActive), 0, text.length(), 0);
        } else {
            spanString.setSpan(new ForegroundColorSpan(colorInactive), 0, text.length(), 0);
        }
        return spanString;
    }

    /**
     * Create a string of header debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     */
    public static SpannableStringBuilder createHeaderDebugData(SpannableStringBuilder spannableStringBuilder, ConnectedCar connectedCar, Antenna.BLEChannel bleChannel) {
        spannableStringBuilder.append("Scanning on channel: ").append(bleChannel.toString()).append("\n");
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append(colorText(connectedCar.isActive(ConnectedCar.NUMBER_TRX_LEFT), "      LEFT       ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(colorText(connectedCar.isActive(ConnectedCar.NUMBER_TRX_MIDDLE), "   MIDDLE        ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(colorText(connectedCar.isActive(ConnectedCar.NUMBER_TRX_RIGHT), "   RIGHT     ", Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(colorText(connectedCar.isActive(ConnectedCar.NUMBER_TRX_BACK), "   BACK\n", Color.WHITE, Color.DKGRAY));
        return spannableStringBuilder;
    }

    /**
     * Create a string of footer debug
     *
     * @param spannableStringBuilder the spannable string builder to fill
     * @param connectedCar           the connected car
     * @return the spannable string builder filled with the first footer
     */
    public static SpannableStringBuilder createFirstFooterDebugData(SpannableStringBuilder spannableStringBuilder, ConnectedCar connectedCar) {
        spannableStringBuilder.append("\n"); // return to line after tryStrategies print if success
        StringBuilder dataStringBuilder = new StringBuilder()
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)).append("     ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)).append("     ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)).append("     ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT)).append("      ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_1, Antenna.AVERAGE_DEFAULT)).append("     ")
                .append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_2, Antenna.AVERAGE_DEFAULT)).append("\n");
        dataStringBuilder
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_2)).append("      ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2)).append("      ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_2)).append("      ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getCurrentOriginalRssi(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_2)).append("\n");
        dataStringBuilder
                .append("      ").append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_0, Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_0, Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_0, Antenna.AVERAGE_DEFAULT)).append("           ")
                .append("      ").append(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_0, Antenna.AVERAGE_DEFAULT)).append("\n");
        dataStringBuilder.append("                               ")
                .append("Total :").append(" ").append(connectedCar.getAllTrxAverage(Antenna.AVERAGE_DEFAULT)).append("\n");
        spannableStringBuilder.append(dataStringBuilder.toString());
        return spannableStringBuilder;
    }

    /**
     * Get the string from the second footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param connectedCar             the connected car
     * @param smartphoneIsInPocket     a boolean that determine if the smartphone is in the user pocket or not.
     * @param smartphoneIsLaidDownLAcc a boolean that determine if the smartphone is moving
     * @param totalAverage             the total average of all trx
     * @param rearmLock                a boolean corresponding to the rearm for lock purpose
     * @param rearmUnlock              a boolean corresponding to the rear for unlock purpose
     * @return the spannable string builder filled with the second footer
     */
    public static SpannableStringBuilder createSecondFooterDebugData(SpannableStringBuilder spannableStringBuilder,
                                                                     ConnectedCar connectedCar, boolean smartphoneIsInPocket, boolean smartphoneIsLaidDownLAcc,
                                                                     int totalAverage, boolean rearmLock, boolean rearmUnlock) {
        // WELCOME
        spannableStringBuilder.append("welcome ");
        StringBuilder welcomeStringBuilder = new StringBuilder().append("rssi > (")
                .append(TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket))
                .append("): ").append(totalAverage).append("\n");
        spannableStringBuilder.append(colorText(
                totalAverage > TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket),
                welcomeStringBuilder.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_WELCOME, Color.WHITE,
                TrxUtils.getCurrentLockThreshold(welcomeThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, connectedCar));
        // LOCK
        spannableStringBuilder.append("lock").append("  mode : ").append(String.valueOf(lockMode)).append(" ");
        StringBuilder averageLSDeltaLockStringBuilder = new StringBuilder().append(String.valueOf(TrxUtils.getAverageLSDelta(connectedCar))).append(" ");
        spannableStringBuilder.append(colorText(
                TrxUtils.getAverageLSDeltaGreaterThanThreshold(connectedCar, TrxUtils.getCurrentLockThreshold(averageDeltaLockThreshold, smartphoneIsInPocket)),
                averageLSDeltaLockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        StringBuilder lockStringBuilder = new StringBuilder().append("rssi < (").append(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(colorText(
                connectedCar.isInLockArea(TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket)),
                lockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        StringBuilder rearmLockStringBuilder = new StringBuilder().append("rearm Lock: ").append(rearmLock).append("\n");
        spannableStringBuilder.append(colorText(
                rearmLock,
                rearmLockStringBuilder.toString(), Color.RED, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_LOCK, Color.RED,
                TrxUtils.getCurrentLockThreshold(lockThreshold, smartphoneIsInPocket), "<", smartphoneIsLaidDownLAcc, connectedCar));
        // UNLOCK
        spannableStringBuilder.append("unlock").append("  mode : ").append(String.valueOf(unlockMode)).append(" ");
        StringBuilder averageLSDeltaUnlockStringBuilder = new StringBuilder().append(String.valueOf(TrxUtils.getAverageLSDelta(connectedCar))).append(" ");
        spannableStringBuilder.append(colorText(
                TrxUtils.getAverageLSDeltaLowerThanThreshold(connectedCar, TrxUtils.getCurrentUnlockThreshold(averageDeltaUnlockThreshold, smartphoneIsInPocket)),
                averageLSDeltaUnlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder unlockStringBuilder = new StringBuilder().append("rssi > (").append(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)).append(") ");
        spannableStringBuilder.append(colorText(
                connectedCar.isInUnlockArea(TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket)),
                unlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder rearmUnlockStringBuilder = new StringBuilder().append("rearm Unlock: ").append(rearmUnlock).append("\n");
        spannableStringBuilder.append(colorText(
                rearmUnlock,
                rearmUnlockStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_UNLOCK, Color.GREEN,
                TrxUtils.getCurrentUnlockThreshold(unlockThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, connectedCar));
        StringBuilder ratioLRStringBuilder = new StringBuilder().append("       ratio L/R > (+/-")
                .append(nearDoorRatioThreshold)
                .append("): ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(colorText(
                connectedCar.isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, nearDoorRatioThreshold)
                        || connectedCar.isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_RIGHT, -nearDoorRatioThreshold),
                ratioLRStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder ratioLBStringBuilder = new StringBuilder().append("       ratio LouR - B (")
                .append("< ").append(nearBackDoorRatioThresholdMin)
                .append("): ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK))
                .append("|").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK)).append("\n");
        spannableStringBuilder.append(colorText(
                (connectedCar.isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin)
                        || connectedCar.isRatioNearDoorLowerThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, nearBackDoorRatioThresholdMin)),
                ratioLBStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        StringBuilder ratioRBStringBuilder = new StringBuilder().append("       ratio LouR - B (")
                .append(" > ").append(nearBackDoorRatioThresholdMax)
                .append("): ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK))
                .append("|").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK)).append("\n");
        spannableStringBuilder.append(colorText(
                (connectedCar.isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_LEFT, ConnectedCar.NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax)
                        || connectedCar.isRatioNearDoorGreaterThanThreshold(Antenna.AVERAGE_UNLOCK, ConnectedCar.NUMBER_TRX_RIGHT, ConnectedCar.NUMBER_TRX_BACK, nearBackDoorRatioThresholdMax)),
                ratioRBStringBuilder.toString(), Color.GREEN, Color.DKGRAY));
        // START
        spannableStringBuilder.append("start").append("  mode : ").append(String.valueOf(startMode)).append(" ");
        StringBuilder startStringBuilder = new StringBuilder().append("rssi > (").append(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)).append(")\n");
        spannableStringBuilder.append(colorText(
                connectedCar.isInStartArea(TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket)),
                startStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        spannableStringBuilder.append(printModedAverage(Antenna.AVERAGE_START, Color.CYAN,
                TrxUtils.getCurrentStartThreshold(startThreshold, smartphoneIsInPocket), ">", smartphoneIsLaidDownLAcc, connectedCar));
        StringBuilder ratioMLMRMaxStringBuilder = new StringBuilder().append("       ratio M/L OR M/R Max > (")
                .append(nearDoorThresholdMLorMRMax)
                .append("): ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT))
                .append(" | ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(colorText(
                connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMax
                        || connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMax,
                ratioMLMRMaxStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        StringBuilder ratioMLMRMinStringBuilder = new StringBuilder().append("       ratio M/L AND M/R Min > (")
                .append(nearDoorThresholdMLorMRMin)
                .append("): ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT))
                .append(" & ").append(connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT)).append("\n");
        spannableStringBuilder.append(colorText(
                connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_LEFT) > nearDoorThresholdMLorMRMin
                        && connectedCar.getRatioNearDoor(Antenna.AVERAGE_START, ConnectedCar.NUMBER_TRX_MIDDLE, ConnectedCar.NUMBER_TRX_RIGHT) > nearDoorThresholdMLorMRMin,
                ratioMLMRMinStringBuilder.toString(), Color.CYAN, Color.DKGRAY));
        return spannableStringBuilder;
    }

    /**
     * Get the string from the third footer
     *
     * @param spannableStringBuilder   the string builder to fill
     * @param connectedCar             the connected car
     * @param bytesToSend              the bytes to send
     * @param bytesReceived            the bytes received
     * @param deltaLinAcc              the delta of linear acceleration
     * @param smartphoneIsLaidDownLAcc the boolean that determine if the smartphone is moving or not
     * @param mBluetoothManager        the bluetooth manager
     * @return the string builder filled with the third footer data
     */
    public static SpannableStringBuilder createThirdFooterDebugData(SpannableStringBuilder spannableStringBuilder, ConnectedCar connectedCar,
                                                                    byte[] bytesToSend, byte[] bytesReceived, double deltaLinAcc, boolean smartphoneIsLaidDownLAcc, BluetoothManagement mBluetoothManager) {
        if (mBluetoothManager.isFullyConnected()) {
            spannableStringBuilder.append("Connected").append("\n")
                    .append("       Send:       ").append(printBleBytes((bytesToSend))).append("\n")
                    .append("       Receive: ").append(printBleBytes(bytesReceived)).append("\n");
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        StringBuilder lAccStringBuilder = new StringBuilder().append("Linear Acceleration < (")
                .append(linAccThreshold)
                .append("): ").append(String.format(Locale.FRANCE, "%1$.4f", deltaLinAcc)).append("\n");
        spannableStringBuilder.append(colorText(
                smartphoneIsLaidDownLAcc,
                lAccStringBuilder.toString(), Color.WHITE, Color.DKGRAY));
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        spannableStringBuilder.append("offset channel 38 :\n");
        StringBuilder offset38StringBuilder = new StringBuilder()
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_2)).append("      ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2)).append("      ");
        offset38StringBuilder
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_2)).append("      ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_1)).append("     ")
                .append(connectedCar.getOffsetBleChannel38(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_2)).append("\n");
        spannableStringBuilder.append(offset38StringBuilder.toString());
        spannableStringBuilder.append("-------------------------------------------------------------------------");
        return spannableStringBuilder;
    }

    /**
     * Color each antenna average with color if comparaisonSign (> or <) threshold, DK_GRAY otherwise
     *
     * @param mode            the average mode to calculate
     * @param color           the color to use if the conditions is checked
     * @param threshold       the threshold to compare with
     * @param comparaisonSign the comparaison sign
     * @return a colored spannablestringbuilder with all the trx's average
     */
    private static SpannableStringBuilder printModedAverage(int mode, int color, int threshold, String comparaisonSign, boolean smartphoneIsLaidDownLAcc, ConnectedCar connectedCar) {
        SpannableStringBuilder ssb = new SpannableStringBuilder(String.valueOf(getNbElement(mode, smartphoneIsLaidDownLAcc)) + "     ");
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_1, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_LEFT, Trx.ANTENNA_ID_2, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_1, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_MIDDLE, Trx.ANTENNA_ID_2, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_1, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_RIGHT, Trx.ANTENNA_ID_2, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_1, mode), color, threshold, comparaisonSign));
        ssb.append(colorAntennaAverage(connectedCar.getRssiAverage(ConnectedCar.NUMBER_TRX_BACK, Trx.ANTENNA_ID_2, mode), color, threshold, comparaisonSign));
        ssb.append("\n");
        return ssb;
    }

    /**
     * Calculate the number of element to use to calculate the rolling average
     *
     * @param mode the average mode
     * @return the number of element to calculate the average
     */
    private static int getNbElement(int mode, boolean smartphoneIsLaidDownLAcc) {
        if (smartphoneIsLaidDownLAcc) {
            return rollingAvElement;
        }
        switch (mode) {
            case Antenna.AVERAGE_DEFAULT:
                return rollingAvElement;
            case Antenna.AVERAGE_START:
                return startNbElement;
            case Antenna.AVERAGE_LOCK:
                return lockNbElement;
            case Antenna.AVERAGE_UNLOCK:
                return unlockNbElement;
            case Antenna.AVERAGE_WELCOME:
                return welcomeNbElement;
            case Antenna.AVERAGE_LONG:
                return longNbElement;
            case Antenna.AVERAGE_SHORT:
                return shortNbElement;
            default:
                return rollingAvElement;
        }
    }
}
