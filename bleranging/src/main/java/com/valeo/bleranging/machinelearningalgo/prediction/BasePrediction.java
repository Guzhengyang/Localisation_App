package com.valeo.bleranging.machinelearningalgo.prediction;

import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import hex.genmodel.easy.EasyPredictModelWrapper;
import hex.genmodel.easy.RowData;


/**
 * Created by l-avaratha on 12/01/2017
 */

public class BasePrediction {
    protected List<EasyPredictModelWrapper> modelWrappers = new ArrayList<>();
    protected RowData rowData;
    protected Context mContext;
    protected double[] rssi_offset; //    rssi after adding smartphone offset
    protected double[] rssi; //    rssi used for algo entry
    protected boolean binomial; // whether the model is binomial
    private boolean arePredictRawFileRead = false;
    private List<String> rowDataKeySet;

    public BasePrediction(Context context, String modelClassName, List<String> rowDataKeySet) {
        this.mContext = context;
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit().execute(modelClassName);
    }

    public BasePrediction(Context context, String modelClassNameX, String modelClassNameY, List<String> rowDataKeySet) {
        this.mContext = context;
        this.rowDataKeySet = rowDataKeySet;
        this.rowData = new RowData();
        new AsyncPredictionInit().execute(modelClassNameX, modelClassNameY);
    }

    protected void constructRowData(double[] rssi) {
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

    public double[] getRssi() {
        return rssi;
    }

    public boolean isPredictRawFileRead() {
        return arePredictRawFileRead;
    }

    private class AsyncPredictionInit extends AsyncTask<String, Void, Void> {

        @Override
        protected Void doInBackground(String... elements) {
            try {
                for (String genModelClassName : elements) {
                    hex.genmodel.GenModel rawModel = (hex.genmodel.GenModel) Class.forName(genModelClassName).newInstance();
                    modelWrappers.add(new EasyPredictModelWrapper(rawModel));
                    binomial = rawModel.getNumResponseClasses() == 2;
                }
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
