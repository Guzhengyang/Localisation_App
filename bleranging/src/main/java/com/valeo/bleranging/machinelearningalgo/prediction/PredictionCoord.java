package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.graphics.PointF;
import android.os.AsyncTask;
import android.widget.Toast;

import weka.classifiers.functions.MultilayerPerceptron;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.SerializationHelper;

/**
 * Created by l-avaratha on 12/01/2017
 */

public class PredictionCoord {
    private static final double THRESHOLD_RSSI_AWAY = 1;
    private static final double THRESHOLD_DIST = 0.3;
    private static final int MAX_ROWS = 11;
    private static final int MAX_COLUMNS = 10;
    private Context mContext;
    private MultilayerPerceptron mlp_Px;
    private MultilayerPerceptron mlp_Py;
    private Instance sample;
    private double[] rssi_offset;
    private double[] rssi;
    private double[] coord;
    private boolean arePredictRawFileRead = false;

    public PredictionCoord(Context context, int modelId) {
        this.mContext = context;
        new AsyncPredictionInit().execute(modelId);
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    public void init(double[] rssi, int offset) {
        this.rssi_offset = new double[rssi.length];
        this.rssi = new double[rssi.length];
        this.coord = new double[2];
        for (int i = 0; i < rssi.length; i++) {
            this.rssi_offset[i] = rssi[i] - offset;
            this.rssi[i] = rssi_offset[i];
            sample.setValue(i, this.rssi[i]);
        }
    }

    public void setRssi(int index, double rssi, int offset) {
        this.rssi_offset[index] = rssi - offset;
        this.rssi[index] = correctRssiUnilateral(this.rssi[index], rssi_offset[index]);
        sample.setValue(index, this.rssi[index]);
    }

    public void calculatePredictionCoord() {
        double[] coord_new = new double[2];
        try {
            coord_new[0] = mlp_Px.classifyInstance(sample);
            coord_new[1] = mlp_Py.classifyInstance(sample);
            correctCoord(coord_new, THRESHOLD_DIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PointF getPredictionCoord() {
        return new PointF((float) coord[0], MAX_COLUMNS - (float) coord[1]);
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

    private class AsyncPredictionInit extends AsyncTask<Integer, Void, Void> {

        @Override
        protected Void doInBackground(Integer... elements) {
            try {
                Object[] model = (Object[]) SerializationHelper.read(mContext.getResources().openRawResource(elements[0]));
                mlp_Px = (MultilayerPerceptron) model[0];
                mlp_Py = (MultilayerPerceptron) model[1];
                Instances instances = (Instances) model[2];
                sample = instances.instance(0);
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
                Toast.makeText(mContext, "Init for MLP failed", Toast.LENGTH_SHORT).show();
            }
        }
    }
}