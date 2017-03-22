package com.valeo.bleranging.listeners;

/**
 * Created by l-avaratha on 19/07/2016
 */
public interface ChessBoardListener {
    void applyNewDrawable();

    void updateChessboard(final float pointX, final float pointY);
}
