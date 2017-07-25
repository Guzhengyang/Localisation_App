package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.util.SparseArray;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.prediction.RegressionModelPrediction;

import static com.valeo.bleranging.utils.CalculUtils.averageRssi;
import static com.valeo.bleranging.utils.CalculUtils.correctRssiUnilateral;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionCoord extends BasePrediction {
    public static final int INDEX_KALMAN = 0;
    public static final int INDEX_THRESHOLD = 1;
    private static final int MAX_HISTORIC_SIZE = 5;// rssi history size
    private final LinkedHashMap<Integer, List<Double>> rssiHistoric;// rssi saved to calculate the average
    private final SparseArray<Coord> coords; // regression prediction result
    private String predictionType;// standard prediction or rp prediction

    public PredictionCoord(Context context, String modelClassNameX, String modelClassNameY, List<String> rowDataKeySet) {
        super(context, modelClassNameX, modelClassNameY, rowDataKeySet);
        this.rssiHistoric = new LinkedHashMap<>();
        this.coords = new SparseArray<>();
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.modified_rssi = new double[rssi.length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.modified_rssi[i] = rssi_offset[i];
            rssiHistoric.put(i, new ArrayList<Double>());
            rssiHistoric.get(i).add(this.modified_rssi[i]);
        }
        constructRowData(this.modified_rssi);
        for (int i = 0; i < 2; i++) {
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

    public void setRssi(double rssi[], int offset) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                this.modified_rssi[index] = correctRssiUnilateral(this.modified_rssi[index], rssi_offset[index]);
                if (rssiHistoric.get(index).size() == MAX_HISTORIC_SIZE) {
                    rssiHistoric.get(index).remove(0);
                }
                rssiHistoric.get(index).add(this.modified_rssi[index]);
            }
            double[] rssiAverage = new double[rssi.length];
            for (int i = 0; i < rssi.length; i++) {
                rssiAverage[i] = averageRssi(rssiHistoric.get(i));
            }
            constructRowData(this.modified_rssi);
        }
    }

    public Coord getMLCoord() {
        final Coord coordML = new Coord();
        try {
            final RegressionModelPrediction modelPredictionX = modelWrappers.get(0).predictRegression(rowData);
            coordML.setCoord_x(modelPredictionX.value);
            final RegressionModelPrediction modelPredictionY = modelWrappers.get(1).predictRegression(rowData);
            coordML.setCoord_y(modelPredictionY.value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return coordML;
    }

    public Coord getPredictionCoord(final int index) {
        if (index < coords.size()) {
            return coords.get(index);
        }
        return null;
    }

    @Override
    public String printDebug() {
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < 2; i++) {
            if (coords.get(i) != null) {
                stringBuilder.append(String.format(Locale.FRANCE, "x: %.2f   y: %.2f \n",
                        coords.get(i).getCoord_x(), coords.get(i).getCoord_y()));
            }
        }
        return stringBuilder.toString();
    }

    public int getCoordsSize() {
        return coords.size();
    }
}
