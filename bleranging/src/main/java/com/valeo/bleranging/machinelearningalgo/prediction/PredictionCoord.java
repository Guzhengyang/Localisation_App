package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.RegressionModelPrediction;


/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionCoord {
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private static final int MAX_X = 11;
    private static final int MAX_HISTORIC_SIZE = 5;
    //    rssi saved to calculate the average
    private final LinkedHashMap<Integer, List<Double>> rssiHistoric;
    private Context mContext;
    //    rssi after adding smartphone offset
    private double[] rssi_offset;
    //    rssi used for algo entry
    private double[] rssi;
    private boolean arePredictRawFileRead = false;
    private EasyPredictModelWrapper modelWrapper;
    private RowData rowData;
    private List<String> rowDataKeySet;
    private double coord;

    public PredictionCoord(Context context, String modelClassName, List<String> rowDataKeySet) {
        this.mContext = context;
        this.rssiHistoric = new LinkedHashMap<>();
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit().execute(modelClassName);
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    private void constructRowData(double[] rssi) {
        if (rssi == null) {
            return;
        }
        int i = 0;
        for (String elem : rowDataKeySet) {
            if (i < rssi.length) {
                rowData.put(elem, String.valueOf(rssi[i]));
                i++;
            }
        }
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

    private double averageRssi(List<Double> rssiList) {
        if (rssiList == null || rssiList.size() == 0) {
            return 0;
        }
        double somme = 0;
        for (Double elem : rssiList) {
            somme += elem;
        }
        return somme / rssiList.size();
    }

    public void calculatePredictionCoord() {
        try {
            final RegressionModelPrediction modelPrediction = modelWrapper.predictRegression(rowData);
            coord = modelPrediction.value;
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double[] point2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / (2 * MAX_X + 1));
        coord[0] = index - coord[1] * (2 * MAX_X + 1);
        return coord;
    }

    public double[] square2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / MAX_X);
        coord[0] = index - coord[1] * MAX_X;
        coord[0] = coord[0] + 0.5;
        coord[1] = coord[1] + 0.5;
        return coord;
    }

    public double getPredictionCoord() {
        return coord;
    }

    public String printDebug() {
        return String.format(Locale.FRANCE, "%.2f", coord);
    }

    private double correctRssiUnilateral(double rssi_old, double rssi_new) {
        double rssi_correted;
        if (rssi_new > rssi_old) {
            rssi_correted = rssi_new;
        } else {
            rssi_correted = rssi_old - Math.min(rssi_old - rssi_new, THRESHOLD_RSSI_AWAY);
        }
        return rssi_correted;
    }

    public double[] getRssi() {
        return rssi;
    }

    private class AsyncPredictionInit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... elements) {
            try {
                hex.genmodel.GenModel rawModel = (hex.genmodel.GenModel) Class.forName(elements[0]).newInstance();
                modelWrapper = new EasyPredictModelWrapper(rawModel);
                arePredictRawFileRead = true;
            } catch (Exception e) {
                e.printStackTrace();
                arePredictRawFileRead = false;
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            if (!arePredictRawFileRead) {
                Toast.makeText(mContext, "Init for Coord Model Fail", Toast.LENGTH_LONG).show();
            }
        }
    }
}
