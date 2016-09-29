package com.valeo.psa.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.view.View;

/**
 * Utils class used to generate a blurred image from a View.
 *
 * @see "http://stackoverflow.com/questions/6795483/create-blurry-transparent-background-effect"
 */
public class BlurBuilder {
    /** A scale applied to the view background. */
    private static final float BITMAP_SCALE = 0.4f;

    /** The blur default radius. */
    private static final float BLUR_RADIUS = 7.5f;

    /**
     * Generate and return a blurred version of the provided view content.
     *
     * @param v the source view.
     * @return the generated blurred image.
     */
    public static Bitmap blur(final View v) {
        return blur(v.getContext(), getScreenshot(v));
    }

    /**
     * Generate and return a blurred version of the provided bitmap.
     *
     * @param ctx   the execution context.
     * @param image the source image.
     * @return the generated blurred image.
     */
    private static Bitmap blur(final Context ctx, final Bitmap image) {
        int width = Math.round(image.getWidth() * BITMAP_SCALE);
        int height = Math.round(image.getHeight() * BITMAP_SCALE);

        Bitmap inputBitmap = Bitmap.createScaledBitmap(image, width, height, false);
        Bitmap outputBitmap = Bitmap.createBitmap(inputBitmap);

        RenderScript rs = RenderScript.create(ctx);
        ScriptIntrinsicBlur theIntrinsic = ScriptIntrinsicBlur.create(rs, Element.U8_4(rs));
        Allocation tmpIn = Allocation.createFromBitmap(rs, inputBitmap);
        Allocation tmpOut = Allocation.createFromBitmap(rs, outputBitmap);
        theIntrinsic.setRadius(BLUR_RADIUS);
        theIntrinsic.setInput(tmpIn);
        theIntrinsic.forEach(tmpOut);
        tmpOut.copyTo(outputBitmap);

        return outputBitmap;
    }

    /**
     * Generate and return a bitmap representing the provided view content.
     *
     * @param v the source view.
     * @return the generated bitmap.
     */
    private static Bitmap getScreenshot(final View v) {
        Bitmap b = Bitmap.createBitmap(v.getWidth(), v.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(b);
        v.draw(c);
        return b;
    }
}
