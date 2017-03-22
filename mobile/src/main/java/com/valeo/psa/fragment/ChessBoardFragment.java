package com.valeo.psa.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.valeo.bleranging.listeners.ChessBoardListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Created by l-avaratha on 21/03/2017
 */
public class ChessBoardFragment extends Fragment implements ChessBoardListener {
    private static final int MAX_POSITIONS = 15;
    private static final int MAX_ROWS = 11;
    private static final int MAX_COLUMNS = 10;
    private static final int MAX_WIDTH = 700;
    private final Paint paintOne = new Paint();
    private final Paint paintTwo = new Paint();
    private final Paint paintCar = new Paint();
    private final Paint paintUnlock = new Paint();
    private final Paint paintLock = new Paint();
    private final ArrayList<PointF> positions = new ArrayList<>(MAX_POSITIONS);
    private ImageView chessboard;
    private int measuredWidth;
    private int stepX;
    private int measuredHeight;
    private int stepY;
    private PointF lastLoc;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.chessboard_fragment, container, false);
        setView(rootView);
        return rootView;
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        chessboard = (ImageView) rootView.findViewById(R.id.chessboard);
        setPaint();
        setSteps();
    }

    private void setSteps() {
        measuredWidth = MAX_WIDTH;
        stepX = measuredWidth / MAX_ROWS;
        measuredHeight = stepX * MAX_COLUMNS;
        stepY = measuredHeight / MAX_COLUMNS;
    }

    private void setPaint() {
        paintOne.setColor(Color.BLACK);
        paintOne.setStyle(Paint.Style.STROKE);
        paintOne.setStrokeWidth(5f);
        paintTwo.setColor(Color.LTGRAY);
        paintTwo.setStyle(Paint.Style.STROKE); // print border
        paintCar.setColor(Color.DKGRAY);
        paintUnlock.setColor(Color.GREEN);
        paintLock.setColor(Color.RED);
    }

    @Override
    public void applyNewDrawable() {
        chessboard.setBackground(drawChessBoard());
    }

    @Override
    public void updateChessboard(final PointF point) {
        chessboard.setImageBitmap(placeUserOnChessBoard(point));
    }

    private Bitmap placeUserOnChessBoard(final PointF point) {
        final Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final PointF currentLoc = point;
        if (positions.size() == MAX_POSITIONS) {
            positions.remove(0);
        }
        positions.add(currentLoc);
        if (positions.size() == 1 || lastLoc == null) {
            lastLoc = currentLoc;
        }
        for (int index = 0; index + 1 < positions.size(); index++) {
            canvas.drawLine(positions.get(index).x, positions.get(index).y, positions.get(index + 1).x, positions.get(index + 1).y, paintOne);
        }
        PSALogs.d("chess", String.format(Locale.FRANCE, "%.1f %.1f\n %.1f %.1f", lastLoc.x, lastLoc.y, currentLoc.x, currentLoc.y));
        lastLoc = currentLoc;
        return bitmap;
    }

    private BitmapDrawable drawChessBoard() {
        final Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        PSALogs.i("chess", "stepX " + stepX + " measuredWidth " + measuredWidth);
        canvas.drawRect(0, 0, measuredWidth, measuredHeight, paintLock); //lock rect
        canvas.drawRect(stepX, stepY * 2, stepX * 9, stepY * 8, paintUnlock); // unlock rect
        for (int width = 0, x = 0; width < measuredWidth && x < MAX_ROWS; width += stepX, x++) {
            canvas.drawLine(width, 0, width, measuredHeight, paintTwo); // rows lines
        }
        for (int height = 0, y = 0; height < measuredHeight && y < MAX_COLUMNS; height += stepY, y++) {
            canvas.drawLine(0, height, measuredWidth, height, paintTwo); // columns lines
        }
        canvas.drawRect(stepX * 3, stepY * 4, stepX * 7, stepY * 6, paintCar); // car rect
        return new BitmapDrawable(getResources(), bitmap);
    }
}
