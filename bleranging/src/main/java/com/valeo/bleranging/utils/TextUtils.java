package com.valeo.bleranging.utils;

import android.graphics.Color;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.ForegroundColorSpan;

import com.valeo.bleranging.model.ConnectedCar;
import com.valeo.bleranging.model.Trx;
import com.valeo.bleranging.persistence.SdkPreferencesHelper;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.LinkedHashMap;
import java.util.Locale;

/**
 * Created by l-avaratha on 20/07/2016
 */
public class TextUtils {

    /**
     * Convert hex string to byte array
     * @param encoded the hex string
     * @return a byte array
     */
    public static byte[] fromHexString(final String encoded) {
        if ((encoded.length() % 2) != 0)
            throw new IllegalArgumentException("Input string must contain an even number of characters");

        final byte result[] = new byte[encoded.length() / 2];
        final char enc[] = encoded.toCharArray();
        for (int i = 0; i < enc.length; i += 2) {
            StringBuilder curr = new StringBuilder(2);
            curr.append(enc[i]).append(enc[i + 1]);
            result[i / 2] = (byte) Integer.parseInt(curr.toString(), 16);
        }
        return result;
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
    private static SpannableString colorText(boolean active, String text, int colorActive, int colorInactive) {
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

    /**
     * Create a string of header debug
     *
     * @param bytesToSend      the bytes to send
     * @param bytesReceived    the bytes received
     * @param isFullyConnected the boolean that determine if the smartphone is connected or not
     * @return the spannable string builder filled with the header
     */
    public static SpannableStringBuilder createHeaderDebugData(final byte[] bytesToSend, final byte[] bytesReceived, boolean isFullyConnected) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (isFullyConnected) {
            if (bytesToSend != null) {
                spannableStringBuilder.append("       Send:       ").append(TextUtils.printBleBytes((bytesToSend))).append("\n");
            }
            if (bytesReceived != null) {
                spannableStringBuilder.append("       Receive: ").append(TextUtils.printBleBytes(bytesReceived)).append("\n");
            }
        } else {
            SpannableString disconnectedSpanString = new SpannableString("Disconnected\n");
            disconnectedSpanString.setSpan(new ForegroundColorSpan(Color.DKGRAY), 0, "Disconnected\n".length(), 0);
            spannableStringBuilder.append(disconnectedSpanString);
        }
        spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        return spannableStringBuilder;
    }

    /**
     * Create a string of footer debug
     *
     * @return the spannable string builder filled with the first footer
     */
    public static SpannableStringBuilder createFirstFooterDebugData(final ConnectedCar connectedCar) {
        final SpannableStringBuilder spannableStringBuilder = new SpannableStringBuilder();
        if (connectedCar != null) {
            final LinkedHashMap<Integer, Trx> trxLinkedHMap = connectedCar.getMultiTrx().getTrxLinkedHMap();
            for (Trx trx : trxLinkedHMap.values()) {
                spannableStringBuilder.append(String.format(Locale.FRANCE, "%7s",
                        TextUtils.colorText(connectedCar.getMultiTrx().isActive(trx.getTrxNumber()), trx.getTrxName(), Color.WHITE, Color.DKGRAY)));
            }
            spannableStringBuilder.append("\n");
            for (Trx trx : trxLinkedHMap.values()) {
                spannableStringBuilder.append(String.format(Locale.FRANCE, "%10d",
                        connectedCar.getMultiTrx().getCurrentOriginalRssi(trx.getTrxNumber())));
            }
            spannableStringBuilder.append('\n');
            for (Trx trx : trxLinkedHMap.values()) {
                spannableStringBuilder.append(String.format(Locale.FRANCE, "%10s",
                        getCurrentBLEChannelString(connectedCar, trx.getTrxNumber())));
            }
            spannableStringBuilder.append('\n');
            spannableStringBuilder.append("-------------------------------------------------------------------------\n");
            for (Trx trx : trxLinkedHMap.values()) {
                spannableStringBuilder.append(trx.getTrxName()).append("   ");
                spannableStringBuilder.append(SdkPreferencesHelper.getInstance().getTrxAddress(trx.getTrxNumber())).append("   ");
                spannableStringBuilder.append(String.valueOf(SdkPreferencesHelper.getInstance().getCarRssi(trx.getTrxNumber()))).append("\n");
            }
            spannableStringBuilder.append("-------------------------------------------------------------------------\n");
            spannableStringBuilder.append(connectedCar.getMultiPrediction().printDebug());
            spannableStringBuilder.append("-------------------------------------------------------------------------\n");
        }
        return spannableStringBuilder;
    }

    private static String getCurrentBLEChannelString(final ConnectedCar connectedCar, int trxNumber) {
        String result;
        switch (connectedCar.getMultiTrx().getCurrentBLEChannel(trxNumber)) {
            case BLE_CHANNEL_37:
                result = " 37";
                break;
            case BLE_CHANNEL_38:
                result = " 38";
                break;
            case BLE_CHANNEL_39:
                result = " 39";
                break;
            default:
                result = "   ";
                break;
        }
        return result;
    }
}
