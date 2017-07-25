package com.valeo.psa.fragment;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.DisplayMetrics;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.valeo.bleranging.listeners.ChessBoardListener;
import com.valeo.bleranging.utils.PSALogs;
import com.valeo.psa.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Created by l-avaratha on 21/03/2017
 */
public class ChessBoardFragment extends Fragment implements ChessBoardListener {
    private static final int MAX_POSITIONS = 5;
    private static final float MAX_ROWS = 11;
    private static final float MAX_COLUMNS = 10;
    private final Paint paintOne = new Paint();
    private final Paint paintTwo = new Paint();
    private final Paint paintThree = new Paint();
    private final Paint paintFour = new Paint();
    private final Paint paintCar = new Paint();
    private final Paint paintUnlock = new Paint();
    private final Paint paintLock = new Paint();
    private final SparseArray<ArrayList<PointF>> positions = new SparseArray<>();
    private final SparseArray<ArrayList<Paint>> paints = new SparseArray<>();
    private final SparseArray<Path> paths = new SparseArray<>();
    private ImageView chessboard;
    private TextView chessboard_debug_info;
    private int measuredWidth;
    private float stepX;
    private int measuredHeight;
    private float stepY;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        super.onCreateView(inflater, container, savedInstanceState);
        View rootView = inflater.inflate(R.layout.chessboard_fragment, container, false);
        setView(rootView);
        positions.put(0, new ArrayList<PointF>(MAX_POSITIONS));
        positions.put(1, new ArrayList<PointF>(MAX_POSITIONS));
        paints.put(0, new ArrayList<Paint>());
        paints.put(1, new ArrayList<Paint>());
        paints.get(0).add(paintOne);
        paints.get(0).add(paintThree);
        paints.get(1).add(paintOne);
        paints.get(1).add(paintFour);
        paths.put(0, new Path());
        paths.put(1, new Path());
        return rootView;
    }

    /**
     * Find all view by their id
     */
    private void setView(View rootView) {
        chessboard = (ImageView) rootView.findViewById(R.id.chessboard);
        chessboard_debug_info = (TextView) rootView.findViewById(R.id.chessboard_debug_info);
        setPaint();
        setSteps();
    }

    private void setSteps() {
        DisplayMetrics metrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(metrics);
        measuredWidth = metrics.widthPixels;
        stepX = (float) (Math.round((measuredWidth / MAX_ROWS) * 100.0) / 100.0);
        measuredHeight = (int) (stepX * MAX_COLUMNS);
        stepY = (float) (Math.round((measuredHeight / MAX_COLUMNS) * 100.0) / 100.0);
    }

    private void setPaint() {
        paintOne.setColor(Color.BLACK);
        paintOne.setStyle(Paint.Style.STROKE);
        paintOne.setStrokeWidth(5f);
        paintTwo.setColor(Color.LTGRAY);
        paintTwo.setStyle(Paint.Style.STROKE); // print border
        paintThree.setColor(Color.YELLOW); // KALMAN
        paintThree.setStrokeWidth(25f);
        paintFour.setColor(Color.BLUE); // THRESHOLD
        paintFour.setStrokeWidth(25f);
        paintCar.setColor(Color.DKGRAY);
        paintUnlock.setColor(Color.GREEN);
        paintLock.setColor(Color.RED);
    }

    @Override
    public void applyNewDrawable() {
        chessboard.setBackground(drawChessBoard());
    }

    @Override
    public void updateChessboard(final List<PointF> points, final List<Double> dists) {
        if (points != null && dists != null) {
            chessboard.setImageBitmap(placeUserOnChessBoard(points, dists));
        }
    }

    private Bitmap placeUserOnChessBoard(final List<PointF> points, final List<Double> dists) {
        final Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < points.size(); i++) {
            final PointF point = points.get(i);
            final ArrayList<PointF> positionHistoric = positions.get(i);
            final ArrayList<Paint> paint = paints.get(i);
            final Path path = paths.get(i);
            PSALogs.d("chess", String.format(Locale.FRANCE, "coord : %.1f %.1f", point.x, point.y));
            stringBuilder.append(String.format(Locale.FRANCE,
                    "coord : x = %.1f      y = %.1f    distance : %.1f \n", point.x, point.y, dists.get(i)));
            point.y = 10 - point.y;
            point.x *= stepX;
            point.y *= stepY;
            PSALogs.d("chess", String.format(Locale.FRANCE, "pixel : %.1f %.1f", point.x, point.y));
            if (positionHistoric.size() == MAX_POSITIONS) {
                positionHistoric.remove(0);
                path.reset();
                path.moveTo(positionHistoric.get(0).x, positionHistoric.get(0).y);
            }
            positionHistoric.add(point);
            if (positionHistoric.size() == 1) {
                positionHistoric.add(point);
                path.moveTo(point.x, point.y);
            }
            for (int index = 1; index < positionHistoric.size(); index++) {
                PointF tempPoint = positionHistoric.get(index);
                path.lineTo(tempPoint.x, tempPoint.y);
            }
            canvas.drawPath(path, paint.get(0));
            canvas.drawPoint(point.x, point.y, paint.get(1));
        }
        chessboard_debug_info.setText(stringBuilder.toString());
        return bitmap;
    }

    private BitmapDrawable drawChessBoard() {
        final Bitmap bitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888);
        final Canvas canvas = new Canvas(bitmap);
        PSALogs.i("chess", "stepX " + stepX + " measuredWidth " + measuredWidth);
        PSALogs.i("chess", "stepY " + stepY + " measuredHeight " + measuredHeight);
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
