package com.valeo.bleranging.listeners;

import android.graphics.PointF;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface ChessBoardListener {
    void applyNewDrawable();

    void updateChessboard(final PointF point);
}
