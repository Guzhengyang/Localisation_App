package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;
import hex.genmodel.easy.prediction.MultinomialModelPrediction;


/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionCoord {
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private static final double THRESHOLD_DIST = 0.5;
    private static final int MAX_ROWS = 11;
    private static final int MAX_COLUMNS = 10;
    private static final int MAX_HISTORIC_SIZE = 5;
    private static double X1 = 3, X2 = 7, Y1 = 4, Y2 = 6;
    private final LinkedHashMap<Integer, List<Double>> rssiHistoric;
    private Context mContext;
    private double[] rssi_offset;
    private double[] rssi;
    private double[] coord;
    private double dist;
    private boolean arePredictRawFileRead = false;
    private EasyPredictModelWrapper modelWrapper;
    private RowData rowData;
    private List<String> rowDataKeySet;
    private String label;

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
        this.coord = new double[2];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi[i] = rssi_offset[i];
            rssiHistoric.put(i, new ArrayList<Double>());
            rssiHistoric.get(i).add(this.rssi[i]);
        }
        constructRowData(rssi);
        try {
            String index;
            final MultinomialModelPrediction modelPrediction = modelWrapper.predictMultinomial(rowData);
            label = modelPrediction.label;
            index = label.replace("S", "");
            this.coord = square2PxPy(Integer.parseInt(index));
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
            constructRowData(rssiAverage);
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

    public void calculatePredictionCoord(float[] orientation) {
        double[] coord_new;
        String index;
        try {
            final MultinomialModelPrediction modelPrediction = modelWrapper.predictMultinomial(rowData);
            label = modelPrediction.label;
            index = label.replace("S", "");
            coord_new = square2PxPy(Integer.parseInt(index));
            correctCoord(coord_new, THRESHOLD_DIST);
            calculateDist2Car();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double[] point2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / (2 * MAX_ROWS + 1));
        coord[0] = index - coord[1] * (2 * MAX_ROWS + 1);
        return coord;
    }

    public double[] square2PxPy(int index) {
        double[] coord = new double[2];
        index = index - 1;
        coord[1] = Math.floor(index / MAX_ROWS);
        coord[0] = index - coord[1] * MAX_ROWS;
        coord[0] = coord[0] + 0.5;
        coord[1] = coord[1] + 0.5;
        return coord;
    }

    public PointF getPredictionCoord() {
        return new PointF((float) coord[0], MAX_COLUMNS - (float) coord[1]);
    }

    public double getDist2Car() {
        return dist;
    }


    private void correctCoord(double[] coord_new, double threshold_dist) {
        double deltaX = coord_new[0] - coord[0];
        double deltaY = coord_new[1] - coord[1];
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        if (dist > threshold_dist) {
            double ratio = threshold_dist / dist;
            coord[0] = coord[0] + deltaX * ratio;
            coord[1] = coord[1] + deltaY * ratio;
        } else {
            coord[0] = coord_new[0];
            coord[1] = coord_new[1];
        }
        if (coord[0] > MAX_ROWS) {
            coord[0] = MAX_ROWS;
        } else if (coord[0] < 0) {
            coord[0] = 0;
        }
        if (coord[1] > MAX_COLUMNS) {
            coord[1] = MAX_COLUMNS;
        } else if (coord[1] < 0) {
            coord[1] = 0;
        }
    }

    public String printDebug() {
        if (coord != null) {
            return String.format(Locale.FRANCE, "%.2f %.2f", coord[0], coord[1]);
        } else {
            return "";
        }
    }

    private void calculateDist2Car() {
        if ((coord[0] < X1) & (coord[1] < Y1)) {
            dist = Math.sqrt((coord[0] - X1) * (coord[0] - X1) + (coord[1] - Y1) * (coord[1] - Y1));
        } else if ((coord[0] < X1) & (coord[1] > Y2)) {
            dist = Math.sqrt((coord[0] - X1) * (coord[0] - X1) + (coord[1] - Y2) * (coord[1] - Y2));
        } else if ((coord[0] > X2) & (coord[1] < Y1)) {
            dist = Math.sqrt((coord[0] - X2) * (coord[0] - X2) + (coord[1] - Y1) * (coord[1] - Y1));
        } else if ((coord[0] > X2) & (coord[1] > Y2)) {
            dist = Math.sqrt((coord[0] - X2) * (coord[0] - X2) + (coord[1] - Y2) * (coord[1] - Y2));
        } else if ((coord[0] < X1) & (coord[1] > Y1) & (coord[1] < Y2)) {
            dist = X1 - coord[0];
        } else if ((coord[0] > X2) & (coord[1] > Y1) & (coord[1] < Y2)) {
            dist = coord[0] - X2;
        } else if ((coord[1] < Y1) & (coord[0] > X1) & (coord[0] < X2)) {
            dist = Y1 - coord[1];
        } else if ((coord[1] > Y2) & (coord[0] > X1) & (coord[0] < X2)) {
            dist = coord[1] - Y2;
        } else if ((coord[0] > X1) & (coord[0] < X2) & (coord[1] > Y1) & (coord[1] < Y2)) {
            dist = -1;
        }
    }

    private double calculateDist(double[] point1, double[] point2) {
        double deltaX = point1[0] - point1[0];
        double deltaY = point2[1] - point2[1];
        double dist = Math.sqrt(deltaX * deltaX + deltaY * deltaY);
        return dist;
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
