package com.valeo.psa.view;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.content.res.ResourcesCompat;
import android.util.AttributeSet;
import android.view.View;

import com.valeo.psa.R;

/**
 * Created by Weiping on 2016/3/16
 */
public class BlurView extends View {

    private final int[] mBlurredViewXY = new int[2];
    private final int[] mBlurringViewXY = new int[2];
    private int mBlurRadius;
    private int mDownsampleFactor;
    private int mOverlayColor;
    private View mBlurredView;
    private int mBlurredViewWidth, mBlurredViewHeight;
    private boolean mDownsampleFactorChanged;
    private Bitmap mBitmapToBlur, mBlurredBitmap;
    private Canvas mBlurringCanvas;
    private RenderScript mRenderScript;
    private ScriptIntrinsicBlur mBlurScript;
    private Allocation mBlurInput, mBlurOutput;

    public BlurView(Context context) {
        this(context, null);
    }

    public BlurView(Context context, AttributeSet attrs) {
        super(context, attrs);

        final Resources res = getResources();
        final int defaultBlurRadius = (int) res.getDimension(R.dimen.default_blur_radius);
        final int defaultDownsampleFactor = (int) res.getDimension(R.dimen.default_downsample_factor);
        final int defaultOverlayColor = ResourcesCompat.getColor(getResources(), R.color.default_overlay_color, null);

        initializeRenderScript(context);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.BlurView);
        setBlurRadius(a.getInt(R.styleable.BlurView_blurRadius, defaultBlurRadius));
        setDownsampleFactor(a.getInt(R.styleable.BlurView_downsampleFactor,
                defaultDownsampleFactor));
        setOverlayColor(a.getColor(R.styleable.BlurView_overlayColor, defaultOverlayColor));
        a.recycle();
    }

    public void setBlurredView(View blurredView) {
        mBlurredView = blurredView;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if (mBlurredView != null) {
            if (prepare()) {
                // If the background of the blurred view is a color drawable, we use it to clear
                // the blurring canvas, which ensures that edges of the child views are blurred
                // as well; otherwise we clear the blurring canvas with a transparent color.
                if (mBlurredView.getBackground() != null && mBlurredView.getBackground() instanceof ColorDrawable) {
                    mBitmapToBlur.eraseColor(((ColorDrawable) mBlurredView.getBackground()).getColor());
                } else {
                    mBitmapToBlur.eraseColor(Color.TRANSPARENT);
                }

                mBlurredViewXY[0] = 0;
                mBlurredViewXY[1] = 0;
                mBlurredView.getLocationOnScreen(mBlurredViewXY);
                mBlurringViewXY[0] = 0;
                mBlurringViewXY[1] = 0;
                getLocationOnScreen(mBlurringViewXY);

                mBlurredView.draw(mBlurringCanvas);
                blur();

                canvas.save();

                // modify here to get the correct bitmap when the blurring view is in a parent
                canvas.translate(mBlurredViewXY[0] - mBlurringViewXY[0], mBlurredViewXY[1] - mBlurringViewXY[1]);
                canvas.scale(mDownsampleFactor, mDownsampleFactor);
                canvas.drawBitmap(mBlurredBitmap, 0, 0, null);
                canvas.restore();
            }
            canvas.drawColor(mOverlayColor);
        }
    }

    public int getBlurRadius() {
        return mBlurRadius;
    }

    private void setBlurRadius(int radius) {
        mBlurRadius = radius;
        mBlurScript.setRadius(mBlurRadius);
    }

    public int getDownsampleFactor() {
        return mDownsampleFactor;
    }

    private void setDownsampleFactor(int factor) {
        if (factor <= 0) {
            throw new IllegalArgumentException("Downsample factor must be greater than 0.");
        }

        if (mDownsampleFactor != factor) {
            mDownsampleFactor = factor;
            mDownsampleFactorChanged = true;
        }
    }

    private void setOverlayColor(int color) {
        mOverlayColor = color;
    }

    public int getmOverlayColor() {
        return mOverlayColor;
    }

    private void initializeRenderScript(Context context) {
        mRenderScript = RenderScript.create(context);
        mBlurScript = ScriptIntrinsicBlur.create(mRenderScript, Element.U8_4(mRenderScript));
    }

    private boolean prepare() {
        final int width = mBlurredView.getWidth();
        final int height = mBlurredView.getHeight();

        if (mBlurringCanvas == null || mDownsampleFactorChanged
                || mBlurredViewWidth != width || mBlurredViewHeight != height) {
            mDownsampleFactorChanged = false;

            mBlurredViewWidth = width;
            mBlurredViewHeight = height;

            int scaledWidth = width / mDownsampleFactor;
            int scaledHeight = height / mDownsampleFactor;

            // The following manipulation is to avoid some RenderScript artifacts at the edge.
            scaledWidth = scaledWidth - scaledWidth % 4 + 4;
            scaledHeight = scaledHeight - scaledHeight % 4 + 4;

            if (mBlurredBitmap == null
                    || mBlurredBitmap.getWidth() != scaledWidth
                    || mBlurredBitmap.getHeight() != scaledHeight) {
                mBitmapToBlur = Bitmap.createBitmap(scaledWidth, scaledHeight,
                        Bitmap.Config.ARGB_8888);
                if (mBitmapToBlur == null) {
                    return false;
                }

                mBlurredBitmap = Bitmap.createBitmap(scaledWidth, scaledHeight,
                        Bitmap.Config.ARGB_8888);
                if (mBlurredBitmap == null) {
                    return false;
                }
            }

            mBlurringCanvas = new Canvas(mBitmapToBlur);
            mBlurringCanvas.scale(1f / mDownsampleFactor, 1f / mDownsampleFactor);
            mBlurInput = Allocation.createFromBitmap(mRenderScript, mBitmapToBlur,
                    Allocation.MipmapControl.MIPMAP_NONE, Allocation.USAGE_SCRIPT);
            mBlurOutput = Allocation.createTyped(mRenderScript, mBlurInput.getType());
        }
        return true;
    }

    private void blur() {
        mBlurInput.copyFrom(mBitmapToBlur);
        mBlurScript.setInput(mBlurInput);
        mBlurScript.forEach(mBlurOutput);
        mBlurOutput.copyTo(mBlurredBitmap);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (mRenderScript != null) {
            mRenderScript.destroy();
        }
    }

}
