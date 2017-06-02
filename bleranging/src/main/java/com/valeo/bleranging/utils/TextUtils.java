package com.valeo.bleranging.utils;

import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;

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
     * Print the bytes of a tab of bytes
     * @param bytesTab the byte tab to print
     * @return the string representation of the tab of bytes
     */
    public static String printAddressBytes(byte[] bytesTab) {
        if (bytesTab != null && bytesTab.length > 0) {
            StringBuilder sb = new StringBuilder((bytesTab.length * 3) - 1);
            for (int i = 0; i < bytesTab.length; i++) {
                byte b = bytesTab[i];
                sb.append(String.format("%02X", b));
                if (i < bytesTab.length - 1) {
                    sb.append(":");
                }
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

    public static void copyFile(String src, String dst) throws IOException {
        FileChannel inChannel = new FileInputStream(src).getChannel();
        FileChannel outChannel = new FileOutputStream(dst).getChannel();
        try {
            inChannel.transferTo(0, inChannel.size(), outChannel);
        } finally {
            closeQuietly(inChannel);
            closeQuietly(outChannel);
        }
    }

    /**
     * Close file channel quietly
     *
     * @param fileChannel the file channel to close
     */
    private static void closeQuietly(FileChannel fileChannel) {
        try {
            if (fileChannel != null) {
                fileChannel.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
