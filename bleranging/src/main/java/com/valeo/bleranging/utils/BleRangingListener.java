package com.valeo.bleranging.utils;

import android.text.SpannableStringBuilder;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface BleRangingListener {
    void lightUpArea(int area);
    void darkenArea(int area);

    void applyNewDrawable();
    void printDebugInfo(SpannableStringBuilder spannableStringBuilder);

    void updateBLEStatus();
    void updateCarDoorStatus(boolean lockStatus);

    void updateCarDrawable();

    void doWelcome();
}
