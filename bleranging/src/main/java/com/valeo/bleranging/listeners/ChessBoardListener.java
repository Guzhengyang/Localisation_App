package com.valeo.bleranging.listeners;

import android.graphics.PointF;

import java.util.List;

/**
 * Gateway between the BLE and the chessboard, sending data to it and updating it when needed.
 */
public interface ChessBoardListener {
    void applyNewDrawable();

    void updateChessboard(final List<PointF> point, final List<Double> dist);
}
