package com.valeo.bleranging.utils;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

/**
 * Created by l-avaratha on 20/07/2016
 */
public class TextUtils {

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
}
