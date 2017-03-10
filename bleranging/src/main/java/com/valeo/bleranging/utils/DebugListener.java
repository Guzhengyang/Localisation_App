package com.valeo.bleranging.utils;

import android.text.SpannableStringBuilder;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface DebugListener {
    void lightUpArea(String area);

    void darkenArea(String area);

    void applyNewDrawable();

    void printDebugInfo(SpannableStringBuilder spannableStringBuilder);

    void updateCarDrawable(boolean isLocked);
}
