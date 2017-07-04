package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;

import com.valeo.bleranging.utils.PSALogs;

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
    private static final int MAX_HISTORIC_SIZE = 5;// rssi history size
    private final LinkedHashMap<Integer, List<Double>> rssiHistoric;// rssi saved to calculate the average
    private double coord;// regression prediction result
    private String predictionType;// standard prediction or rp prediction

    public PredictionCoord(Context context, String modelClassName, List<String> rowDataKeySet) {
        super(context, modelClassName, rowDataKeySet);
        this.rssiHistoric = new LinkedHashMap<>();
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.rssi = new double[rssi.length];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi[i] = rssi_offset[i];
            rssiHistoric.put(i, new ArrayList<Double>());
            rssiHistoric.get(i).add(this.rssi[i]);
        }
        constructRowData(this.rssi);
        try {
            final RegressionModelPrediction modelPrediction = modelWrapper.predictRegression(rowData);
            coord = modelPrediction.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setRssi(double rssi[], int offset) {
        if (this.rssi_offset != null) {
            for (int index = 0; index < rssi.length; index++) {
                this.rssi_offset[index] = rssi[index] - offset;
                this.rssi[index] = correctRssiUnilateral(this.rssi[index], rssi_offset[index]);
                if (rssiHistoric.get(index).size() == MAX_HISTORIC_SIZE) {
                    rssiHistoric.get(index).remove(0);
                }
                rssiHistoric.get(index).add(this.rssi[index]);
            }
            double[] rssiAverage = new double[rssi.length];
            for (int i = 0; i < rssi.length; i++) {
                rssiAverage[i] = averageRssi(rssiHistoric.get(i));
            }
            constructRowData(this.rssi);
        }
    }

    public void calculatePredictionCoord() {
        try {
            final RegressionModelPrediction modelPrediction = modelWrapper.predictRegression(rowData);
            coord = modelPrediction.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getPredictionCoord() {
        return coord;
    }

    public String printDebug() {
        return String.format(Locale.FRANCE, "%.2f", coord);
    }
}
