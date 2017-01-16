package com.valeo.bleranging.utils;

import android.text.SpannableStringBuilder;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface BleRangingListener {
    void lightUpArea(String area);

    void darkenArea(String area);

    void updateCarDrawable();
    void applyNewDrawable();
    void printDebugInfo(SpannableStringBuilder spannableStringBuilder);

    void showSnackBar(String message);

    void updateBLEStatus();
    void updateCarDoorStatus(boolean lockStatus);

    void askBleOn();
    void doWelcome();
}
