package com.valeo.bleranging.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.model.Antenna;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.util.Locale;

/**
 * Created by l-avaratha on 20/07/2016
 */
public class TextUtils {
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
    public static SpannableString colorAntennaAverage(int average, int color, int threshold, String comparaisonSign, String space) {
        StringBuilder averageStringBuilder = new StringBuilder().append(space).append(String.format(Locale.FRANCE, "%1$03d", average)).append(space);
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
    public static SpannableString colorText(boolean active, String text, int colorActive, int colorInactive) {
        SpannableString spanString = new SpannableString(text);
        if(active) {
            spanString.setSpan(new ForegroundColorSpan(colorActive), 0, text.length(), 0);
        } else {
            spanString.setSpan(new ForegroundColorSpan(colorInactive), 0, text.length(), 0);
        }
        return spanString;
    }

    /**
     * Calculate the number of element to use to calculate the rolling average
     *
     * @param mode the average mode
     * @return the number of element to calculate the average
     */
    public static int getNbElement(int mode, boolean smartphoneIsMovingSlowly) {
        if (smartphoneIsMovingSlowly) {
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
