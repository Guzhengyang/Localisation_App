package com.valeo.psa.view;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.ProgressBar;

public class ReverseProgressBar extends ProgressBar {

    public ReverseProgressBar(Context context) {
        super(context);
    }

    public ReverseProgressBar(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ReverseProgressBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        canvas.save();
        //now we change the matrix
        //We need to scale around the center of our text
        //Otherwise it scale around the origin, and that's bad.
        canvas.scale(-1f, 1f, super.getWidth() * 0.5f, super.getHeight() * 0.5f);
        super.onDraw(canvas);
        canvas.restore();
    }
}
