package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.util.SparseArray;

import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.prediction.RegressionModelPrediction;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionCoord extends BasePrediction {
    public static final int INDEX_KALMAN = 0; // use kalman filter
    public static final int INDEX_THRESHOLD = 1; // use threshold filter
    public static final int INDEX_RAW = 2; // use no filter
    public static final int INDEX_MAX = 3; // number of comparison result
    private final SparseArray<Coord> coords; // regression prediction result

    public PredictionCoord(Context context, String modelClassNameX, String modelClassNameY, List<String> rowDataKeySet) {
        super(context, modelClassNameX, modelClassNameY, rowDataKeySet);
        this.coords = new SparseArray<>();
    }

    /***
     *  initialization
     * @param rssi raw rssi vector
     * @param offset smartphone offset value
     */
    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.rssi_modified = new double[rssi.length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi_modified[i] = rssi_offset[i];
        }
        constructRowData(this.rssi_modified);
        for (int i = 0; i < INDEX_MAX; i++) { // doo not use getCoordsSize() because it's empty
            coords.put(i, new Coord());
            try {
                final RegressionModelPrediction modelPredictionX = modelWrappers.get(0).predictRegression(rowData);
                coords.get(i).setCoord_x(modelPredictionX.value);
                final RegressionModelPrediction modelPredictionY = modelWrappers.get(1).predictRegression(rowData);
                coords.get(i).setCoord_y(modelPredictionY.value);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /***
     * update rowData object for ML prediction
     * @param rssi raw rssi vector
     * @param offset
     */
    public void setRssi(double rssi[], int offset) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                this.rssi_modified[index] = this.rssi_offset[index]; // don't use unilateral limiter
            }
            constructRowData(this.rssi_modified);
        }
    }

    /***
     * get coord prediction results
     * @return coord
     */
    public Coord getMLCoord() {
        Coord coordML = null;
        try {
            coordML = new Coord();
            final RegressionModelPrediction modelPredictionX = modelWrappers.get(0).predictRegression(rowData);
            coordML.setCoord_x(modelPredictionX.value);
            final RegressionModelPrediction modelPredictionY = modelWrappers.get(1).predictRegression(rowData);
            coordML.setCoord_y(modelPredictionY.value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordML;
    }

    /***
     * get coord results after different filter(kalman, threshold, no filtering)
     * @param index
     * @return
     */
    public Coord getPredictionCoord(final int index) {
        if (index < coords.size()) {
            return coords.get(index);
        }
        return null;
    }

    @Override
    public String printDebug() {
        final StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < getCoordsSize(); i++) {
            if (coords.get(i) != null) {
                stringBuilder.append(String.format(Locale.FRANCE, "x: %.2f   y: %.2f " + getSerieName(i) + "\n",
                        coords.get(i).getCoord_x(), coords.get(i).getCoord_y()));
            }
        }
        return stringBuilder.toString();
    }

    /**
     * Get a serie name
     *
     * @param index the serie index
     * @return the name of the serie
     */
    private String getSerieName(final int index) {
        switch (index) {
            case 0:
                return "KALMAN";
            case 1:
                return "THRESHOLD";
            case 2:
                return "RAW";
            default:
                return "UNKNOWN";
        }
    }

    /**
     * Get the coord tab size
     *
     * @return the size of the coord tab
     */
    public int getCoordsSize() {
        return coords.size();
    }
}
