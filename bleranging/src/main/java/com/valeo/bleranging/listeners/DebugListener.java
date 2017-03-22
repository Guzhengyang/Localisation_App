package com.valeo.bleranging.listeners;

import android.text.SpannedString;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface DebugListener {
    void lightUpArea(String area);

    void darkenArea(String area);

    void applyNewDrawable();

    void printDebugInfo(final SpannedString spannedString);

    void updateCarDrawable(boolean isLocked);
}
